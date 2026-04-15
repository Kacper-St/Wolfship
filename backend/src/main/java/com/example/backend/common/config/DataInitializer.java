package com.example.backend.common.config;

import com.example.backend.operations.domain.model.Courier;
import com.example.backend.operations.domain.model.CourierType;
import com.example.backend.operations.domain.repository.CourierRepository;
import com.example.backend.routing.domain.model.Zone;
import com.example.backend.routing.domain.repository.HubConnectionRepository;
import com.example.backend.routing.domain.repository.ZoneRepository;
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

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ZoneRepository zoneRepository;
    private final HubConnectionRepository hubConnectionRepository;
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initRoles();
        initAdminUser();
        initTestCouriers();
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
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setActive(true);
        admin.setForcePasswordChange(false);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        log.info("Admin user created: {} / admin", adminEmail);
    }

    private void initTestCouriers() {
        if (courierRepository.existsByCourierType(CourierType.ZONE_COURIER)) {
            log.info("Test couriers already exist, skipping.");
            return;
        }
        Role courierRole = roleRepository.findByName(RoleName.ROLE_COURIER)
                .orElseThrow();

        List<Zone> zones = zoneRepository.findAll();
        for (Zone zone : zones) {
            String email = "courier." + zone.getTerytCode() + "@wolfship.com";

            if (userRepository.existsByEmail(email)) {
                continue;
            }

            User user = new User();
            user.setEmail(email);
            user.setFirstName("Kurier");
            user.setLastName(zone.getName());
            user.setPassword(passwordEncoder.encode("password"));
            user.setActive(true);
            user.setForcePasswordChange(false);
            user.setRoles(Set.of(courierRole));
            User savedUser = userRepository.save(user);

            Courier courier = Courier.builder()
                    .userId(savedUser.getId())
                    .courierType(CourierType.ZONE_COURIER)
                    .zoneId(zone.getId())
                    .active(true)
                    .build();
            courierRepository.save(courier);
        }
        log.info("Zone couriers initialized: {}", zones.size());

        hubConnectionRepository.findAllWithHubs().forEach(connection -> {
            String email = "linehaul." + connection.getSourceHub().getCode() + "." + connection.getTargetHub().getCode()
                    + "@wolfship.com";

            if (userRepository.existsByEmail(email)) {
                if (courierRepository
                        .findBySourceHubIdAndTargetHubIdAndActiveTrue(
                                connection.getSourceHub().getId(),
                                connection.getTargetHub().getId())
                        .isEmpty()) {

                    User existing = userRepository.findByEmail(email)
                            .orElseThrow();

                    Courier courier = Courier.builder()
                            .userId(existing.getId())
                            .courierType(CourierType.LINE_HAUL_COURIER)
                            .sourceHubId(connection.getSourceHub().getId())
                            .targetHubId(connection.getTargetHub().getId())
                            .active(true)
                            .build();
                    courierRepository.save(courier);
                }
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setFirstName("Przewoźnik");
            user.setLastName(connection.getSourceHub().getCode() + "-" + connection.getTargetHub().getCode());
            user.setPassword(passwordEncoder.encode("password"));
            user.setActive(true);
            user.setForcePasswordChange(false);
            user.setRoles(Set.of(courierRole));
            User savedUser = userRepository.save(user);

            Courier courier = Courier.builder()
                    .userId(savedUser.getId())
                    .courierType(CourierType.LINE_HAUL_COURIER)
                    .sourceHubId(connection.getSourceHub().getId())
                    .targetHubId(connection.getTargetHub().getId())
                    .active(true)
                    .build();
            courierRepository.save(courier);
        });

        log.info("Line-haul couriers initialized");
    }
}