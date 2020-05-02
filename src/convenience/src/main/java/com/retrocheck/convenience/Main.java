package com.retrocheck.convenience;

import com.retrocheck.graph.*;

import java.util.*;
import java.util.function.Function;

public class Main {
    public static void main(String [] args)
    {
        DefaultGenerator generator = new DefaultGenerator().withUnique(Boolean.class, (r, status, unique) -> unique.compute("b", () -> r.nextBoolean()));

        // for now, nodes have to be specified individually like this (and not directly instantiated in a list
        // initializer), because otherwise we'll have no way of referring to them in edges.
        Node<Integer> jeff = new Node<>("jeff", Integer.class, generator, null, true);
        Node<String> carl = new Node<>("carl", String.class, generator, null);
        Node<Integer> bob = new Node<>("bob", Integer.class, x -> 5, generator, null, Probability.ALWAYS, false);
        Node<Integer> dave = new Node<>("dave", Integer.class, x -> x, generator, null, new Probability(1), false);
        Node<Integer> gene = new Node<>("gene", Integer.class, generator, null);
        Node<Integer> larry = new Node<>("larry", Integer.class, x -> 6, generator, null, new Probability(100), false);
        Node<Boolean> s1 = new Node<>("s1", Boolean.class, generator, null);
        Node<Boolean> s2 = new Node<>("s2", Boolean.class, generator, null);

        List<Node<?>> nodes = Arrays.asList(
                jeff,
                carl,
                bob,
                dave,
                gene,
                larry,
                s1,
                s2);

        List<Edge<?, ?>> edges = Arrays.asList(
                new Edge<>(jeff, carl, (u, v) -> u.toString(), new Probability(50)),
                new Edge<>(bob, dave, (u, v) -> u, new Probability(50), "x"),
                new Edge<>(bob, gene, null, new Probability(40), "x"),
                new Edge<>(dave, larry, null, new Probability(75)));

        Node<Integer> a = new Node<>("a", Integer.class, generator, null, true);
        Node<Integer> b = new Node<>("b", Integer.class, generator, null, true);
        Subgraph subgraph = new Subgraph(false).withName("test subgraph").withNodes(Arrays.asList(a, b));

        Map<String, Function<?, ?>> loader = new HashMap<>();
        loader.put("redis", entityInstance -> entityInstance);
        Map<String, Function<?, ?>> unloader = new HashMap<>();
        unloader.put("redis", entityInstance -> entityInstance);
        DefaultDataLoader dataLoader = new DefaultDataLoader(loader, unloader);
        DefaultTester tester = new DefaultTester("EXAMPLE GRAPH");
        DefaultGraph graph = new DefaultGraph().withNodes(nodes).withEdges(edges).withSubgraphs(Arrays.asList(subgraph));
        tester.preprocess(graph);
        tester.process(x -> true, new Outcome("eh")); // just here for testing, doesn't actually do anything.
        tester.postprocess();
        dataLoader.destroy();
    }
}