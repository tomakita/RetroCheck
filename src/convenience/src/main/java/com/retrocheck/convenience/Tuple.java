package com.retrocheck.convenience;

import java.io.Serializable;

// because java was created in the 16th century, i have to make my own tuple type.
// yes, there is javafx.util.Pair, but javafx isn't in openjdk 8.
public class Tuple<T, U> implements Serializable {
    private T first;
    private U second;

    public Tuple(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}
