# retrocheck.convenience

## Goal

One of the overarching goals of RetroCheck is to be unopionated about the way that you use it.  This means that it should be possible to opt into various features of RetroCheck as you want/need to.  Still, it's possible that there is a use-case of RetroCheck that is common to many users, and the retrocheck.convenience library is an attempt at solving for that use-case.  To this end, retrocheck.convenience contains classes and methods that can be used to easily emit (over a network) and listen for assertion success/failure/error events that originate from components of a distributed system.

## Features

- Supplies default implementations of interfaces that are used for testing: `DefaultDataLoader`, `DefaultGenerator`, `DefaultGraph`, `DefaultRandomizer`, and `DefaultTester`
- Creates a connection (one per consumer of retrocheck.convenience) to a Redis cluster (retrocheck.convenience assumes that you have access to a Redis cluster of at least one node).
- Emits assertion results to Redis pub-sub channels (via `Emitter`).  Meant to be used by applications that execute assertions.
- Listens to Redis pub-sub channels (via `Listener`).  Meant to be used by applications that do things with assertion results, such as test runners.
- Configures assertion-executing applications to automatically emit assertion results to Redis pub-sub channels (via `ResultEmitter`).
- Reads and writes key-value pairs to and from Redis.

All Redis interactions are implemented with Redisson.