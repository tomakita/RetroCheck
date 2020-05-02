package com.retrocheck.graph;

public class Result {
    private GraphChoice graphChoice;
    private CyWorkflow cyWorkflow;

    public Result(GraphChoice graphChoice) {
        this.graphChoice = graphChoice;
    }

    public Result(GraphChoice graphChoice, CyWorkflow cyWorkflow) {
        this.graphChoice = graphChoice;
        this.cyWorkflow = cyWorkflow;
    }

    public GraphChoice getGraphChoice() {
        return graphChoice;
    }

    public CyWorkflow getCyWorkflow() {
        return cyWorkflow;
    }
}
