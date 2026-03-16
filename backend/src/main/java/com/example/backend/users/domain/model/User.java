package com.example.backend.users.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE users SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false, length = 11)
    @Size(min = 11, max = 11, message = "PESEL must be 11 characters long")
    @Pattern(regexp = "\\d{11}", message = "PESEL must contain only digits")
    private String pesel;

    @Column(nullable = false)
    private boolean forcePasswordChange = true;

    private String password;

    private String firstName;
    private String lastName;

    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public boolean hasRole(RoleName roleName) {
        if (roles == null || roleName == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.getName() == roleName);
    }
}
