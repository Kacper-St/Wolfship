package com.example.backend.users.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank(message = "Login can not be blank")
    private String loginIdentifier;

    @NotBlank(message = "Password can not be blank")
    private String password;
}
