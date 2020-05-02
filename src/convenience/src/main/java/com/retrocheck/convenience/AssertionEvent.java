package com.retrocheck.convenience;

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

    public void trigger(AssertionResultStrings result) {
        callbacks.forEach(callback -> callback.execute(result));
    }
}
