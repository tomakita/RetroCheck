package com.retrocheck.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Preprocessor {
    public AugmentedGraph process(List<Node<?>> nodes, List<Edge<?, ?>> edges) {
        Map<UUID, Node<?>> nodesById = makeNodesById(nodes);
        List<UUID> sourceIds = findSources(nodesById, edges);

        Map<UUID, List<Edge<?, ?>>> graph = makeGraph(nodes, edges);
        Map<UUID, Integer> levelsByNodeId = findLevelsByNodeId(graph, nodesById, sourceIds);
        List<Edge<?, ?>> terminatedEdges = withTerminalEdges(withTerminalLeafEdges(nodesById, edges), nodesById);

        return new AugmentedGraph(nodesById, sourceIds, levelsByNodeId, terminatedEdges);
    }

    public Map<UUID, List<Edge<?, ?>>> makeGraph(List<Node<?>> nodes, List<Edge<?, ?>> edges) {
        Map<UUID, List<Edge<?, ?>>> graph = edges.stream().collect(Collectors.groupingBy(edge -> edge.identify().getFirst()));

        // Find nodes which are on neither endpoint of any edge, and add them as keys to the graph with empty list values
        List<Node<?>> disconnectedNodes = GraphChoice.computeDisconnectedNodes(nodes, edges);
        for (Node<?> node : disconnectedNodes) {
            graph.put(node.identify(), new ArrayList<>());
        }

        return graph;
    }

    private Map<UUID, Node<?>> makeNodesById(List<Node<?>> nodes) {
        Map<UUID, Node<?>> map = new HashMap<>();
        for (Node<?> node : nodes) {
            map.put(node.identify(), node);
        }

        return map;

        // fails on mac with IncompatibleClassChangeError if i use this approach,
        // but runs fine on windows:
        //return nodes.stream().collect(Collectors.toMap(Identifiable::identify, Functions.identity()));
    }

    private Map<UUID, Integer> findLevelsByNodeId(Map<UUID, List<Edge<?, ?>>> graph, Map<UUID, Node<?>> nodesById, List<UUID> sourceIds) {
        Map<UUID, Integer> levelsByNodeId = new HashMap<>();
        for (UUID sourceId : sourceIds) {
            levelDfs(sourceId, graph, nodesById, 0, levelsByNodeId);
        }

        return levelsByNodeId;
    }

    private void levelDfs(UUID nodeId, Map<UUID, List<Edge<?, ?>>> graph, Map<UUID, Node<?>> nodesById, int level, Map<UUID, Integer> levelsByNodeId) {
        Node<?> u = nodesById.get(nodeId);
        if (u.toString().equals("_")) {
            return;
        }

        int deepestLevel = levelsByNodeId.getOrDefault(nodeId, level);
        int newDeepestLevel = Math.max(level, deepestLevel);
        levelsByNodeId.put(nodeId, newDeepestLevel);

        List<Edge<?, ?>> outedges = graph.get(nodeId);
        if (outedges == null) {
            return;
        }

        for (Edge<?, ?> edge : outedges) {
            levelDfs(edge.identify().getSecond(), graph, nodesById, level + 1, levelsByNodeId);
        }
    }

    private List<Edge<?, ?>> withTerminalEdges(List<Edge<?, ?>> edges, Map<UUID, Node<?>> nodesById) {
        Map<Optional<String>, List<Edge<?, ?>>> multiedges = edges.stream().filter(edge -> edge.isMultiedge()).collect(Collectors.groupingBy(Edge::containingSetId));
        Map<Edge<?, ?>, Integer> firstMultiedgeInSetToCumulativeProbability = multiedges.values().stream().collect(Collectors.toMap(edges_ -> edges_.get(0), edges_ -> edges_.stream().mapToInt(Edge::probability).sum()));
        List<Edge<?, ?>> terminalMultiEdges = firstMultiedgeInSetToCumulativeProbability.entrySet().stream().filter(entry -> entry.getValue() < 100).map(entry -> (Edge<?, ?>)entry.getKey().terminate(100 - entry.getValue())).collect(Collectors.toList());

        // we use !edge.containingSetId().isPresent() instead of !edge.isMultiedge() because
        // edges to NULL nodes are not multiedges, but do have a containingSetId, and we want to
        // exclude them here.
        List<Edge<?, ?>> terminalSingleEdges = edges.stream().filter(edge -> !edge.containingSetId().isPresent()).map(edge -> (Edge<?, ?>)edge.terminate(100 - edge.probability())).collect(Collectors.toList());

        List<Edge<?, ?>> edgesWithTerminalEdges = new ArrayList<>(edges);
        edgesWithTerminalEdges.addAll(terminalSingleEdges);
        edgesWithTerminalEdges.addAll(terminalMultiEdges);

        for (Edge<?, ?> edge : terminalSingleEdges) {
            nodesById.put(edge.identify().getSecond(), (Node<?>) edge.getRightEndpoint());
        }

        for (Edge<?, ?> edge : terminalMultiEdges) {
            nodesById.put(edge.identify().getSecond(), (Node<?>) edge.getRightEndpoint());
        }

        return edgesWithTerminalEdges;
    }

    private List<Edge<?, ?>> withTerminalLeafEdges(Map<UUID, Node<?>> nodesById, List<Edge<?, ?>> edges) {
        Set<UUID> leftNodeIds = edges.stream().map(edge -> edge.identify().getFirst()).collect(Collectors.toSet());
        Set<UUID> rightNodeIds = edges.stream().map(edge -> edge.identify().getSecond()).collect(Collectors.toSet());
        //Set<UUID> allNodeIds = Stream.concat(leftNodeIds.stream(), rightNodeIds.stream()).collect(Collectors.toSet());
        List<UUID> nodeIdsOnlyPresentOnRightSideOfEdges = rightNodeIds.stream().filter(nodeId -> !leftNodeIds.contains(nodeId)).collect(Collectors.toList());
        List<Edge<?, ?>> terminalLeafEdges = nodeIdsOnlyPresentOnRightSideOfEdges.stream().map(nodeId ->(Edge<?, ?>)nodesById.get(nodeId).terminate(0)).collect(Collectors.toList());
        // to terminalLeafEdges, we also want to add all of disconnectedNodes.map(n.terminate)
        List<Node<?>> disconnectedNodes = GraphChoice.computeDisconnectedNodes(new ArrayList<>(nodesById.values()), edges);
        for (Node<?> node : disconnectedNodes) {
            Edge<?, ?> terminalEdge = (Edge<?, ?>) node.terminate(0);
            terminalLeafEdges.add(terminalEdge);
        }

        for (Edge<?, ?> edge : terminalLeafEdges) {
            nodesById.put(edge.identify().getSecond(), (Node<?>) edge.getRightEndpoint());
        }

        return Stream.concat(terminalLeafEdges.stream(), edges.stream()).collect(Collectors.toList());
    }

    public List<UUID> findSources(Map<UUID, Node<?>> nodesById, List<Edge<?, ?>> edges) {
        Set<UUID> leftNodeIds = edges.stream().map(edge -> edge.identify().getFirst()).collect(Collectors.toSet());
        Set<UUID> rightNodeIds = edges.stream().map(edge -> edge.identify().getSecond()).collect(Collectors.toSet());
        List<UUID> nodeIdsOnlyPresentOnLeftSideOfEdges = leftNodeIds.stream().filter(nodeId -> !rightNodeIds.contains(nodeId)).collect(Collectors.toList());

        Set<UUID> allNodeIds = nodesById.keySet();
        Set<UUID> allNodesWhichAreInEdges = Stream.concat(leftNodeIds.stream(), rightNodeIds.stream()).collect(Collectors.toSet());
        List<UUID> nodeIdsWhichAreNotInEdges = allNodeIds.stream().filter(nodeId -> !allNodesWhichAreInEdges.contains(nodeId)).collect(Collectors.toList());

        return Stream
                .concat(nodeIdsOnlyPresentOnLeftSideOfEdges.stream(), nodeIdsWhichAreNotInEdges.stream())
                .collect(Collectors.toList());
    }
}
