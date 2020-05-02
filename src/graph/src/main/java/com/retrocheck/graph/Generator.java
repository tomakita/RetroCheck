package com.retrocheck.graph;

import java.util.Map;

// Implement this for e.g. jooq classes which need to generate by iterating over the Fields<?> collection, rather than
// by reflection as in DefaultGenerator.
public interface Generator {
    Object generate(Class schema) throws Exception;
    Object generate(Class schema, boolean shouldTryAncestor) throws Exception;
    Map<Class, Unique> getUniquesByType();
    long getSeed();
    long reSeed(long seed);
    Randomizer getRandomizer();
}
