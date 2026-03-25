package com.example.backend.shipping.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "House number is required")
    private String houseNumber;

    private String apartmentNumber;

    @NotBlank
    @Size(min = 2, max = 2)
    private String country = "PL";

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Zip code is required")
    private String zipCode;
}