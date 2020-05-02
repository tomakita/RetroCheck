package com.retrocheck.graph;

import java.util.Random;

public class JavaRandomizer implements Randomizer {
    private Random r;
    private long seed;

    public JavaRandomizer() {
        this.seed = new Random().nextLong();
        r = new Random(seed);
    }

    @Override
    public int random() {
        return r.nextInt();
    }

    @Override
    public int random(int upperExclusive) {
        return r.nextInt(upperExclusive);
    }

    @Override
    public int random(int lowerInclusive, int upperExclusive) {
        return r.nextInt(upperExclusive - lowerInclusive) + lowerInclusive;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }
}
