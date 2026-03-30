package com.example.backend.routing.application.graph;

import com.example.backend.routing.domain.exception.RouteCalculationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubGraphService {

    private final HubGraphBuilder graphBuilder;

    public List<UUID> findShortestPath(UUID sourceId, UUID targetId) {
        log.info("Calculating shortest path from hub {} to hub {}", sourceId, targetId);

        var graph = graphBuilder.getGraph();

        if (sourceId.equals(targetId)) {
            log.info("Source and target hub are the same: {}", sourceId);
            return List.of(sourceId);
        }

        if (!graph.containsVertex(sourceId) || !graph.containsVertex(targetId)) {
            log.warn("Route requested for non-existent hub: {} -> {}", sourceId, targetId);
            throw new RouteCalculationException("Hub not found in routing system");
        }

        DijkstraShortestPath<UUID, DefaultWeightedEdge> dijkstra =
                new DijkstraShortestPath<>(graph);

        var path = dijkstra.getPath(sourceId, targetId);

        if (path == null) {
            log.error("No path found from hub {} to hub {}", sourceId, targetId);
            throw new RouteCalculationException("No connection available between selected hubs");
        }

        List<UUID> hubSequence = path.getVertexList();
        log.info("Shortest path found: {} hubs, total time: {} minutes", hubSequence.size(), (int) path.getWeight());

        return hubSequence;
    }
}