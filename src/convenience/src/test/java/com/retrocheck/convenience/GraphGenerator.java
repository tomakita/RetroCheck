package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.retrocheck.graph.Edge;
import com.retrocheck.graph.Node;
import com.retrocheck.graph.Probability;

import java.util.*;

public class GraphGenerator extends Generator<DefaultGraph> {
    public GraphGenerator() {
        super(DefaultGraph.class);
    }

    // Generate a graph that has no cycles.
    @Override
    public DefaultGraph generate(
            SourceOfRandomness r,
            GenerationStatus status) {
        DefaultGenerator generator = new DefaultGenerator();

        List<Node<?>> nodes = new ArrayList<>();
        int nodeCount = r.nextInt(1, 100);
        //int nodeCount = 2;
        for (int i = 0; i < nodeCount; i++) {
            Node<Integer> node = new Node<>(UUID.randomUUID().toString(), Integer.class, generator, "");
            nodes.add(node);
        }

        List<Edge<?, ?>> edges = new ArrayList<>();
        // This is a very lame way of ensuring no cycles.
        Set<UUID> usedNodeIds = new HashSet<>();
        int edgeCount = r.nextInt(Math.min(40, nodeCount));
        //int edgeCount = 1;
        Probability edgeProbability = r.choose(new Probability[] { new Probability(0), new Probability(50), Probability.ALWAYS });
        for (int i = 0; i < edgeCount; i++) {
            Node<Integer> u = (Node<Integer>)nodes.get(i);
            if (usedNodeIds.contains(u.identify())) {
                continue;
            }

            int j = 0;
            Node<Integer> v = (Node<Integer>) nodes.get(j);
            while (j == i || usedNodeIds.contains(v.identify())) {
                j++;
                if (j >= nodes.size()) {
                    continue;
                }
                v = (Node<Integer>) nodes.get(j);
            }

            Edge<Integer, Integer> edge = new Edge<>(u, v, (_u, _v) -> _u, edgeProbability);
            //Edge<Integer, Integer> edge = new Edge<>(u, v, (_u, _v) -> _u, new Probability(99));
            edges.add(edge);
            usedNodeIds.add(u.identify());
            usedNodeIds.add(v.identify());
        }

        return new DefaultGraph().withNodes(nodes).withEdges(edges);
    }
}
