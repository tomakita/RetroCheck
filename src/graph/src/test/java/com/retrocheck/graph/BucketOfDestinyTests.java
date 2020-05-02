package com.retrocheck.graph;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

// Note: I'm not using PBT here because we need to run these tests on large sample sets
//       in order to overcome variance, and PBT would be too much of a performance hit
//       on such large sample sets.

public class BucketOfDestinyTests {
    @Test
    public void fromProbability_resultsInASomewhatUniformDistribution() {
        Map<Boolean, AtomicInteger> countsByDestiny = new HashMap<>();
        countsByDestiny.put(true, new AtomicInteger(0));
        countsByDestiny.put(false, new AtomicInteger(0));

        int samples = 1000000;
        for (int i = 0; i < samples; i++) {
            countsByDestiny.get(BucketOfDestiny.fromProbability(50, new JavaRandomizer())).getAndIncrement();
        }

        double tolerance = 0.01 * samples;
        int t = countsByDestiny.get(true).get();
        int f = countsByDestiny.get(false).get();
        int difference = Math.abs(t - f);
        assertTrue(difference < tolerance);
    }

    @Test
    public void fromProbabilities_resultsInASomewhatUniformDistribution() {
        List<Tuple<Integer, Integer>> setIdProbabilityPairs = new ArrayList<>();
        setIdProbabilityPairs.add(new Tuple<>(0, 10));
        setIdProbabilityPairs.add(new Tuple<>(1, 5));
        setIdProbabilityPairs.add(new Tuple<>(2, 11));
        setIdProbabilityPairs.add(new Tuple<>(3, 35));
        setIdProbabilityPairs.add(new Tuple<>(4, 39));

        Map<Integer, AtomicInteger> countsByDestiny = new HashMap<>();
        countsByDestiny.put(0, new AtomicInteger(0));
        countsByDestiny.put(1, new AtomicInteger(0));
        countsByDestiny.put(2, new AtomicInteger(0));
        countsByDestiny.put(3, new AtomicInteger(0));
        countsByDestiny.put(4, new AtomicInteger(0));

        BucketOfDestiny<Integer> bucket = BucketOfDestiny.fromProbabilities(setIdProbabilityPairs, new JavaRandomizer());
        int samples = 1000000;
        for (int i = 0; i < samples; i++) {
            countsByDestiny.get(bucket.choose()).getAndIncrement();
        }

        double tolerance = 0.01;
        for (int i = 0; i < 5; i++) {
            int probability = setIdProbabilityPairs.get(i).getSecond();
            double expectedPercentage = (double)probability / 100;
            int observations = countsByDestiny.get(i).get();
            double observedPercentage = (double)observations / samples;

            double difference = Math.abs(observedPercentage - expectedPercentage);
            assertTrue(difference < tolerance);
        }
    }
}
