package com.retrocheck.assertion;

import org.aspectj.lang.Signature;

import java.util.List;

public class OutcomeHandlingMetadata {
    private Signature calledSignature;
    private Tuple<String, Object> invocationResults;
    private List<Tuple<String, Object>> signatureTypesAndValues;
    private String continuation;
    private boolean isExecutionComplete;

    public Signature getCalledSignature() {
        return calledSignature;
    }

    public Tuple<String, Object> getInvocationResults() {
        return invocationResults;
    }

    public List<Tuple<String, Object>> getSignatureTypesAndValues() {
        return signatureTypesAndValues;
    }

    public String getContinuation() {
        return continuation;
    }

    public boolean isExecutionComplete() {
        return isExecutionComplete;
    }

    public OutcomeHandlingMetadata(Signature calledSignature, Tuple<String, Object> invocationResults, List<Tuple<String, Object>> signatureTypesAndValues, String continuation, boolean isExecutionComplete) {
        this.calledSignature = calledSignature;
        this.invocationResults = invocationResults;
        this.signatureTypesAndValues = signatureTypesAndValues;
        this.continuation = continuation;
        this.isExecutionComplete = isExecutionComplete;
    }
}
