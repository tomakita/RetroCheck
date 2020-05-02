package com.retrocheck.assertion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD })
// Marker annotation that denotes an assertion which doesn't completely specify correctness for the
// function with which it's associated.
public @interface Incomplete {
}
