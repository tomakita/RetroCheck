package com.retrocheck.graph;

public class CyEdge {
    private Data data;
    private Style style;

    public CyEdge(String id, String source, String target, int probability, String refinement, String backgroundColor) {
        data = new Data(id, source,  target,  probability, refinement);
        style = new Style(backgroundColor);
    }

    public CyEdge(String id, String source, String target, int probability, String color) {
        data = new Data(id, source,  target,  probability, null);
        style = new Style(color);
    }

    public class Data {
        public String id;
        public String source;
        public String target;
        public int probability;
        public String refinement;

        public Data(String id, String source, String target, int probability, String refinement) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.probability = probability;
            this.refinement = refinement;
        }
    }

    public class Style {
        public String lineColor;
        public String targetArrowColor;

        public Style(String color) {
            this.lineColor = color;
            this.targetArrowColor = color;
        }
    }
}
