package com.retrocheck.assertion;

public class AssertionResult {
    private boolean isSuccess;
    private String continuation;
    private boolean isExecutionComplete;

    public AssertionResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public AssertionResult(boolean isSuccess, String continuation) {
        this.isSuccess = isSuccess;
        this.continuation = continuation;
    }

    public AssertionResult(boolean isSuccess, String continuation, boolean isExecutionComplete) {
        this.isSuccess = isSuccess;
        this.continuation = continuation;
        this.isExecutionComplete = isExecutionComplete;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getContinuation() {
        return continuation;
    }

    public boolean isExecutionComplete() {
        return isExecutionComplete;
    }

    public static AssertionResult failed = new AssertionResult(false, null);
}
