package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.retrocheck.graph.Randomizer;

import java.util.Random;

public class DefaultRandomizer implements Randomizer {
    private SourceOfRandomness sourceOfRandomness;
    private long seed;

    public DefaultRandomizer() {
        sourceOfRandomness = new SourceOfRandomness(new Random());
        seed = sourceOfRandomness.seed();
    }

    public DefaultRandomizer(long seed) {
        sourceOfRandomness = new SourceOfRandomness(new Random());
        sourceOfRandomness.setSeed(seed);
        this.seed = seed;
    }

    public DefaultRandomizer(SourceOfRandomness sourceOfRandomness) {
        this.sourceOfRandomness = sourceOfRandomness;
        this.seed = sourceOfRandomness.seed();
    }

    @Override
    public int random() {
        return sourceOfRandomness.nextInt();
    }

    @Override
    public int random(int upperExclusive) {
        return sourceOfRandomness.nextInt(upperExclusive);
    }

    @Override
    public int random(int lowerInclusive, int upperExclusive) {
        return sourceOfRandomness.nextInt(lowerInclusive, upperExclusive);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    public SourceOfRandomness getSourceOfRandomness() {
        return sourceOfRandomness;
    }
}
