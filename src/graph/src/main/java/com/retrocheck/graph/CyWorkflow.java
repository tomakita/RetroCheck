package com.retrocheck.graph;

import java.util.List;
import java.util.stream.Collectors;

public class CyWorkflow {
    private String outcome;
    private List<Data> workflow;

    private CyWorkflow(String outcome, List<Data> workflow) {
        this.outcome = outcome;
        this.workflow = workflow;
    }

    public static class Data {
        public String entity;
        public String instance;
        public String instanceName;
        public String id;

        public Data(String entity, String instance, String instanceName, String id) {
            this.entity = entity;
            this.instance = instance;
            this.instanceName = instanceName;
            this.id = id;
        }
    }

    public String getOutcome() {
        return outcome;
    }

    public List<Data> getWorkflow() {
        return workflow;
    }

    public static CyWorkflow fromWorkflow(Workflow w) {
        List<Data> entities = w.getDataSetup().stream().map(entity -> new Data(entity.getSchema().getName(), entity.getInstance().toString().replace("\n", "<br>"), entity.getInstanceName(), entity.getId().toString())).collect(Collectors.toList());
        return new CyWorkflow(w.getOutcome().getName(), entities);
    }
}
