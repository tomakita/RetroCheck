package com.retrocheck.assertion;

import java.util.ArrayList;
import java.util.List;

// TODO: implement `map: ParameterContainer<T> -> ParameterContainer<U>`
public class ParameterContainer<T> {
    private T thing;
    // The order of the elements in this list is important, as it determines
    // the order in which arguments are passed to the method represented by
    // this.method.
    private List<ParameterKind> parameterMetadata = new ArrayList<>();

    public ParameterContainer(T thing) {
        this.thing = thing;
    }

    public ParameterContainer(T thing, List<ParameterKind> parameterMetadata) {
        this.thing = thing;
        this.parameterMetadata = parameterMetadata;
    }

    public T get() {
        return thing;
    }

    public List<ParameterKind> getParameterMetadata() {
        return parameterMetadata;
    }

    public void add(ParameterKind parameterKind) {
        parameterMetadata.add(parameterKind);
    }
}
