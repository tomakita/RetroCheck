package com.retrocheck.convenience;

import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

public class Redis_ {
    private static Redis singleton;

    public Redis_() {
        if (singleton == null) {
            singleton = new Redis();
        }
    }

    public Redis_(String address) {
        if (singleton == null) {
            singleton = new Redis(address);
        }
    }

    public Redis getSingleton() {
        return singleton;
    }

    public RedissonClient getClient() {
        return singleton.getClient();
    }

    public RTopic getSuccessTopic() {
        return singleton.getSuccessTopic();
    }

    public RTopic getFailureTopic() {
        return singleton.getFailureTopic();
    }

    public RTopic getInvocationCompletionTopic() {
        return singleton.getInvocationCompletionTopic();
    }

    public <V> void put(String key, V value) {
        singleton.getClient().getBucket(key).set(value);
    }

    public <V> V get(String key) {
        RBucket<V> bucket = singleton.getClient().getBucket(key);
        return bucket.get();
    }

    public void delete(String key) {
        singleton.getClient().getBucket(key).delete();
    }
}
