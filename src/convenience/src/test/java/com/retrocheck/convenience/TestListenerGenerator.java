package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.*;

public class TestListenerGenerator extends Generator<TestListener> {
    public TestListenerGenerator() {
        super(TestListener.class);
    }

    @Override
    public TestListener generate(
            SourceOfRandomness r,
            GenerationStatus status) {
        CustomStringGenerator stringGenerator = new CustomStringGenerator();
        String[] methodNames = new String[] { "edge", "of", "sanity", "y", ".", "and", "?" };

        List<Integer> triggerSequence = new ArrayList<>();
        List<AssertionResultStrings> resultSequence = new ArrayList<>();
        int sequenceLength = r.nextInt(100);
        for (int i = 0; i < sequenceLength; i++) {
            triggerSequence.add(r.nextInt(0, 2));

            String methodName = r.choose(methodNames);
            String output = stringGenerator.generate(r, status);
            String input = stringGenerator.generate(r, status);
            String continuation = r.choose(methodNames);
            boolean isExecutionComplete = r.nextBoolean();
            AssertionResultStrings result = new AssertionResultStrings(methodName, output, input, continuation, isExecutionComplete);
            resultSequence.add(result);
        }

        return new TestListener(triggerSequence, resultSequence);
    }
}
