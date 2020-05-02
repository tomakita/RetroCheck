package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public interface Generatable<T> {
    T arbitrary(SourceOfRandomness randomness, GenerationStatus status);
}
