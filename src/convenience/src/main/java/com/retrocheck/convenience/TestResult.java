package com.retrocheck.convenience;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    private List<AssertionResultStrings> assertionResults = new ArrayList<>();
    private long seed;

    public TestResult(List<AssertionResultStrings> assertionResults, long seed) {
        this.assertionResults = assertionResults;
        this.seed = seed;
    }

    public List<AssertionResultStrings> getAssertionResults() {
        return assertionResults;
    }

    public long getSeed() {
        return seed;
    }

    public boolean hasFailures() {
        return assertionResults.size() > 0;
    }
}
