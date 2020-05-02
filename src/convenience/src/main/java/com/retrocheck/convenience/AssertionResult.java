package com.retrocheck.convenience;

import java.util.List;

public class AssertionResult {
    private String calledSignature;
    private Tuple<String, Object> invocationResult;
    private List<Tuple<String, Object>> signatureTypesAndValues;
    private Object instance;

    public String getCalledSignature() {
        return calledSignature;
    }

    public Tuple<String, Object> getInvocationResult() {
        return invocationResult;
    }

    public List<Tuple<String, Object>> getSignatureTypesAndValues() {
        return signatureTypesAndValues;
    }

    public Object getInstance() {
        return instance;
    }

    public AssertionResult() {}

    public AssertionResult(String calledSignature, Tuple<String, Object> invocationResult, List<Tuple<String, Object>> signatureTypesAndValues, Object instance) {
        this.calledSignature = calledSignature;
        this.invocationResult = invocationResult;
        this.signatureTypesAndValues = signatureTypesAndValues;
        this.instance = instance;
    }
}
