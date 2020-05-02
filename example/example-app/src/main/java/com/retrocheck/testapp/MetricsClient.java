package com.retrocheck.testapp;

import com.retrocheck.mock.MockWith;

public class MetricsClient {
    // [Mocking]: TODO: should be used sparingly, only for systems that we don't own, can make system
    //                  difficult to reason about.  mock signature.
    // The MockWith annotation tells RetroCheck where to find the mock for this method.
    @MockWith(MetricsClientMock.class)
    public void emit() {
        // Imagine that there's code here that we don't want to execute during our tests.
    }
}
