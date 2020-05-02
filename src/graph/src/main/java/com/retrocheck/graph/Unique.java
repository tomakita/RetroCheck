package com.retrocheck.graph;

import java.util.*;
import java.util.concurrent.Callable;

// Track and generate values for all properties of some type.
// This obviously won't work for gigantic value sets, since memory will be a problem.
public class Unique {
    private Map<String, Set<Object>> usedElementsByProperty = new HashMap<>();

    public Unique() {}

    public Map<String, Set<Object>> getUsedElementsByProperty() {
        return usedElementsByProperty;
    }

    public <U> U compute(String propertyName, Callable<U> f) {
        try {
            return uniquely(propertyName, f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <U> U uniquely(String propertyName, Callable<U> adHocGenerator) {
        try {
            Object candidate = adHocGenerator.call();
            return ensureUniqueness(candidate, propertyName, adHocGenerator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <U> U ensureUniqueness(Object candidate, String propertyName, Callable<U> adHocGenerator) throws Exception {
        if (!usedElementsByProperty.containsKey(propertyName)) {
            usedElementsByProperty.put(propertyName, new HashSet<>());
        }

        int attemptCount = 0;
        int attemptLimit = 1000; // chosen arbitrarily
        while (usedElementsByProperty.get(propertyName).contains(candidate)) {
            if (attemptCount > attemptLimit) {
                throw new RuntimeException("It may be impossible to generate a unique value for " + propertyName + ".");
            }

            candidate = adHocGenerator.call();

            attemptCount++;
        }

        usedElementsByProperty.get(propertyName).add(candidate);
        return (U)candidate;
    }

    public static void unify(List<Unique> uniques) {
        Map<String, Set<Object>> masterUsedElementsByProperty = new HashMap<>();
        for (Unique unique : uniques) {
            masterUsedElementsByProperty.putAll(unique.getUsedElementsByProperty());
        }

        for (Unique unique : uniques) {
            for (Map.Entry<String, Set<Object>> masterUsedElementsForProperty : masterUsedElementsByProperty.entrySet()) {
                unique.getUsedElementsByProperty().replace(masterUsedElementsForProperty.getKey(), masterUsedElementsForProperty.getValue());
            }
        }
    }
}
