package com.retrocheck.assertion;

import org.aspectj.lang.Signature;
import java.util.ArrayList;
import java.util.List;

public class AssertionExceptionEvent {
    private List<AssertionExceptionCallback> callbacks = new ArrayList<>();

    public int size() {
        return callbacks.size();
    }

    public void add(AssertionExceptionCallback callback) {
        callbacks.add(callback);
    }

    public void clear() {
        callbacks.clear();
    }

    public void trigger(Signature calledSignature, List<Tuple<String, Object>> signatureTypesAndValues, Object target, Exception ex) {
        callbacks.forEach(callback -> callback.execute(calledSignature, signatureTypesAndValues, target, ex));
    }
}
