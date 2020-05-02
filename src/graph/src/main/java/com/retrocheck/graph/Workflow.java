package com.retrocheck.graph;

import java.util.List;

public class Workflow {
    private List<Entity<Object>> dataSetup;
    private Outcome outcome;
    private long seed;

    public Workflow(List<Entity<Object>> dataSetup) {
        this.dataSetup = dataSetup;
    }

    public Workflow(List<Entity<Object>> dataSetup, Outcome outcome) {
        this.dataSetup = dataSetup;
        this.outcome = outcome;
    }

    public List<Entity<Object>> getDataSetup() {
        return dataSetup;
    }

    public void setDataSetup(List<Entity<Object>> dataSetup) {
        this.dataSetup = dataSetup;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workflow workflow = (Workflow) o;
        List<Entity<Object>> otherDataSetup = workflow.getDataSetup();

        if (dataSetup.size() != otherDataSetup.size()) return false;

        boolean areDataSetupsEqual = true;
        for (int i = 0; i < dataSetup.size(); i++) {
            Object a = dataSetup.get(i).getInstance();
            Object b = otherDataSetup.get(i).getInstance();

            if (!a.equals(b)) {
                areDataSetupsEqual = false;
                break;
            }
        }

        return seed == workflow.seed &&
                areDataSetupsEqual &&
                outcome.getName().equals(workflow.outcome.getName());
    }
}
