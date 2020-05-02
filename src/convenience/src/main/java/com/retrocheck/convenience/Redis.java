package com.retrocheck.convenience;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class Redis {
    private static final String DEFAULT_ADDRESS = "redis://127.0.0.1:6379";
    private RedissonClient client;
    private RTopic successTopic;
    private RTopic failureTopic;
    private RTopic invocationCompletionTopic;
    private static final String SUCCESS_CHANNEL_NAME = "pbm_on_assertion_success";
    private static final String FAILURE_CHANNEL_NAME = "pbm_on_assertion_failure";
    private static final String INVOCATION_COMPLETION_CHANNEL_NAME = "pbm_on_invocation_completion";

    // note that this does couple this class to redis, but only this class.
    // we don't force people to use redis by doing this -- they still have the flexibility
    // to do whatever they like.  this class is just a convenience for those who are ok
    // with using redis.

    // if you want to make initialization of this class more flexible, use builder pattern.

    public Redis() {
        Config config = createConfig(DEFAULT_ADDRESS);
        connect(config);
    }

    public Redis(String address) {
        Config config = createConfig(address);
        connect(config);
    }

    private Config createConfig(String address) {
        Config config = new Config();
        config.useSingleServer().setAddress(address);
        return config;
    }

    private void connect(Config config) {
        client = Redisson.create(config);
        successTopic = client.getTopic(SUCCESS_CHANNEL_NAME);
        failureTopic = client.getTopic(FAILURE_CHANNEL_NAME);
        invocationCompletionTopic = client.getTopic(INVOCATION_COMPLETION_CHANNEL_NAME);
    }

    public RedissonClient getClient() {
        return client;
    }

    public RTopic getSuccessTopic() {
        return successTopic;
    }

    public RTopic getFailureTopic() {
        return failureTopic;
    }

    public RTopic getInvocationCompletionTopic() {
        return invocationCompletionTopic;
    }

    public String getSuccessChannelName() {
        return SUCCESS_CHANNEL_NAME;
    }

    public String getFailureChannelName() {
        return FAILURE_CHANNEL_NAME;
    }

    public String getInvocationCompletionChannelName() {
        return INVOCATION_COMPLETION_CHANNEL_NAME;
    }

    public <V> void put(String key, V value) {
        client.getBucket(key).set(value);
    }

    public <V> V get(String key) {
        RBucket<V> bucket = client.getBucket(key);
        return bucket.get();
    }

    public void delete(String key) {
        client.getBucket(key).delete();
    }

    public void destroy() {
        client.shutdown();
    }

//    public Listener withSuccessChannelName(String successChannelName) {
//        throwIfAlreadyInitialized();
//        this.successChannelName = successChannelName;
//        return this;
//    }
//
//    public Listener withFailureChannelName(String failureChannelName) {
//        throwIfAlreadyInitialized();
//        this.failureChannelName = failureChannelName;
//        return this;
//    }
//
//    public Listener withInvocationCompletionChannelName(String invocationCompletionChannelName) {
//        throwIfAlreadyInitialized();
//        this.invocationCompletionChannelName = invocationCompletionChannelName;
//        return this;
//    }

//    // so i can be lazy and (for now) avoid having to write a deepcopy method for AssertionEvent
//    private boolean isAlreadyInitialized() {
//        return onAssertionFailure.size() > 0 || onAssertionSuccess.size() > 0 || onInvocationCompletion.size() > 0;
//    }
//
//    private void throwIfAlreadyInitialized() {
//        if (isAlreadyInitialized()) {
//            throw new IllegalStateException("This listener is already initialized, and cannot be built any further.");
//        }
//    }
}
