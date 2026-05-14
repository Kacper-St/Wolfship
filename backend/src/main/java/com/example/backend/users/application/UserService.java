package com.example.backend.users.application;

import com.example.backend.users.api.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    AuthResponse loginUser(LoginRequest request);

    UserResponse createUser(UserRequest userRequest);

    void changePassword(@Valid PasswordChangeRequest request);

    void deleteUserById(UUID id);

    UserResponse getUserById(UUID id);

    UserResponse updateUserById(UUID id, @Valid UserRequest userRequest);

    AuthResponse registerUser(@Valid RegisterRequest request);

    AuthResponse refreshToken(String rawRefreshToken);

    Page<UserResponse> getAllUsers(Pageable pageable);
}
