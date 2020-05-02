# RetroCheck

![Release](https://github.com/tomakita/retrocheck/workflows/Release/badge.svg)

RetroCheck is a tool that makes it easy to test systems that would otherwise be hard or impossible to write automated tests for.  When testing a system using RetroCheck, you specify a data model and correctness properties for your system, and RetroCheck generates instances of that data model and uses them to exercise your system and check the correctness properties that you specified.

## Goals

- Make automated end-to-end testing of distributed systems significantly easier.
- Test systems that are "untestable": test systems without refactoring them in order to make them "testable".  RetroCheck requires almost no changes to the system under test.
- Test systems that operate asynchronously and/or recursively.
- Monitor systems in production using the same assertions used in tests.
- Reuse the same data model for all tests, rather than writing test data by hand for each test.
- Exercise all assertions of correctness in the same test, so that interactions between functions and services can be tested.

## Definitions

Here are definitions that will help to understand the rest of this document.

*System*: The distributed system under test. 

*Function*: A constructor or method in the system.

*Assertion*: Code that specifies the correctness of a function.

*Entity*: A thing that contains data.  E.g. a database table or a message on a message queue.

*Constraint*: A way in which two entities must be related.  E.g. a foreign key constraint, or the expectation that a key in a HTTP request payload be present in some entity.

*Data model*: A collection of entities and the constraints between them.

*Mock*: Code that runs in the place of some other code.

## Features

RetroCheck provides libraries for:

- Associating assertions with functions.  RetroCheck injects the bytecode for assertions immediately after the bytecode for the functions with which they are associated, such that the assertion for a function *f* is executed immediately after *f*, for all executions of *f*.
- Mocking calls to services which are external to the system.
- Taking actions based upon the results of assertions.
- Generating instances of entities in such a way that the constraints between all entities are satisfied.
- Specifying constraints on entities, and expressing constraints and entities as part of the complete data model of a system.
- Visualizing a system's data model.
- Loading and unloading a system's data model into and out of its corresponding data stores, in an order such that all constraints (e.g. foreign key constraints) are satisfied.
- Emitting assertion events that can be consumed by test runners.
- Knowing when a test case is over without polling or timeouts, even when testing asynchronous systems.

Usage of RetroCheck isn't "all or nothing" -- most of these features can be opted into.

## Example

There's a runnable example in [/example](https://github.com/tomakita/retrocheck/tree/master/example#example).

## Implementation Details

RetroCheck is implemented as four Java libraries:

### [retrocheck.assertion](https://github.com/tomakita/retrocheck/tree/master/src/assertion#retrocheck.assertion)

Uses AspectJ to associate assertions with functions, and to execute those assertions.  Supports service location, for integration with things like Spring's IoC container.

Builds with Maven.

### [retrocheck.mock](https://github.com/tomakita/retrocheck/tree/master/src/mock#retrocheck.mock)

Uses AspectJ to replace calls to network dependencies with calls to arbitrary, user-defined functions.

Builds with Maven.

### [retrocheck.graph](https://github.com/tomakita/retrocheck/tree/master/src/graph#retrocheck.graph)

Expresses entities, constraints, and data models.  Loads and unloads data models into and out of data stores.  Visualizes data models using cytoscape.js.

Builds with Gradle.

### [retrocheck.convenience](https://github.com/tomakita/retrocheck/tree/master/src/convenience#retrocheck.convenience)

Conveniently configures assertions and test runners to interact with Redis.  Conveniently generates entity values.  Conveniently orchestrates tests: starts and stops tests, and loads and unloads data models from data stores automatically as tests start and stop.

Builds with Gradle.

### Dependencies

- retrocheck.assertion: AspectJ
- retrocheck.mock: AspectJ
- retrocheck.graph: Jackson (for graph visualization), Cytoscape.js (for graph visualization)
- retrocheck.convenience: Redis (for emitting assertion results to test runners, and for mock data storage), Redisson, retrocheck.assertion, junit-quickcheck (for entity generation), Awaitility (for waiting for tests to complete)

All libraries require JRE 1.8 or above.

## Contributing

Contributions are welcome!  See [CONTRIBUTING](https://github.com/tomakita/retrocheck/blob/master/CONTRIBUTING.md) for notes on contributing, as well as a list of issues that might be a good starting point, if you'd like to get started as a contributor.  For large changes, please open an issue to discuss the change before writing any code.  Thanks for your contributions!