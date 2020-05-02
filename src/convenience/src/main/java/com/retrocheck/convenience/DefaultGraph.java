package com.retrocheck.convenience;

import com.retrocheck.graph.*;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGraph implements Graph {
    private List<Node<?>> nodes = new ArrayList<>();
    private List<Edge<?, ?>> edges = new ArrayList<>();
    private List<Subgraph> subgraphs = new ArrayList<>();

    private long seed;
    private Randomizer randomizer;

    public DefaultGraph() {}

    public DefaultGraph withNodes(List<Node<?>> nodes) {
        this.nodes.addAll(copyNodes(nodes));
        this.randomizer = DefaultGenerator.unify(this.nodes.stream().map(node -> node.getGenerator()).distinct().collect(Collectors.toList()));
        this.seed = this.randomizer.getSeed();
        return this;
    }

    public DefaultGraph withEdges(List<Edge<?, ?>> edges) {
        this.edges.addAll(copyEdges(edges));
        return this;
    }

    public DefaultGraph withSubgraphs(List<Subgraph> subgraphs) {
        this.subgraphs.addAll(subgraphs);

        for (Subgraph subgraph : subgraphs) {
            this.nodes.addAll(copyNodes(subgraph.getNodes()));
            this.edges.addAll(copyEdges(subgraph.getEdges()));
        }

        this.randomizer = DefaultGenerator.unify(this.nodes.stream().map(node -> node.getGenerator()).distinct().collect(Collectors.toList()));
        this.seed = this.randomizer.getSeed();
        return this;
    }

    public DefaultGraph reSeed(long seed) {
        this.randomizer = DefaultGenerator.unifyWithSeed(this.nodes.stream().map(node -> node.getGenerator()).distinct().collect(Collectors.toList()), seed);
        this.seed = this.randomizer.getSeed();
        return this;
    }

    public List<Node<?>> getNodes() {
        return nodes;
    }

    // temporary?
    public void setNodes(List<Node<?>> nodes) {
        this.nodes = copyNodes(nodes);
    }

    public List<Edge<?, ?>> getEdges() {
        return edges;
    }

    // temporary?
    public void setEdges(List<Edge<?, ?>> edges) {
        this.edges = copyEdges(edges);
    }

    public List<Subgraph> getSubgraphs() {
        return subgraphs;
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public Randomizer getRandomizer() {
        return randomizer;
    }

    private List<Node<?>> copyNodes(List<Node<?>> nodes) {
        return nodes.stream().map(Node::copy).collect(Collectors.toList());
    }

    private List<Edge<?, ?>> copyEdges(List<Edge<?, ?>> edges) {
        if (this.nodes.size() == 0) {
            // TODO: this indicates that the builder pattern is not a good fit here...
            throw new IllegalArgumentException("Unable to add edges to a graph that has no nodes.  Add nodes first, via Graph.withNodes.");
        }
        Map<UUID, Node<?>> nodesByIdCopy = this.nodes.stream().collect(Collectors.toMap(Node::identify, node -> node));

        return edges.stream().map(edge -> edge.copy(nodesByIdCopy)).collect(Collectors.toList());
    }
}
