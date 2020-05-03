# RetroCheck

![Release](https://github.com/tomakita/retrocheck/workflows/Release/badge.svg)

RetroCheck is a tool that makes it easier to test systems that would otherwise be hard or impossible to write automated tests for.  When testing a system using RetroCheck, you first associate assertions with functions in the system under test, and you then specify a data model for that system.  RetroCheck will then generate instances of that data model and use them to exercise your system and to execute the assertions that you specified.

For those who are familiar with [property-based testing](https://fsharpforfunandprofit.com/posts/property-based-testing/), RetroCheck could be described as property-based testing in which tests are composable, and in which dependent data generation is supported.

## Goals

- Make automated end-to-end testing of distributed systems significantly easier.
- Test systems that are "untestable": test systems without refactoring them in order to make them "testable".  RetroCheck requires almost no changes to the system under test, so it's easy to start using it, and it's also easy to stop using it.
- Test systems that operate asynchronously and/or recursively.
- Generate data satisfying the data model of your application, and reuse the same data model for all tests, rather than writing test data by hand for each test.
- Exercise all assertions of correctness in the same test, so that interactions between functions and services can be tested.
- Monitor systems in production using the same assertions used in tests.

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
- Specifying constraints on entities, and expressing constraints and entities as part of the complete data model of a system.
- Generating instances of entities in such a way that the constraints between all entities are satisfied.
- Loading and unloading a system's data model into and out of its corresponding data stores, in an order such that all constraints (e.g. foreign key constraints) are satisfied.
- Visualizing a system's data model.
- Emitting assertion events that can be consumed by test runners.
- Knowing when a test case is over without polling or timeouts, even when testing asynchronous systems.
- Mocking calls to services which are external to the system.

Usage of RetroCheck isn't "all or nothing" -- most of these features can be opted into.  RetroCheck isn't meant to replace unit or integration testing -- it's just another tool.

## Example

There's a runnable example, along with documentation, in [/example](https://github.com/tomakita/retrocheck/tree/master/example#example).

## Implementation Details

RetroCheck is implemented as four Java libraries:

### [retrocheck.assertion](https://github.com/tomakita/retrocheck/tree/master/src/assertion#retrocheckassertion)

Uses AspectJ to associate assertions with functions, and to execute those assertions.  Supports service location, for integration with things like Spring's IoC container.

### [retrocheck.mock](https://github.com/tomakita/retrocheck/tree/master/src/mock#retrocheckmock)

Uses AspectJ to replace calls to network dependencies with calls to arbitrary, user-defined functions.

### [retrocheck.graph](https://github.com/tomakita/retrocheck/tree/master/src/graph#retrocheckgraph)

Expresses entities, constraints, and data models.  Loads and unloads data models into and out of data stores.  Visualizes data models using cytoscape.js.

### [retrocheck.convenience](https://github.com/tomakita/retrocheck/tree/master/src/convenience#retrocheckconvenience)

Conveniently configures assertions and test runners to interact with Redis.  Conveniently generates entity values.  Conveniently orchestrates tests: starts and stops tests, and loads and unloads data models from data stores automatically as tests start and stop.

### Dependencies

- retrocheck.assertion: AspectJ
- retrocheck.mock: AspectJ
- retrocheck.graph: Jackson (for graph visualization), Cytoscape.js (for graph visualization)
- retrocheck.convenience: Redis (for emitting assertion results to test runners, and for mock data storage), Redisson, retrocheck.assertion, junit-quickcheck (for entity generation), Awaitility (for waiting for tests to complete)

All libraries require JRE 1.8 or above.

## Contributing

Contributions are welcome!  See [CONTRIBUTING](https://github.com/tomakita/retrocheck/blob/master/CONTRIBUTING.md) for notes on contributing, as well as a list of issues that might be a good starting point, if you'd like to get started as a contributor.  For large changes, please open an issue to discuss the change before writing any code.

For bug reports and feature requests, feel free to open an issue.

Thanks for your contributions!