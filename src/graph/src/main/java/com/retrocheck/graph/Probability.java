package com.retrocheck.graph;

public class Probability {
    public static Probability ALWAYS = new Probability(100);

    private int value;

    public Probability(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
