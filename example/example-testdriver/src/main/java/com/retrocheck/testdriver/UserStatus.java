package com.retrocheck.testdriver;

import java.time.Instant;

public class UserStatus {
    private Integer userId;
    private boolean isLoggedIn;
    private Instant timestamp;

    public UserStatus(int userId, boolean isLoggedIn) {
        this.userId = userId;
        this.isLoggedIn = isLoggedIn;
    }

    public UserStatus(int userId, boolean isLoggedIn, Instant timestamp) {
        this.userId = userId;
        this.isLoggedIn = isLoggedIn;
        this.timestamp = timestamp;
    }

    public Integer getUserId() {
        return userId;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public UserStatus withId(int id) {
        userId = id;
        return this;
    }

    @Override
    public String toString() {
        return "(" + userId + ", " + isLoggedIn + ", " + timestamp + ")";
    }
}