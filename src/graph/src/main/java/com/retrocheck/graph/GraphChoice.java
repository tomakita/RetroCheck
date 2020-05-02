package com.retrocheck.graph;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GraphChoice {
    private List<CyNode> nodes;
    private List<CyEdge> edges;

    public GraphChoice(List<CyNode> nodes, List<CyEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<CyNode> getNodes() {
        return nodes;
    }

    public List<CyEdge> getEdges() {
        return edges;
    }

    public static Set<Node<?>> computeConnectedNodes(List<Edge<?, ?>> edges) {
        Set<Node<?>> connectedNodes = new HashSet<>();
        for (Edge<?, ?> edge : edges) {
            connectedNodes.add((Node<?>)edge.getLeftEndpoint());
            connectedNodes.add((Node<?>)edge.getRightEndpoint());
        }

        return connectedNodes;
    }

    public static List<Node<?>> computeDisconnectedNodes(List<Node<?>> nodes, List<Edge<?, ?>> edges) {
        Set<Node<?>> connectedNodes = computeConnectedNodes(edges);
        List<Node<?>> disconnectedNodes = nodes.stream().filter(n -> !connectedNodes.contains(n)).collect(Collectors.toList());

        return disconnectedNodes;
    }

    public static GraphChoice fromNodesAndEdges(List<Node<?>> nodes, List<Edge<?, ?>> edges, Map<String, String> nodeColorsBySubgraphName, Map<String, String> edgeColorsByMultiedgeSetId, boolean includeNullNodes) {
        //Set<Node<?>> connectedNodes = computeConnectedNodes(edges);
        //List<CyNode> cyNodes = nodes.stream().filter(connectedNodes::contains).map(node -> {

        String nullNodeName = "_";
        List<Edge<?, ?>> edgesWithoutNullNodes = edges.stream().filter(e -> !((Node<?>)e.getRightEndpoint()).getName().equals(nullNodeName)).collect(Collectors.toList());
        List<Node<?>> nodesWithoutNullNodes = nodes.stream().filter(n -> !n.getName().equals(nullNodeName)).collect(Collectors.toList());

        if (!includeNullNodes) {
            nodes = nodesWithoutNullNodes;
            edges = edgesWithoutNullNodes;
        }

        String defaultColorHex = "#4eff84";
        String greyColorHex = "#eeeeee";
        String entryPointColorHex =  "#e032ff";

        List<String> subgraphNames = nodes.stream().map(node -> node.getSubgraphName()).distinct().collect(Collectors.toList());
        Color[] nodeColors = Colors.generateVisuallyDistinctColors(subgraphNames.size(), 0.8f, 0.3f);

        Map<String, String> _nodeColorsBySubgraphName = IntStream.range(0, subgraphNames.size()).boxed().map(i -> new Tuple<>(subgraphNames.get(i), Colors.toHex(nodeColors[i]))).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        for (Map.Entry<String, String> subgraphNameColorHexPair : _nodeColorsBySubgraphName.entrySet()) {
            nodeColorsBySubgraphName.putIfAbsent(subgraphNameColorHexPair.getKey(), subgraphNameColorHexPair.getValue());
        }
        nodeColorsBySubgraphName.replace("ROOT", defaultColorHex);
        //Map<String, String> nodeColorsBySubgraphName = IntStream.range(0, subgraphNames.size()).boxed().map(i -> new Tuple<>(subgraphNames.get(i), Colors.toHex(nodeColors[i]))).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        //nodeColorsBySubgraphName.replace("ROOT", defaultColorHex);

        List<CyNode> cyNodes = nodes.stream().map(node -> {
            String nodeId = node.toString();
            boolean isNodeTheNullNode = nodeId.equals("_");

            String colorHex = defaultColorHex;
            if (isNodeTheNullNode) {
                colorHex = greyColorHex;
            } else {
                colorHex = nodeColorsBySubgraphName.get(node.getSubgraphName());
            }
//            else if (node.isEntryPoint()) {
//                colorHex = entryPointColorHex;
//            }

            return new CyNode(isNodeTheNullNode ? "_" : node.toString(), node.identify().toString(), node.probability(), node.getSubgraphName(), colorHex);
        }).collect(Collectors.toList());

        List<Edge<?, ?>> multiedges = edges.stream().filter(edge -> edge.containingSetId().isPresent()).collect(Collectors.toList());
        List<String> setIds = multiedges.stream().map(edge -> edge.containingSetId().get()).distinct().collect(Collectors.toList());
        Color[] edgeColors = Colors.generateVisuallyDistinctColors(setIds.size(), 0.8f, 0.3f);

        Map<String, String> _edgeColorsByMultiedgeSetId = IntStream.range(0, setIds.size()).boxed().map(i -> new Tuple<>(setIds.get(i), Colors.toHex(edgeColors[i]))).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        for (Map.Entry<String, String> setIdColorHexPair : _edgeColorsByMultiedgeSetId.entrySet()) {
            edgeColorsByMultiedgeSetId.putIfAbsent(setIdColorHexPair.getKey(), setIdColorHexPair.getValue());
        }
        //Map<String, String> edgeColorsByMultiedgeSetId = IntStream.range(0, setIds.size()).boxed().map(i -> new Tuple<>(setIds.get(i), Colors.toHex(edgeColors[i]))).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

        List<CyEdge> cyEdges = edges.stream().map(edge -> {
            Tuple<UUID, UUID> endpointIds = edge.identify();
            Optional<String> maybeSetId = edge.containingSetId();
            boolean isMultiedge = maybeSetId.isPresent();
            String defaultEdgeHex = "#dddddd";
            String colorHex = isMultiedge ? edgeColorsByMultiedgeSetId.get(maybeSetId.get()) : defaultEdgeHex;
            return new CyEdge(endpointIds.getFirst().toString() + "->" + endpointIds.getSecond().toString(), endpointIds.getFirst().toString(), endpointIds.getSecond().toString(), edge.probability(), colorHex);
        }).collect(Collectors.toList());

        return new GraphChoice(cyNodes, cyEdges);
    }
}
