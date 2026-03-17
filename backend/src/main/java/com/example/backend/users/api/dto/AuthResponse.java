package com.example.backend.users.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AuthResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
}
