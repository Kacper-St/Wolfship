package com.example.backend.users.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class AuthResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    private boolean forcePasswordChange;
}