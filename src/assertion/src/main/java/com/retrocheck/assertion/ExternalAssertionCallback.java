package com.retrocheck.assertion;

import java.util.List;

public interface ExternalAssertionCallback {
    void execute(String calledSignature, Tuple<String, Object> invocationResult, List<Tuple<String, Object>> signatureTypesAndValues, Object instance, String continuation, boolean isExecutionComplete);
}