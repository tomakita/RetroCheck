# retrocheck.assertion

## Definitions

*Action*: Code that runs after an assertion succeeds, fails, or throws an exception.
*Aspect code*: Code that is internal to the retrocheck.assertion library, which locates and executes assertions.

## Features

- Execute assertions using AspectJ.
- Associate assertions with functions.
- Define actions that are taken after assertions succeed, fail, or throw exceptions.

## Assertions

Assertions are injected into the bytecode of the system under test using AspectJ's compile-time weaving.

Assertions are associated with functions via the `@MonitorWith` (or `@ExceptionOnlyMonitorWith`, for assertions which are meant to handle exceptions thrown by functions) annotation, which has a single parameter of type Class.  This parameter represents the class which contains the assertion which should execute following the execution of the annotated function.  `@MonitorWith` can be applied to methods and constructors.

When the aspect code detects the execution of a function which has been annotated with `@MonitorWith` (or `@ExceptionOnlyMonitorWith`), it performs service location.  Service location is implemented as a user-specifiable lambda.  By default, service location is just a reflective lookup of the class passed to the `@MonitorWith` annotation, followed by a lookup of the name of the function which was annotated.

If service location succeeds, the class and method that were located are memoized for later use, and then executed.  The result of the assertion is then passed to `AssertionEvent` instances, for handling by the user.

In the event that the (attempted) execution of an assertion results in an exception or error, it is logged (to std out) and swallowed by the assertion execution framework, so that execution of system code is not affected.

## Actions

Actions are implemented by the `AssertionCallback` class.  When an assertion succeeds, fails, or throws an exception, a corresponding `AssertionEvent` is triggered, which executes user-specified `AssertionCallback` instances.  These `AssertionCallback` instances can do things such as write logs or emit events for use by a test runner.