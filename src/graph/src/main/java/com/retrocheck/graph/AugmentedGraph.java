package com.retrocheck.graph;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AugmentedGraph {
    private List<Edge<?, ?>> edges;
    private Map<UUID, Node<?>> nodesById;
    private List<UUID> sourceIds;
    private Map<UUID, Integer> levelsByNodeId;

    public AugmentedGraph(Map<UUID, Node<?>> nodesById, List<UUID> sourceIds, Map<UUID, Integer> levelsByNodeId, List<Edge<?, ?>> edges) {
        this.nodesById = nodesById;
        this.sourceIds = sourceIds;
        this.edges = edges;
        this.levelsByNodeId = levelsByNodeId;
    }

    public List<Edge<?, ?>> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge<?, ?>> edges) {
        this.edges = edges;
    }

    public Map<UUID, Node<?>> getNodesById() {
        return nodesById;
    }

    public void setNodesById(Map<UUID, Node<?>> nodesById) {
        this.nodesById = nodesById;
    }

    public List<UUID> getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(List<UUID> sourceIds) {
        this.sourceIds = sourceIds;
    }

    public Map<UUID, Integer> getLevelsByNodeId() {
        return levelsByNodeId;
    }

    public void setLevelsByNodeId(Map<UUID, Integer> levelsByNodeId) {
        this.levelsByNodeId = levelsByNodeId;
    }

    public AugmentedGraph copy() {
        Map<UUID, Node<?>> nodesByIdCopy = nodesById.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (Node<?>)entry.getValue().copy()));
        List<Edge<?, ?>> edgesCopy = edges.stream().map(edge -> (Edge<?, ?>)edge.copy((Map<UUID, Node<?>>)nodesByIdCopy)).collect(Collectors.toList());
        List<UUID> sourceIdsCopy = new ArrayList<>(sourceIds);
        Map<UUID, Integer> levelsByNodeIdCopy = new HashMap<>(levelsByNodeId);
        return new AugmentedGraph(nodesByIdCopy, sourceIdsCopy, levelsByNodeIdCopy, edgesCopy);
    }

    // topological sort -- in order to find cycles, which would invalidate the input graph.
//    private boolean findCycles(Map<UUID, List<E>> graph, Map<UUID, N> nodesById) {
//        Map<UUID, Mark> nodesMarked = nodesById.keySet().stream().collect(Collectors.toMap(Functions.identity(), edges -> Mark.None));
//        while (nodesMarked.values().contains(Mark.None)) {
//
//        }
//
//    }

//    private enum Mark {
//        Permanent,
//        Temporary,
//        None
//    }
}
