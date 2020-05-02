package com.retrocheck.testapp;

import com.retrocheck.convenience.ResultEmitter;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MemcachedConfig {

    public MemcachedConfig(@Autowired ApplicationContext context) {

        // This configures RetroCheck to emit metadata to Redis as assertions
        // succeed and fail.  This metadata will be used by our test driver,
        // which will also be connected to Redis.
        ResultEmitter.connect(
                //"redis://172.17.0.3:6379", // Address of Redis.
                "redis://localhost:6379", // Address of Redis.
                false,
                context::getBean); // Service locator method.
    }

    @Bean
    public MemcachedClient memcached() throws IOException {

        //return new XMemcachedClient("172.17.0.2",11211);
        return new XMemcachedClient("localhost",11211);
    }
}
