package com.example.backend.routing.application.graph;

import com.example.backend.routing.domain.model.HubConnection;
import com.example.backend.routing.domain.repository.HubConnectionRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubGraphBuilder {

    private final HubConnectionRepository hubConnectionRepository;

    @Getter
    private SimpleDirectedWeightedGraph<UUID, DefaultWeightedEdge> graph;

    @PostConstruct
    public void init() {
        buildGraph();
    }

    public void buildGraph() {
        log.info("Initializing Hub Routing Graph...");

        var newGraph = new SimpleDirectedWeightedGraph<UUID, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        List<HubConnection> connections = hubConnectionRepository.findAllWithHubs();

        for (var conn : connections) {
            UUID source = conn.getSourceHub().getId();
            UUID target = conn.getTargetHub().getId();

            newGraph.addVertex(source);
            newGraph.addVertex(target);

            DefaultWeightedEdge edge = newGraph.addEdge(source, target);
            if (edge != null) {
                newGraph.setEdgeWeight(edge, conn.getTravelTimeMinutes());
            }
        }

        this.graph = newGraph;
        log.info("Hub graph ready. Vertices: {}, Edges: {}", newGraph.vertexSet().size(), newGraph.edgeSet().size());
    }

}