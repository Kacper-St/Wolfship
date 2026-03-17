package com.example.backend.users.application;

import com.example.backend.users.api.dto.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface UserService {

    void loginUser(LoginRequest request);

    UserResponse createUser(UserRequest userRequest);

    void changePassword(@Valid PasswordChangeRequest request);

    void deleteUserById(UUID id);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    UserResponse updateUserById(UUID id, @Valid UserRequest userRequest);

    AuthResponse registerUser(@Valid RegisterRequest request);
}
