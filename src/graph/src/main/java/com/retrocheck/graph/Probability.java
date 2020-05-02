package com.retrocheck.graph;

public class Probability {
    public static Probability ALWAYS = new Probability(100);

    private int value;

    public Probability(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("value must be between 0 and 100, inclusive!");
        }

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
