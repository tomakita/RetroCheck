package com.retrocheck.graph;

import java.util.List;

public interface Graph {
    List<Node<?>> getNodes();
    List<Edge<?, ?>> getEdges();
    List<Subgraph> getSubgraphs();
    long getSeed();
    Randomizer getRandomizer();
}
