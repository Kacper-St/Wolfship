package com.example.backend.routing.integration;

import com.example.backend.integration.BaseIntegrationTest;
import com.example.backend.routing.application.graph.HubGraphBuilder;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HubGraphBuilder — integration with real DB")
class HubGraphBuilderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private HubGraphBuilder hubGraphBuilder;

    @Test
    @DisplayName("should build graph with all 10 hubs as vertices")
    void shouldBuildGraphWithAllHubs() {
        var graph = hubGraphBuilder.getGraph();

        assertThat(graph.vertexSet()).hasSize(10);
    }

    @Test
    @DisplayName("should build all hub connections as directed edges")
    void shouldBuildAllConnections() {
        var graph = hubGraphBuilder.getGraph();

        assertThat(graph.edgeSet()).hasSize(34);
    }

    @Test
    @DisplayName("should assign positive travel time weight to every edge")
    void shouldHavePositiveWeights() {
        SimpleDirectedWeightedGraph<UUID, DefaultWeightedEdge> graph =
                hubGraphBuilder.getGraph();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            assertThat(graph.getEdgeWeight(edge))
                    .as("travel time for edge %s", edge)
                    .isGreaterThan(0.0);
        }
    }

    @Test
    @DisplayName("should not contain isolated hubs")
    void shouldNotHaveIsolatedHubs() {
        var graph = hubGraphBuilder.getGraph();

        for (UUID hub : graph.vertexSet()) {
            int connections = graph.outgoingEdgesOf(hub).size()
                    + graph.incomingEdgesOf(hub).size();
            assertThat(connections)
                    .as("hub %s should have at least one connection", hub)
                    .isGreaterThan(0);
        }
    }
}