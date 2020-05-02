# retrocheck.mock

## Features

- Execute mocks using AspectJ.
- Associate mocks with functions.

## Mocks

Mocks use AspectJ in the same manner as assertions, with the following differences:

- The annotation we use to mark functions to be mocked is called `@MockWith`.
- An assertion executes *after its associated function executes*, whereas a mock executes *instead of its associated function*.
- Mocks are less configurable than assertions (the retrocheck.mock `Actions` class is much smaller than the retrocheck.assertions `Actions` class), though service location is still supported.