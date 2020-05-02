package com.retrocheck.graph;

import java.util.*;
import java.util.stream.Collectors;

public class BucketOfDestiny<T> {
    private Randomizer r;
    private List<T> bucket;

    private BucketOfDestiny(List<T> bucket, Randomizer r) {
        this.bucket = bucket;
        this.r = r;
    }

    public T choose() {
        return bucket.get(r.random(bucket.size()));
    }

    public static <T> BucketOfDestiny<T> fromProbabilities(List<Tuple<T, Integer>> probabilitiesBySetId, Randomizer r) {
        List<T> probabilityRanges = probabilitiesBySetId.stream().map(pair -> Collections.nCopies(pair.getSecond(), pair.getFirst())).flatMap(List::stream).collect(Collectors.toList());

        if (probabilityRanges.size() != 100) {
            throw new RuntimeException("One or more multiedges has cumulative probability != 100");
        }

        return new BucketOfDestiny<T>(probabilityRanges, r);
    }

    public static boolean fromProbability(int probability, Randomizer r) {
        List<Boolean> probabilityRange = new ArrayList<>();
        for (int i = 0; i < probability; i++) {
            probabilityRange.add(true);
        }
        for (int i = probability; i < 100; i++) {
            probabilityRange.add(false);
        }

        if (probability > 100 || probability < 0) {
            throw new RuntimeException("Probability p must satisfy 0 <= p <= 100");
        }

        return new BucketOfDestiny<>(probabilityRange, r).choose();
    }
}
