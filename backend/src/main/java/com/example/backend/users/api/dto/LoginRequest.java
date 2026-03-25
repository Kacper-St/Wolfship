package com.example.backend.users.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank(message = "Login can not be blank")
    @Email
    private String email;

    @NotBlank(message = "Password can not be blank")
    private String password;
}
