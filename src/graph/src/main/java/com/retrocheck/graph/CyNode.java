package com.retrocheck.graph;

public class CyNode {
    private Data data;
    private Style style;

    public CyNode(String name, String id, int probability, String subgraph, String backgroundColor) {
        data = new Data(name, id, probability, subgraph);
        style = new Style(backgroundColor);
    }

    public class Data {
        public String name;
        public String id;
        public int probability;
        public String subgraph;

        public Data(String name, String id, int probability, String subgraph) {
            this.name = name + " (" + probability + ")";
            this.id = id;
            this.probability = probability;
            this.subgraph = subgraph;
        }
    }

    public class Style {
        public String backgroundColor;

        public Style(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
    }
}
