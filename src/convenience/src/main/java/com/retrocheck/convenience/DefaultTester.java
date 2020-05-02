package com.retrocheck.convenience;

import com.retrocheck.graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DefaultTester implements Tester {
    private Results results;
    private AugmentedGraph baseGraph;
    // node.subgraphName -> color hex
    private Map<String, String> nodeColorsBySubgraphName = new HashMap<>();
    // edge.setId -> color hex
    private Map<String, String> edgeColorsByMultiedgeSetId = new HashMap<>();
    private long seed;
    private Randomizer r;

    public DefaultTester(String title) {
        results = new Results(title);
    }

    @Override
    public void preprocess(Graph graph) {
        List<Node<?>> nodes = graph.getNodes();
        List<Edge<?, ?>> edges = graph.getEdges();
        this.seed = graph.getSeed();
        this.r = graph.getRandomizer();

        GraphChoice rawCyNodesAndCyEdges = GraphChoice.fromNodesAndEdges(nodes, edges, nodeColorsBySubgraphName, edgeColorsByMultiedgeSetId, true);
        Result rawResult = new Result(rawCyNodesAndCyEdges); // will happen at test startup
        Preprocessor preprocessor = new Preprocessor();
        baseGraph = preprocessor.process(nodes, edges);
        GraphChoice nulledCyNodesAndCyEdges = GraphChoice.fromNodesAndEdges(new ArrayList<>(baseGraph.getNodesById().values()), baseGraph.getEdges(), nodeColorsBySubgraphName, edgeColorsByMultiedgeSetId, true);
        Result nulledResult = new Result(nulledCyNodesAndCyEdges); // will happen at test startup
        results.add(rawResult);
        results.add(nulledResult);
    }

    // Outcome class: outcomeName, completeImmediately
    // requires a change in Workflow to replace string with Outcome
    // requires a change in orchestrateConveniently around awaiting

    @Override
    public <T> T process(Function<Workflow, T> loader, Outcome defaultOutcome) {
        AugmentedGraph graphCopy = baseGraph.copy();
        Processor processor = new Processor();
        AugmentedGraph chosenGraph = processor.process(graphCopy, r);
        GraphChoice chosenCyNodesAndCyEdges = GraphChoice.fromNodesAndEdges(new ArrayList<>(chosenGraph.getNodesById().values()), chosenGraph.getEdges(), nodeColorsBySubgraphName, edgeColorsByMultiedgeSetId, false);
        Workflow workflow = processor.traverse(chosenGraph);
        workflow.setOutcome(defaultOutcome);
        workflow.setSeed(this.seed);

        CyWorkflow cyWorkflow = CyWorkflow.fromWorkflow(workflow);
        Result chosenResult = new Result(chosenCyNodesAndCyEdges, cyWorkflow); // will happen once per test case
        results.add(chosenResult);
        return loader.apply(workflow);
    }

    @Override
    public void postprocess() {
        results.writeToFile();
    }
}
