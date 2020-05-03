package com.retrocheck.testapp;

import com.retrocheck.mock.MockWith;

public class MetricsClient {
    // The MockWith annotation tells RetroCheck where to find the mock for this method.
    @MockWith(MetricsClientMock.class)
    public void emit() {
        // Imagine that there's code here that we don't want to execute during our tests.
    }
}
