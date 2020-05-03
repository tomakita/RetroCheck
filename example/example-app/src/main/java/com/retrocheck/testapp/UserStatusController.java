package com.retrocheck.testapp;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

import com.retrocheck.assertion.MonitorWith;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserStatusController {

    private MemcachedClient memcached;
    private MetricsClient metricsClient = new MetricsClient();

    public UserStatusController(@Autowired MemcachedClient memcached) {
        this.memcached = memcached;
    }

    @RequestMapping("/userstatus")
    // The MonitorWith annotation tells RetroCheck where to find the assertion for
    // the userStats method.
    @MonitorWith(UserStatusControllerAssertions.class)
    public UserStatus userStatus(@RequestParam(value="userId") Integer userId)
            throws InterruptedException, MemcachedException, TimeoutException {

        System.out.println("Serving request for user id = " + userId);

        // Due to mocking, this is actually a call to MockMetricsClient.emit.
        metricsClient.emit();

        return findUserStatus(userId);
    }

    // This method queries for the status of a user, by keying into
    // memcached with the user's ID.  If the user isn't found in memcached,
    // this method returns null.  Otherwise, it returns the user's status
    // wrapped in a UserStatus object.
    private UserStatus findUserStatus(Integer userId)
            throws InterruptedException, MemcachedException, TimeoutException {

        Boolean isLoggedIn  = memcached.get(userId.toString());
        if (isLoggedIn != null) {
            return new UserStatus(userId, isLoggedIn, Instant.now());
        } else {
            return null;
        }
    }
}