package com.retrocheck.graph;

public interface Randomizer {
    int random();
    int random(int upperExclusive);
    int random(int lowerInclusive, int upperExclusive);
    long getSeed();
}
