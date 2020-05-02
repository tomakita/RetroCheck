package com.retrocheck.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Subgraph {
    private String name;
    private List<Node<?>> nodes = new ArrayList<>();
    private List<Edge<?, ?>> edges = new ArrayList<>();
    private Map<String, Node<?>> nodesByName = new HashMap<>();
    private boolean setNodeNames;

    public Subgraph(boolean setNodeNames) {
        this.setNodeNames = setNodeNames;
    }

    public Subgraph withName(String name) {
        this.name = name;
        return this;
    }

    public Subgraph withNodes(List<Node<?>> nodes) {
        this.nodes.addAll(nodes);

        for (Node<?> node : nodes) {
            nodesByName.put(node.getName(), node);
            node.setSubgraphName(this.name);
            if (this.setNodeNames) {
                if (name == null) {
                    throw new IllegalArgumentException("The user specified that node names should be set, but did not supply the Subgraph with a name!");
                }

                node.setName(node.getName() + "_" + name);
            } else {
                node.setName(node.getName());
            }
        }

        return this;
    }

    public Subgraph withEdges(List<Edge<?, ?>> edges) {
        this.edges.addAll(edges);
        return this;
    }

    public Subgraph withSubgraphs(List<Subgraph> subgraphs) {
        for (Subgraph subgraph : subgraphs) {
            this.nodes.addAll(subgraph.getNodes());
            this.edges.addAll(subgraph.getEdges());
        }

        return this;
    }

    public String getName() {
        return name;
    }

    public List<Node<?>> getNodes() {
        return nodes;
    }

    public List<Edge<?, ?>> getEdges() {
        return edges;
    }

    public Node<?> getNode(String nodeName) {
        if (nodesByName.containsKey(nodeName)) {
            return nodesByName.get(nodeName);
        } else {
            throw new RuntimeException("Node with name <" + nodeName + "> not found in the subgraph!");
        }
    }
}
