package com.retrocheck.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: make it so you can also apply this at the class level

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MockWith {
    public Class value();
}
