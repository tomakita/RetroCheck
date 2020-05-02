package com.retrocheck.convenience;

public class Emitter {
    private final Redis redis;

    public Emitter(Redis redis) {
        this.redis = redis;
    }

    public void emitSuccess(AssertionResult result) {
        redis.getSuccessTopic().publish(result);
    }

    public void emitFailure(AssertionResult result) {
        redis.getFailureTopic().publish(result);
    }

    public void emitInvocationComplete(AssertionResult result) {
        redis.getInvocationCompletionTopic().publish(result);
    }

    public void emitSuccess(AssertionResultStrings result) {
        redis.getSuccessTopic().publish(result);
    }

    public void emitFailure(AssertionResultStrings result) {
        redis.getFailureTopic().publish(result);
    }

    public void emitInvocationComplete(AssertionResultStrings result) {
        redis.getInvocationCompletionTopic().publish(result);
    }
}
