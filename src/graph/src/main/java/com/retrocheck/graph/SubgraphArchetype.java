package com.retrocheck.graph;

import java.util.List;
import java.util.stream.Collectors;

// For making copies of a subgraph, if the same subgraph appears multiple times (but we need multiple copies)
// in the same data model.
public class SubgraphArchetype {
    private List<Node<?>> nodes;
    private List<Edge<?, ?>> edges;

    public SubgraphArchetype(List<Node<?>> nodes, List<Edge<?, ?>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Subgraph induce(String newName, boolean setNodeNames) {
        return new Subgraph(setNodeNames)
                .withName(newName)
                .withNodes(nodes.stream().map(n -> (Node<?>)n.copyWithNewId()).collect(Collectors.toList()))
                .withEdges(edges.stream().map(e -> (Edge<?, ?>)e.deepCopy()).collect(Collectors.toList()));
    }
}
