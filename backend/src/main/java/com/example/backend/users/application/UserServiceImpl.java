package com.example.backend.users.application;

import com.example.backend.common.exception.InternalTechnicalException;
import com.example.backend.common.util.PasswordGenerator;
import com.example.backend.security.jwt.JwtService;
import com.example.backend.users.api.dto.*;
import com.example.backend.users.api.mapper.UserMapper;
import com.example.backend.users.domain.exception.InvalidCredentialsException;
import com.example.backend.users.domain.exception.SamePasswordException;
import com.example.backend.users.domain.exception.UserAlreadyExistsException;
import com.example.backend.users.domain.exception.UserNotFoundException;
import com.example.backend.users.domain.model.Role;
import com.example.backend.users.domain.model.RoleName;
import com.example.backend.users.domain.model.User;
import com.example.backend.users.domain.repository.RoleRepository;
import com.example.backend.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PasswordGenerator passwordGenerator;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest request) {
        log.info("Attempting authentication for email: {}", request.getEmail());

        User user = findByEmailOrThrow(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed for email {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User {} authenticated successfully. forcePasswordChange={}",
                user.getEmail(), user.isForcePasswordChange());

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setForcePasswordChange(user.isForcePasswordChange());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String rawRefreshToken) {
        if (!jwtService.isRefreshToken(rawRefreshToken)) {
            throw new InvalidCredentialsException();
        }

        String email = jwtService.extractUsername(rawRefreshToken);
        var userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(rawRefreshToken, userDetails)) {
            throw new InvalidCredentialsException();
        }

        String newAccess  = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);

        User user = findByEmailOrThrow(email);
        AuthResponse response = userMapper.toAuthResponse(user);
        response.setAccessToken(newAccess);
        response.setRefreshToken(newRefresh);
        return response;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken");
        }

        User user = userMapper.toEntity(userRequest);

        String rawPassword = passwordGenerator.generate(8);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setForcePasswordChange(true);
        user.setActive(true);

        Set<Role> roles = userRequest.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InternalTechnicalException
                                ("Critical error: Role " + roleName + " not found")))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        User savedUser = userRepository.saveAndFlush(user);
        log.info("User created successfully. TEMP PASSWORD for {}: {}", user.getEmail(), rawPassword);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        log.info("Attempting to change password for user: {}", request.getEmail());

        User user = findByEmailOrThrow(request.getEmail());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password does not match for user {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed: New password is the same as the old one for user {}", request.getEmail());
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);

        userRepository.save(user);
        log.info("Password successfully changed for user: {}. ForcePasswordChange flag reset to false.", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        log.info("Deactivating user with ID: {}", id);

        User user = findUserOrThrow(id);

        userRepository.delete(user);

        log.info("User {} has been successfully deactivated", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.info("Getting user with ID: {}", id);

        User user = findUserOrThrow(id);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");

        List<User> users = userRepository.findAllWithRoles();

        return userMapper.toResponse(users);
    }

    @Override
    @Transactional
    public UserResponse updateUserById(UUID id, UserRequest userRequest) {
        log.info("Updating user with ID: {}", id);

        User user = findUserOrThrow(id);

        userRepository.findByEmail(userRequest.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new UserAlreadyExistsException("Email already taken by another user");
                    }
                });

        Set<Role> newRoles = userRequest.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InternalTechnicalException("Role " + roleName + " not found")))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);

        userMapper.updateUser(userRequest, user);

        User updatedUser = userRepository.save(user);
        log.info("User {} successfully updated", id);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        log.info("Registering user via mapper: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken");
        }

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setForcePasswordChange(false);

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new InternalTechnicalException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        log.info("Public user registered successfully with ID: {}", savedUser.getId());

        var userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        AuthResponse response = userMapper.toAuthResponse(savedUser);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setForcePasswordChange(savedUser.isForcePasswordChange());

        return response;
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");
                });
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email {} not found", email);
                    return new InvalidCredentialsException();
                });
    }
}
