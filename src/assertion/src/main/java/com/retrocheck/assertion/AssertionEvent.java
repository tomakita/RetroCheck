package com.retrocheck.assertion;

import org.aspectj.lang.Signature;

import java.util.ArrayList;
import java.util.List;

public class AssertionEvent {
    private List<AssertionCallback> callbacks = new ArrayList<>();

    public int size() {
        return callbacks.size();
    }

    public void add(AssertionCallback callback) {
        callbacks.add(callback);
    }

    public void clear() {
        callbacks.clear();
    }

    public void trigger(Signature calledSignature, Tuple<String, Object> invocationResult, List<Tuple<String, Object>> signatureTypesAndValues, Object instance, String continuation, boolean isExecutionComplete) {
        callbacks.forEach(callback -> callback.execute(calledSignature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete));
    }
}
