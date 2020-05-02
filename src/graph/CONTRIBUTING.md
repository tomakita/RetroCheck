## Sharp Edges

The code in this library isn't very good, and could stand to be refactored, particularly in the `Preprocessor` and `Processor` classes, which is where all of the graph traversals are implemented.

## Planned Improvements

- The code that has to do with generators (e.g. `DefaultGenerator` and its associated interfaces) is in need of a refactor.
- `Node<T>` currently supports a notion of constraints (the analog of edge constraints), but I'm not sure why.  I need to remember what this was meant to accomplish, and whether or not this feature should be kept.
- Generator, uniqueness, and data loading code be moved to a new `retrocheck.data` lib.
- The graph visualization currently draws the edge probability on each edge, but it would be nice if it could display the actual constraint lambda code there, instead.  This is (AFAIK) impossible to do in an automated way, so maybe the user will need to pass in a string representation of the lambda to the `Edge<U, V>` constructor?