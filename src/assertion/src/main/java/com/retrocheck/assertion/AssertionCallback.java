package com.retrocheck.assertion;

import org.aspectj.lang.Signature;
import java.util.List;

public interface AssertionCallback {
    void execute(Signature calledSignature, Tuple<String, Object> invocationResult, List<Tuple<String, Object>> signatureTypesAndValues, Object instance, String continuation, boolean isExecutionComplete);
}
