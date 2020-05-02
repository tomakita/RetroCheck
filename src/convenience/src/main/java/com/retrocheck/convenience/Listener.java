package com.retrocheck.convenience;

public interface Listener {
    AssertionEvent getAssertionSuccessEvent();

    AssertionEvent getAssertionFailureEvent();

    AssertionEvent getInvocationCompletionEvent();

    void start();

    void destroy();
}
