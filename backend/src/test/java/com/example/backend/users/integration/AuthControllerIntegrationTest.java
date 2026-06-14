package com.example.backend.users.integration;

import com.example.backend.integration.BaseIntegrationTest;
import com.example.backend.users.api.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthController — integration (HTTP + JWT)")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.com";
    }

    private Map<String, Object> registerPayload(String email) {
        return Map.of(
                "email", email,
                "password", "password123",
                "firstName", "Jan",
                "lastName", "Kowalski"
        );
    }

    private ResponseEntity<AuthResponse> register(String email) {
        return client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerPayload(email))
                .retrieve()
                .toEntity(AuthResponse.class);
    }

    @Test
    @DisplayName("should register new user and return tokens (201)")
    void shouldRegisterUser() {
        // given
        String email = uniqueEmail("register");

        // when
        ResponseEntity<AuthResponse> response = register(email);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(email);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("should login with valid credentials and return tokens (200)")
    void shouldLoginWithValidCredentials() {
        // given
        String email = uniqueEmail("login");
        register(email);

        // when
        ResponseEntity<AuthResponse> response = client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", "password123"))
                .retrieve()
                .toEntity(AuthResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(email);
        assertThat(response.getBody().getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("should reject login with wrong password")
    void shouldRejectWrongPassword() {
        // given
        String email = uniqueEmail("wrongpass");
        register(email);

        // when & then
        assertThatThrownBy(() -> client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", "WRONG_password"))
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("should refresh access token with valid refresh token (200)")
    void shouldRefreshToken() {
        // given
        String email = uniqueEmail("refresh");
        String refreshToken = register(email).getBody().getRefreshToken();

        // when
        ResponseEntity<AuthResponse> response = client.post()
                .uri("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("refreshToken", refreshToken))
                .retrieve()
                .toEntity(AuthResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
    }
}