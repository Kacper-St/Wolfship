package com.example.backend.routing;

import com.example.backend.routing.api.dto.RouteResponse;
import com.example.backend.routing.api.mapper.RoutingMapper;
import com.example.backend.routing.application.RoutingServiceImpl;
import com.example.backend.routing.application.ZoneResolverService;
import com.example.backend.routing.application.event.RouteCalculatedEvent;
import com.example.backend.routing.application.graph.HubGraphService;
import com.example.backend.routing.domain.exception.RouteNotFoundException;
import com.example.backend.routing.domain.model.Hub;
import com.example.backend.routing.domain.model.ShipmentRoute;
import com.example.backend.routing.domain.model.Zone;
import com.example.backend.routing.domain.repository.ShipmentRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoutingServiceImpl")
class RoutingServiceImplTest {

    @Mock private ZoneResolverService zoneResolverService;
    @Mock private HubGraphService hubGraphService;
    @Mock private ShipmentRouteRepository shipmentRouteRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private RoutingMapper routingMapper;

    private RoutingServiceImpl routingService;

    private final UUID shipmentId = UUID.randomUUID();
    private final UUID sourceHubId = UUID.randomUUID();
    private final UUID targetHubId = UUID.randomUUID();
    private final UUID sourceZoneId = UUID.randomUUID();
    private final UUID targetZoneId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        routingService = new RoutingServiceImpl(
                zoneResolverService, hubGraphService,
                shipmentRouteRepository, eventPublisher, routingMapper);
    }

    @Nested
    @DisplayName("calculateAndSaveRoute")
    class CalculateAndSaveRoute {

        @Test
        @DisplayName("should resolve zones, find path, save route and publish event")
        void shouldCalculateAndPublish() {
            // given
            Hub sourceHub = mock(Hub.class);
            Hub targetHub = mock(Hub.class);
            when(sourceHub.getId()).thenReturn(sourceHubId);
            when(targetHub.getId()).thenReturn(targetHubId);
            when(sourceHub.getName()).thenReturn("WAW");
            when(targetHub.getName()).thenReturn("KRK");

            Zone sourceZone = mock(Zone.class);
            Zone targetZone = mock(Zone.class);
            when(sourceZone.getHub()).thenReturn(sourceHub);
            when(targetZone.getHub()).thenReturn(targetHub);
            when(sourceZone.getId()).thenReturn(sourceZoneId);
            when(targetZone.getId()).thenReturn(targetZoneId);
            when(sourceZone.getName()).thenReturn("powiat Warszawa");
            when(targetZone.getName()).thenReturn("powiat Kraków");

            List<UUID> hubSequence = List.of(sourceHubId, UUID.randomUUID(), targetHubId);

            when(zoneResolverService.resolveZone(52.21, 21.02)).thenReturn(sourceZone);
            when(zoneResolverService.resolveZone(50.06, 19.94)).thenReturn(targetZone);
            when(hubGraphService.findShortestPath(sourceHubId, targetHubId))
                    .thenReturn(hubSequence);
            when(shipmentRouteRepository.save(any(ShipmentRoute.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ShipmentRoute result = routingService.calculateAndSaveRoute(
                    shipmentId, "WLF-123", "receiver@test.com",
                    52.21, 21.02, 50.06, 19.94);

            // then
            assertThat(result.getShipmentId()).isEqualTo(shipmentId);
            assertThat(result.getHubSequence()).isEqualTo(hubSequence);

            // then
            verify(zoneResolverService).resolveZone(52.21, 21.02);
            verify(zoneResolverService).resolveZone(50.06, 19.94);

            // then
            verify(hubGraphService).findShortestPath(sourceHubId, targetHubId);

            // then
            ArgumentCaptor<RouteCalculatedEvent> captor =
                    ArgumentCaptor.forClass(RouteCalculatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            RouteCalculatedEvent event = captor.getValue();
            assertThat(event.shipmentId()).isEqualTo(shipmentId);
            assertThat(event.trackingNumber()).isEqualTo("WLF-123");
            assertThat(event.sourceZoneId()).isEqualTo(sourceZoneId);
            assertThat(event.targetZoneId()).isEqualTo(targetZoneId);
            assertThat(event.sourceHubId()).isEqualTo(sourceHubId);
            assertThat(event.targetHubId()).isEqualTo(targetHubId);
            assertThat(event.hubSequence()).isEqualTo(hubSequence);
            assertThat(event.receiverEmail()).isEqualTo("receiver@test.com");
        }
    }

    @Nested
    @DisplayName("getRouteByShipmentId")
    class GetRoute {

        @Test
        @DisplayName("should return mapped route when found")
        void shouldReturnRouteWhenFound() {
            // given
            ShipmentRoute route = mock(ShipmentRoute.class);
            RouteResponse response = mock(RouteResponse.class);
            when(shipmentRouteRepository.findByShipmentId(shipmentId))
                    .thenReturn(Optional.of(route));
            when(routingMapper.toRouteResponse(route)).thenReturn(response);

            // when
            RouteResponse result = routingService.getRouteByShipmentId(shipmentId);

            // then
            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("should throw when route not found")
        void shouldThrowWhenNotFound() {
            // given
            when(shipmentRouteRepository.findByShipmentId(shipmentId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> routingService.getRouteByShipmentId(shipmentId))
                    .isInstanceOf(RouteNotFoundException.class);
        }
    }
}