package com.example.backend.users.api;

import com.example.backend.common.api.ApiResponse;
import com.example.backend.users.api.dto.*;
import com.example.backend.users.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user with identifier: {}", request.getLoginIdentifier());
        userService.loginUser(request);
        log.info("Login successful for: {}", request.getLoginIdentifier());
        return ResponseEntity.ok(
                ApiResponse.success(null, "LOGIN_SUCCESSFUL")
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for identifier: {}", request.getLoginIdentifier());
        userService.changePassword(request);
        log.info("Password successfully changed for user: {}", request.getLoginIdentifier());
        return ResponseEntity.ok(
                ApiResponse.success(null, "PASSWORD_CHANGED_SUCCESSFULLY")
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register: {}", request.getEmail());

        AuthResponse authResponse = userService.registerUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(authResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(authResponse);
    }
}
