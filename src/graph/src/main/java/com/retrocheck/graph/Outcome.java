package com.retrocheck.graph;

public class Outcome {
    private String name;
    private boolean completeImmediately;

    public Outcome(String name) {
        this.name = name;
    }

    public Outcome(String name, boolean completeImmediately) {
        this.name = name;
        this.completeImmediately = completeImmediately;
    }

    public Outcome(boolean completeImmediately) {
        this.name = "";
        this.completeImmediately = completeImmediately;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleteImmediately() {
        return completeImmediately;
    }
}
