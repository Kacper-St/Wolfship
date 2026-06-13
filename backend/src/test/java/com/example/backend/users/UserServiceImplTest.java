package com.example.backend.users;

import com.example.backend.common.util.PasswordGenerator;
import com.example.backend.security.jwt.JwtService;
import com.example.backend.users.api.dto.*;
import com.example.backend.users.api.mapper.UserMapper;
import com.example.backend.users.application.UserServiceImpl;
import com.example.backend.users.application.event.UserCreatedEvent;
import com.example.backend.users.domain.exception.InvalidCredentialsException;
import com.example.backend.users.domain.exception.SamePasswordException;
import com.example.backend.users.domain.exception.UserAlreadyExistsException;
import com.example.backend.users.domain.exception.UserNotFoundException;
import com.example.backend.users.domain.model.Role;
import com.example.backend.users.domain.model.RoleName;
import com.example.backend.users.domain.model.User;
import com.example.backend.users.domain.repository.RoleRepository;
import com.example.backend.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;
    private Role courierRole;
    private final UUID userId = UUID.randomUUID();
    private final String email = "test@wolfship.com";
    private final String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, roleRepository, passwordEncoder,
                userMapper, passwordGenerator, jwtService,
                userDetailsService, eventPublisher
        );

        userRole = new Role(RoleName.ROLE_USER);
        courierRole = new Role(RoleName.ROLE_COURIER);

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        testUser.setPassword(passwordEncoder.encode(rawPassword));
        testUser.setFirstName("Jan");
        testUser.setLastName("Kowalski");
        testUser.setActive(true);
        testUser.setForcePasswordChange(false);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private PasswordChangeRequest passwordChangeRequest(String email, String current, String newPass) {
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setEmail(email);
        req.setCurrentPassword(current);
        req.setNewPassword(newPass);
        return req;
    }

    private UserRequest userRequest(String email, Set<RoleName> roles) {
        UserRequest req = new UserRequest();
        req.setEmail(email);
        req.setFirstName("Jan");
        req.setLastName("Kowalski");
        req.setRoles(roles);
        return req;
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword(password);
        req.setFirstName("Jan");
        req.setLastName("Kowalski");
        return req;
    }

    @Nested
    @DisplayName("loginUser")
    class LoginUserTests {

        @Test
        @DisplayName("should authenticate and return tokens with correct user data")
        void shouldAuthenticateWithValidCredentials() {
            // given
            LoginRequest request = loginRequest(email, rawPassword);
            UserDetails userDetails = mock(UserDetails.class);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

            // when
            AuthResponse result = userService.loginUser(request);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getFirstName()).isEqualTo("Jan");
            assertThat(result.getRoles()).contains("ROLE_USER");
        }

        @Test
        @DisplayName("should throw when password does not match")
        void shouldThrowWhenPasswordIsWrong() {
            // given
            LoginRequest request = loginRequest(email, "wrongPassword");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.loginUser(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(jwtService, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenUserNotFound() {
            // given
            LoginRequest request = loginRequest("ghost@wolfship.com", "password");

            when(userRepository.findByEmail("ghost@wolfship.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.loginUser(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("should set forcePasswordChange flag in response")
        void shouldReturnForcePasswordChangeFlag() {
            // given
            testUser.setForcePasswordChange(true);
            LoginRequest request = loginRequest(email, rawPassword);
            UserDetails userDetails = mock(UserDetails.class);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails)).thenReturn("access");
            when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh");

            // when
            AuthResponse result = userService.loginUser(request);

            // then
            assertThat(result.isForcePasswordChange()).isTrue();
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("should create user with encoded password and publish event")
        void shouldCreateUserAndPublishEvent() {
            // given
            String targetEmail = "new@wolfship.com";
            UserRequest request = userRequest(targetEmail, Set.of(RoleName.ROLE_COURIER));

            when(userRepository.existsByEmail(targetEmail)).thenReturn(false);
            when(passwordGenerator.generate(8)).thenReturn("tempPass1");
            when(roleRepository.findByName(RoleName.ROLE_COURIER))
                    .thenReturn(Optional.of(courierRole));
            when(userRepository.saveAndFlush(any(User.class)))
                    .thenAnswer(invocation -> {
                        User u = invocation.getArgument(0);
                        u.setId(UUID.randomUUID());
                        return u;
                    });

            // when
            UserResponse result = userService.createUser(request);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo(targetEmail);
            assertThat(savedUser.getFirstName()).isEqualTo("Jan");
            assertThat(savedUser.isForcePasswordChange()).isTrue();
            assertThat(savedUser.isActive()).isTrue();
            assertThat(passwordEncoder.matches("tempPass1", savedUser.getPassword())).isTrue();
            assertThat(savedUser.getRoles()).contains(courierRole);

            // then
            ArgumentCaptor<UserCreatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(UserCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            UserCreatedEvent event = eventCaptor.getValue();
            assertThat(event.email()).isEqualTo(targetEmail);
            assertThat(event.tempPassword()).isEqualTo("tempPass1");

            // then
            assertThat(result.getEmail()).isEqualTo(targetEmail);
            assertThat(result.getFirstName()).isEqualTo("Jan");
        }

        @Test
        @DisplayName("should reject duplicate email without saving")
        void shouldThrowWhenEmailAlreadyExists() {
            // given
            UserRequest request = userRequest(email, Set.of(RoleName.ROLE_USER));

            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessage("Email already taken");

            verify(userRepository, never()).saveAndFlush(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("should encode new password and reset forcePasswordChange")
        void shouldChangePasswordSuccessfully() {
            // given
            testUser.setForcePasswordChange(true);
            PasswordChangeRequest request = passwordChangeRequest(email, rawPassword, "newSecurePass");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            // when
            userService.changePassword(request);

            // then
            assertThat(passwordEncoder.matches("newSecurePass", testUser.getPassword())).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, testUser.getPassword())).isFalse();
            assertThat(testUser.isForcePasswordChange()).isFalse();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should reject when current password is wrong")
        void shouldThrowWhenCurrentPasswordWrong() {
            // given
            PasswordChangeRequest request = passwordChangeRequest(email, "wrongCurrent", "newPass");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject when new password equals current")
        void shouldThrowWhenSamePassword() {
            // given
            PasswordChangeRequest request = passwordChangeRequest(email, rawPassword, rawPassword);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(SamePasswordException.class);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return mapped user response")
        void shouldReturnUserWhenFound() {
            // given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            UserResponse result = userService.getUserById(userId);

            // then
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getFirstName()).isEqualTo("Jan");
            assertThat(result.getLastName()).isEqualTo("Kowalski");
            assertThat(result.getRoles()).contains("ROLE_USER");
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(unknownId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteUserById")
    class DeleteUserTests {

        @Test
        @DisplayName("should call delete on repository")
        void shouldDeleteWhenFound() {
            // given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            userService.deleteUserById(userId);

            // then
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteUserById(unknownId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateUserById")
    class UpdateUserTests {

        @Test
        @DisplayName("should update fields and roles correctly")
        void shouldUpdateSuccessfully() {
            // given
            UserRequest request = userRequest(email, Set.of(RoleName.ROLE_COURIER));
            request.setFirstName("Piotr");
            request.setLastName("Nowak");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ROLE_COURIER))
                    .thenReturn(Optional.of(courierRole));
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UserResponse result = userService.updateUserById(userId, request);

            // then
            assertThat(result.getFirstName()).isEqualTo("Piotr");
            assertThat(result.getLastName()).isEqualTo("Nowak");
            assertThat(result.getRoles()).contains("ROLE_COURIER");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should reject when email belongs to another user")
        void shouldThrowWhenEmailTakenByAnother() {
            // given
            UserRequest request = userRequest("taken@wolfship.com", Set.of(RoleName.ROLE_USER));

            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            anotherUser.setEmail("taken@wolfship.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("taken@wolfship.com"))
                    .thenReturn(Optional.of(anotherUser));

            // when & then
            assertThatThrownBy(() -> userService.updateUserById(userId, request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessage("Email already taken by another user");
        }

        @Test
        @DisplayName("should allow keeping same email for same user")
        void shouldAllowSameEmailForSameUser() {
            // given
            UserRequest request = userRequest(email, Set.of(RoleName.ROLE_USER));

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ROLE_USER))
                    .thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when & then
            UserResponse result = userService.updateUserById(userId, request);

            assertThat(result.getEmail()).isEqualTo(email);
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUserTests {

        @Test
        @DisplayName("should register with encoded password and return tokens")
        void shouldRegisterAndReturnTokens() {
            // given
            RegisterRequest request = registerRequest("new@wolfship.com", "securePass123");
            UserDetails userDetails = mock(UserDetails.class);

            when(userRepository.existsByEmail("new@wolfship.com")).thenReturn(false);
            when(roleRepository.findByName(RoleName.ROLE_USER))
                    .thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User u = invocation.getArgument(0);
                        u.setId(UUID.randomUUID());
                        return u;
                    });
            when(userDetailsService.loadUserByUsername("new@wolfship.com"))
                    .thenReturn(userDetails);
            when(jwtService.generateAccessToken(userDetails)).thenReturn("access");
            when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh");

            // when
            AuthResponse result = userService.registerUser(request);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access");
            assertThat(result.getRefreshToken()).isEqualTo("refresh");
            assertThat(result.isForcePasswordChange()).isFalse();

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("new@wolfship.com");
            assertThat(savedUser.isActive()).isTrue();
            assertThat(savedUser.isForcePasswordChange()).isFalse();
            assertThat(passwordEncoder.matches("securePass123", savedUser.getPassword())).isTrue();
            assertThat(savedUser.getRoles()).contains(userRole);

            // then
            assertThat(result.getEmail()).isEqualTo("new@wolfship.com");
            assertThat(result.getFirstName()).isEqualTo("Jan");
        }

        @Test
        @DisplayName("should reject duplicate email without saving")
        void shouldThrowWhenEmailAlreadyRegistered() {
            // given
            RegisterRequest request = registerRequest(email, "password");

            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }
    }
}