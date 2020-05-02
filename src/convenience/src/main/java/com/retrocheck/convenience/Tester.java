package com.retrocheck.convenience;

import com.retrocheck.graph.Graph;
import com.retrocheck.graph.Outcome;
import com.retrocheck.graph.Workflow;

import java.util.function.Function;

public interface Tester {
    void preprocess(Graph graph);

    <T> T process(Function<Workflow, T> loader, Outcome defaultOutcome);

    void postprocess();
}
