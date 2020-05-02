package com.retrocheck.convenience;

public class RedisListener implements Listener {
    private AssertionEvent onAssertionSuccess = new AssertionEvent();
    private AssertionEvent onAssertionFailure = new AssertionEvent();
    private AssertionEvent onInvocationCompletion = new AssertionEvent();

    private final Redis redis;

    @Override
    public AssertionEvent getAssertionSuccessEvent() {
        return onAssertionSuccess;
    }

    @Override
    public AssertionEvent getAssertionFailureEvent() {
        return onAssertionFailure;
    }

    @Override
    public AssertionEvent getInvocationCompletionEvent() {
        return onInvocationCompletion;
    }

    public RedisListener(Redis redis) {
        this.redis = redis;
    }

    @Override
    public void start() {
        redis.getSuccessTopic().addListener(AssertionResultStrings.class, (channel, msg) -> {
            onAssertionSuccess.trigger(msg);
        });

        redis.getFailureTopic().addListener(AssertionResultStrings.class, (channel, msg) -> {
            onAssertionFailure.trigger(msg);
        });

        redis.getInvocationCompletionTopic().addListener(AssertionResultStrings.class, (channel, msg) -> {
            onInvocationCompletion.trigger(msg);
        });
    }

    @Override
    public void destroy() {
        redis.getSuccessTopic().removeAllListeners();
        redis.getFailureTopic().removeAllListeners();
        redis.getInvocationCompletionTopic().removeAllListeners();
    }
}
