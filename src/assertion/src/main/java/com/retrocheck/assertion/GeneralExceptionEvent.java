package com.retrocheck.assertion;

import org.aspectj.lang.Signature;
import java.util.ArrayList;
import java.util.List;

public class GeneralExceptionEvent {
    private List<GeneralExceptionCallback> callbacks = new ArrayList<>();

    public int size() {
        return callbacks.size();
    }

    public void add(GeneralExceptionCallback callback) {
        callbacks.add(callback);
    }

    public void clear() {
        callbacks.clear();
    }

    public void trigger(Signature calledSignature, ArrayList<Object> calledArgumentsWithReturnValue, Object target, Exception ex) {
        callbacks.forEach(callback -> callback.execute(calledSignature, calledArgumentsWithReturnValue, target, ex));
    }
}
