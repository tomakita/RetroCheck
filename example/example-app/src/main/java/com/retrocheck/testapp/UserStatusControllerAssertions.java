package com.retrocheck.testapp;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class UserStatusControllerAssertions {

    private MemcachedClient memcached;

    public UserStatusControllerAssertions(@Autowired MemcachedClient memcached) {
        // You can use objects from Spring's IoC container in assertion code.
        this.memcached = memcached;
    }

    // This is an assertion for the UserStatusController.userStatus method.
    // It returns a boolean, and has access to UserStatusController.userStatus's
    // argument list, as well as the value it returned, and the UserStatusController
    // instance on which it was invoked.
    // Assertions are invoked by RetroCheck as the system under test runs, and their
    // return values are sent to Redis for use by test running code.
    public boolean userStatus(Integer userId,
                              UserStatus result,
                              UserStatusController instance)
            throws InterruptedException, MemcachedException, TimeoutException {

        System.out.println("Running assertion for user id = " + userId);

        // We check to see if the user has a status in memcached.  If it does,
        // we make sure that status matches what is being returned by /userstatus.
        // Otherwise, we make sure that /userstatus is returning null.
        Boolean isLoggedIn  = memcached.get(userId.toString());
        if (isLoggedIn != null) {
            return result.isLoggedIn() == isLoggedIn;
        } else {
            return result == null;
        }
    }
}
