package com.retrocheck.mock;

import java.util.function.Function;

public class Actions {
    public static boolean areMocksEnabled = true;

    private static Function<Class, Object> serviceLocator = schema -> {
        try {
            return schema.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    };

    public static Function<Class, Object> getServiceLocator() {
        return serviceLocator;
    }

    public static void setServiceLocator(Function<Class, Object> serviceLocator) {
        Actions.serviceLocator = serviceLocator;
    }
}
