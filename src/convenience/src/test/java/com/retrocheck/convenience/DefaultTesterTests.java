package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.retrocheck.graph.*;
import com.retrocheck.graph.Tuple;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class DefaultTesterTests {
    @Property(trials = 1000)
    public void tester_onSameSeed_yieldsSameWorkflow(@From(GraphGenerator.class) DefaultGraph graph){
        Workflow originalWorkflow = new Workflow(null);

        // Save unmutated copies of nodes and edges.
        List<Node<?>> nodes = graph.getNodes();
        List<Edge<?, ?>> edges = graph.getEdges();
        // Assign them to the graph, making copies (internal to the graph) in the process.
        graph.setNodes(nodes);
        graph.setEdges(edges);

        DefaultTester tester = new DefaultTester("test");
        tester.preprocess(graph);
        tester.process(workflow -> {
            originalWorkflow.setDataSetup(workflow.getDataSetup());
            originalWorkflow.setSeed(workflow.getSeed());
            originalWorkflow.setOutcome(workflow.getOutcome());
            return true;
        }, new Outcome(true));

        tester = new DefaultTester("test");
        // Assign the original, unmutated nodes and edges to the graph for the second test run.
        graph.setNodes(nodes);
        graph.setEdges(edges);

        tester.preprocess(graph.reSeed(graph.getSeed()));
        tester.process(workflow -> {
            assertEquals(originalWorkflow, workflow);
            return true;
        }, new Outcome(true));
    }

    @Property(trials = 1000)
    public void tester_entityValuesInResultingWorkflow_reflectEdgesInGraph(@From(AlwaysEdgedGraphGenerator.class) DefaultGraph graph){
        List<Tuple<UUID, UUID>> idsOfExpectedEqualValuedNodes = graph.getEdges().stream().map(Edge::identify).collect(Collectors.toList());

        DefaultTester tester = new DefaultTester("test");
        tester.preprocess(graph);

        tester.process(workflow -> {
            Map<UUID, Integer> nodeValuesById = workflow.getDataSetup().stream().collect(Collectors.toMap(Entity::getId, el -> (Integer)el.getInstance()));
            for (Tuple<UUID, UUID> nodePair : idsOfExpectedEqualValuedNodes) {
                assertEquals(nodeValuesById.get(nodePair.getFirst()), nodeValuesById.get(nodePair.getSecond()));
            }
            return true;
        }, new Outcome(true));
    }
}
