package com.example.backend.users.application;

import com.example.backend.common.exception.InternalTechnicalException;
import com.example.backend.common.util.PasswordGenerator;
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

    @Override
    @Transactional(readOnly = true)
    public void loginUser(LoginRequest request) {
        log.info("Attempting authentication for identifier: {}", request.getLoginIdentifier());

        User user = findByIdentifierOrThrow(request.getLoginIdentifier());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed: Invalid password for identifier {}", request.getLoginIdentifier());
            throw new InvalidCredentialsException();
        }

        if (user.isForcePasswordChange()) {
            log.info("Authentication successful for user {}. Redirection to password change required.", user.getPesel());
        } else {
            log.info("User {} successfully authenticated.", user.getPesel());
        }
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user with email: {} and PESEL: {}", userRequest.getEmail(), userRequest.getPesel());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken");
        }
        if (userRepository.existsByPesel(userRequest.getPesel())) {
            throw new UserAlreadyExistsException("User with this PESEL already exists");
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
        log.info("User created successfully. TEMP PASSWORD for {}: {}", user.getPesel(), rawPassword);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        log.info("Attempting to change password for user: {}", request.getLoginIdentifier());

        User user = findByIdentifierOrThrow(request.getLoginIdentifier());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password does not match for user {}", request.getLoginIdentifier());
            throw new InvalidCredentialsException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed: New password is the same as the old one for user {}", request.getLoginIdentifier());
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);

        userRepository.save(user);
        log.info("Password successfully changed for user: {}. ForcePasswordChange flag reset to false.", user.getPesel());
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

        userRepository.findByPesel(userRequest.getPesel())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new UserAlreadyExistsException("PESEL already taken by another user");
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
        return userMapper.toAuthResponse(savedUser);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");
                });
    }

    private User findByIdentifierOrThrow(String identifier) {
        return userRepository.findByEmailOrPesel(identifier, identifier)
                .orElseThrow(() -> {
                    log.warn("User with identifier {} not found", identifier);
                    return new InvalidCredentialsException();
                });
    }
}
