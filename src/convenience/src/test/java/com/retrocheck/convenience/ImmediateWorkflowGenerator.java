package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.retrocheck.graph.Graph;
import com.retrocheck.graph.Outcome;
import com.retrocheck.graph.Workflow;

public class ImmediateWorkflowGenerator extends Generator<Workflow> {
    public ImmediateWorkflowGenerator() {
        super(Workflow.class);
    }

    @Override
    public Workflow generate(
            SourceOfRandomness r,
            GenerationStatus status) {
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph graph = graphGenerator.generate(r, status);
        String[] outcomes = new String[] { "edge", "of", "sanity", "y", ".", "and", "?" };

        Workflow workflow = new Workflow(null);
        DefaultTester tester = new DefaultTester("test");
        tester.preprocess(graph);
        tester.process(_workflow -> {
            workflow.setDataSetup(_workflow.getDataSetup());
            workflow.setSeed(_workflow.getSeed());
            workflow.setOutcome(_workflow.getOutcome());
            return true;
        }, new Outcome(r.choose(outcomes), true));

        return workflow;
    }
}
