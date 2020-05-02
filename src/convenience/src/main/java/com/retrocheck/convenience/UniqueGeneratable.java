package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.retrocheck.graph.Unique;

public interface UniqueGeneratable<T> {
    T arbitrary(SourceOfRandomness randomness, GenerationStatus status, Unique unique);
}
