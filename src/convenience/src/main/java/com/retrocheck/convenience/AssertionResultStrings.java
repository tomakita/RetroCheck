package com.retrocheck.convenience;

import java.io.Serializable;
import java.util.Objects;

public class AssertionResultStrings implements Serializable {
    private String methodName;
    private String output;
    private String input;
    private String continuation;
    private boolean isExecutionComplete;

    public AssertionResultStrings() {}

    public AssertionResultStrings(String methodName, String output, String input, String continuation, boolean isExecutionComplete) {
        this.methodName = methodName;
        this.output = output;
        this.input = input;
        this.continuation = continuation;
        this.isExecutionComplete = isExecutionComplete;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getOutput() {
        return output;
    }

    public String getInput() {
        return input;
    }

    public String getContinuation() {
        return continuation;
    }

    public boolean isExecutionComplete() {
        return isExecutionComplete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssertionResultStrings that = (AssertionResultStrings) o;
        return isExecutionComplete == that.isExecutionComplete &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(output, that.output) &&
                Objects.equals(input, that.input) &&
                Objects.equals(continuation, that.continuation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, output, input, continuation, isExecutionComplete);
    }

    @Override
    public String toString() {
        return methodName
                + System.lineSeparator() + input
                + System.lineSeparator() + output
                + System.lineSeparator() + continuation
                + System.lineSeparator() + isExecutionComplete;
    }
}
