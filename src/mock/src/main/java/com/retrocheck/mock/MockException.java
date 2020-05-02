package com.retrocheck.mock;

public class MockException extends Exception {
    public String signature;
    public Object[] parametersWithValues;

    public MockException(String signature, Object[] parametersWithValues, Exception ex) {
        super(ex);
        this.signature = signature;
        this.parametersWithValues = parametersWithValues;
    }
}
