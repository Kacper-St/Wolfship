package com.example.backend.shipping.integration;

import com.example.backend.integration.BaseIntegrationTest;
import com.example.backend.operations.domain.model.Courier;
import com.example.backend.operations.domain.model.CourierType;
import com.example.backend.operations.domain.model.Task;
import com.example.backend.operations.domain.model.TaskStatus;
import com.example.backend.operations.domain.repository.CourierRepository;
import com.example.backend.operations.domain.repository.TaskRepository;
import com.example.backend.routing.domain.model.ShipmentRoute;
import com.example.backend.routing.domain.repository.ShipmentRouteRepository;
import com.example.backend.routing.domain.repository.ZoneRepository;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.application.GeocodingService;
import com.example.backend.shipping.application.LabelService;
import com.example.backend.tracking.domain.repository.TrackingEventRepository;
import com.example.backend.users.api.dto.AuthResponse;
import com.example.backend.users.domain.model.Role;
import com.example.backend.users.domain.model.RoleName;
import com.example.backend.users.domain.model.User;
import com.example.backend.users.domain.repository.RoleRepository;
import com.example.backend.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Shipment full flow — integration")
class ShipmentFullFlowIntegrationTest extends BaseIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @MockitoBean private GeocodingService geocodingService;
    @MockitoBean private LabelService labelService;

    @Autowired private GeometryFactory geometryFactory;
    @Autowired private ShipmentRouteRepository shipmentRouteRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private CourierRepository courierRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TrackingEventRepository trackingEventRepository;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port);

        Point warsaw = geometryFactory.createPoint(new Coordinate(21.0122, 52.2297));
        Point krakow = geometryFactory.createPoint(new Coordinate(19.9366, 50.0614));

        when(geocodingService.geocode(any(), any(), eq("Warszawa"), any(), any())).thenReturn(warsaw);
        when(geocodingService.geocode(any(), any(), eq("Kraków"), any(), any())).thenReturn(krakow);
        when(labelService.generateAndUploadLabel(any())).thenReturn("http://test/label.pdf");
    }

    private String registerSenderAndGetToken() {
        String email = "sender-" + UUID.randomUUID() + "@test.com";
        return registerAndLogin(email);
    }

    private String registerAndLogin(String email) {
        AuthResponse auth = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", "password123",
                        "firstName", "Jan", "lastName", "Kowalski"))
                .retrieve()
                .body(AuthResponse.class);
        return auth.getAccessToken();
    }

    private String loginAndGetToken(String email, String password) {
        AuthResponse auth = client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", password))
                .retrieve()
                .body(AuthResponse.class);
        return auth.getAccessToken();
    }

    private Map<String, Object> address(String city, String email) {
        return Map.of(
                "fullName", "Jan Kowalski",
                "email", email,
                "phoneNumber", "123456789",
                "street", "Testowa",
                "houseNumber", "1",
                "country", "PL",
                "city", city,
                "zipCode", "00-001");
    }

    private ShipmentResponse createShipment(String senderToken) {
        return client.post()
                .uri("/api/v1/shipments")
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "senderAddress", address("Warszawa", "sender@test.com"),
                        "receiverAddress", address("Kraków", "receiver@test.com"),
                        "size", "M"))
                .retrieve()
                .body(ShipmentResponse.class);
    }

    private String seedZoneCourier(UUID zoneId) {
        Role courierRole = roleRepository.findByName(RoleName.ROLE_COURIER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_COURIER)));

        String email = "courier-" + UUID.randomUUID() + "@test.com";
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Kurier");
        user.setLastName("Testowy");
        user.setPassword(passwordEncoder.encode("password"));
        user.setActive(true);
        user.setForcePasswordChange(false);
        user.setRoles(Set.of(courierRole));
        User savedUser = userRepository.save(user);

        Courier courier = Courier.builder()
                .userId(savedUser.getId())
                .courierType(CourierType.ZONE_COURIER)
                .zoneId(zoneId)
                .active(true)
                .build();
        courierRepository.save(courier);

        return email;
    }

    @Test
    @DisplayName("creating a shipment triggers routing and task creation")
    void shouldTriggerFullEventChain() {
        String token = registerSenderAndGetToken();

        ResponseEntity<ShipmentResponse> response = client.post()
                .uri("/api/v1/shipments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "senderAddress", address("Warszawa", "sender@test.com"),
                        "receiverAddress", address("Kraków", "receiver@test.com"),
                        "size", "M"))
                .retrieve()
                .toEntity(ShipmentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID shipmentId = response.getBody().getId();
        assertThat(shipmentId).isNotNull();

        assertThat(response.getBody().getPrice()).isEqualByComparingTo("20.00");
        assertThat(response.getBody().getCurrency()).isEqualTo("PLN");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ShipmentRoute route = shipmentRouteRepository.findByShipmentId(shipmentId).orElse(null);
            assertThat(route).isNotNull();
        });

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(taskRepository.findByShipmentIdAndSequenceOrder(shipmentId, 1)).isPresent());
    }

    @Test
    @DisplayName("courier scans pickup task → task completed and tracking event created")
    void shouldCompletePickupScanAndCreateTrackingEvent() {
        // given
        UUID sourceZoneId = zoneRepository.findZoneByCoordinates(21.0122, 52.2297)
                .orElseThrow().getId();
        String courierEmail = seedZoneCourier(sourceZoneId);

        // given
        String senderToken = registerSenderAndGetToken();
        ShipmentResponse shipment = createShipment(senderToken);
        UUID shipmentId = shipment.getId();
        String trackingNumber = shipment.getTrackingNumber();

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(taskRepository.findByShipmentIdAndSequenceOrder(shipmentId, 1)).isPresent());

        // when
        String courierToken = loginAndGetToken(courierEmail, "password");

        ResponseEntity<Void> scanResponse = client.post()
                .uri("/api/v1/operations/scan")
                .header("Authorization", "Bearer " + courierToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("shipmentId", shipmentId, "trackingNumber", trackingNumber))
                .retrieve()
                .toBodilessEntity();

        // then
        assertThat(scanResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // then
        Task pickupTask = taskRepository.findByShipmentIdAndSequenceOrder(shipmentId, 1).orElseThrow();
        assertThat(pickupTask.getTaskStatus()).isEqualTo(TaskStatus.COMPLETED);

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(trackingEventRepository
                        .findAllByTrackingNumberOrderByCreatedAtDesc(trackingNumber))
                        .isNotEmpty());
    }
}