package com.retrocheck.convenience;

public interface AssertionCallback {
    // Note that the first param here is different than in retrocheck.assertion.AssertionCallback,
    // because i didn't want to couple this lib to aspectj.
    void execute(AssertionResultStrings result);
}