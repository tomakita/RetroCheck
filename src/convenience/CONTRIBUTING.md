## Sharp Edges

- It's important to discourage consumers of retrocheck.convenience from creating multiple connections to Redis.  This is currently done with the `Redis_` class, which is a singleton wrapper around the `Redis` class.  This probably isn't the best way of accomplishing this.  

## Planned Improvements

- Redis is used for both messaging and key-value storage.  If there are better solutions to these two problems (e.g. RabbitMQ for messaging and Memcached for key-value storage), then it may be worth exploring them.  This would replace Redis with two other dependencies, but my thinking is that messaging and key-value storage won't both be used by all users -- messaging will probably be used far more commonly than key-value storage.  So even though there would be two dependencies, many users might only need to use/have one of them.  

Note: I've thought about using ZeroMQ for this, since it's brokerless and would make it so the user doesn't need to worry about having e.g. Redis available, but ZeroMQ would require that the entire system under test be on the same network as the test runner (since all components under test would be sending messages directly to the test runner).  That might not be possible for some users, and it also violates our principle of requiring almost no changes to the system under test.

- Generator, uniqueness, and data loading code be moved to a new `retrocheck.data` lib.