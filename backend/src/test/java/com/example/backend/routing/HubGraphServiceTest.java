package com.example.backend.routing;

import com.example.backend.routing.application.graph.HubGraphBuilder;
import com.example.backend.routing.application.graph.HubGraphService;
import com.example.backend.routing.domain.exception.RouteCalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HubGraphService")
class HubGraphServiceTest {

    @Mock
    private HubGraphBuilder graphBuilder;

    private HubGraphService hubGraphService;

    private UUID hubA;
    private UUID hubB;
    private UUID hubC;
    private UUID isolatedHub;

    @BeforeEach
    void setUp() {
        hubGraphService = new HubGraphService(graphBuilder);

        hubA = UUID.randomUUID();
        hubB = UUID.randomUUID();
        hubC = UUID.randomUUID();
        isolatedHub = UUID.randomUUID();

        SimpleDirectedWeightedGraph<UUID, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex(hubA);
        graph.addVertex(hubB);
        graph.addVertex(hubC);
        graph.addVertex(isolatedHub);

        DefaultWeightedEdge ab = graph.addEdge(hubA, hubB);
        graph.setEdgeWeight(ab, 10);
        DefaultWeightedEdge bc = graph.addEdge(hubB, hubC);
        graph.setEdgeWeight(bc, 20);
        DefaultWeightedEdge ac = graph.addEdge(hubA, hubC);
        graph.setEdgeWeight(ac, 50);

        when(graphBuilder.getGraph()).thenReturn(graph);
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("should choose path through intermediate hub when faster than direct")
        void shouldChooseFasterIndirectPath() {
            List<UUID> path = hubGraphService.findShortestPath(hubA, hubC);

            // then
            assertThat(path).containsExactly(hubA, hubB, hubC);
        }

        @Test
        @DisplayName("should find direct single-step path")
        void shouldFindDirectPath() {
            // when
            List<UUID> path = hubGraphService.findShortestPath(hubA, hubB);

            // then
            assertThat(path).containsExactly(hubA, hubB);
        }

        @Test
        @DisplayName("should return single hub when source equals target")
        void shouldReturnSingleHubWhenSameHub() {
            // when
            List<UUID> path = hubGraphService.findShortestPath(hubA, hubA);

            // then
            assertThat(path).containsExactly(hubA);
        }
    }

    @Nested
    @DisplayName("error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw when source hub does not exist in graph")
        void shouldThrowWhenSourceNotInGraph() {
            // given
            UUID unknownHub = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> hubGraphService.findShortestPath(unknownHub, hubC))
                    .isInstanceOf(RouteCalculationException.class)
                    .hasMessageContaining("Hub not found");
        }

        @Test
        @DisplayName("should throw when target hub does not exist in graph")
        void shouldThrowWhenTargetNotInGraph() {
            // given
            UUID unknownHub = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> hubGraphService.findShortestPath(hubA, unknownHub))
                    .isInstanceOf(RouteCalculationException.class)
                    .hasMessageContaining("Hub not found");
        }

        @Test
        @DisplayName("should throw when no path exists between hubs")
        void shouldThrowWhenNoPathExists() {
            // when & then
            assertThatThrownBy(() -> hubGraphService.findShortestPath(hubA, isolatedHub))
                    .isInstanceOf(RouteCalculationException.class)
                    .hasMessageContaining("No connection available");
        }
    }
}