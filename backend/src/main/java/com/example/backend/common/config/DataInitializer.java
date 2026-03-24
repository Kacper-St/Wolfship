package com.example.backend.common.config;

import com.example.backend.users.domain.model.Role;
import com.example.backend.users.domain.model.RoleName;
import com.example.backend.users.domain.model.User;
import com.example.backend.users.domain.repository.RoleRepository;
import com.example.backend.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void initAdminUser() {
        String adminEmail = "admin@wolfship.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists, skipping.");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow();

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFirstName("Admin");
        admin.setLastName("Wolfship");
        admin.setPesel("00000000000");
        admin.setPassword(passwordEncoder.encode("Admin1234!"));
        admin.setActive(true);
        admin.setForcePasswordChange(false);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        log.info("Admin user created: {} / Admin1234!", adminEmail);
    }
}