package com.example.backend.users.api;

import com.example.backend.users.api.dto.LoginRequest;
import com.example.backend.users.api.dto.PasswordChangeRequest;
import com.example.backend.users.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user with identifier: {}", request.getLoginIdentifier());
        userService.loginUser(request);
        log.info("Login successful for: {}", request.getLoginIdentifier());
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("REST request to change password for identifier: {}", request.getLoginIdentifier());
        userService.changePassword(request);
        log.info("Password successfully changed for user: {}", request.getLoginIdentifier());
        return ResponseEntity.ok("Password changed successfully. You can now log in with your new credentials.");
    }
}
