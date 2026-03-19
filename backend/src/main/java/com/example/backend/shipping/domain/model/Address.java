package com.example.backend.shipping.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    @Email
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(length = 20,  nullable = false)
    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "Street is required")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "House number is required")
    @Column(nullable = false, length = 20)
    private String houseNumber;

    @Column(length = 20)
    private String apartmentNumber;

    @NotBlank
    @Column(nullable = false, length = 2)
    @Size(min = 2, max = 2)
    @Builder.Default
    private String country = "PL";

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Zip code is required")
    @Column(nullable = false, length = 10)
    private String zipCode;
}
