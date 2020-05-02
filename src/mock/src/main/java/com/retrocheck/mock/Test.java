package com.retrocheck.mock;

// TODO: DELETE THIS FILE EVENTUALLY; JUST HERE FOR TESTING

public class Test {
    private int y = 5;

    public Test() {}

    @MockWith(TestMock.class)
    public void write() {
        System.out.println("Test");
    }
}
