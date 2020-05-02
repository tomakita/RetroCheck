package com.retrocheck.convenience;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.internal.generator.EnumGenerator;
import com.pholser.junit.quickcheck.internal.generator.SimpleGenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.retrocheck.graph.Generator;
import com.retrocheck.graph.Randomizer;
import com.retrocheck.graph.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class DefaultGenerator implements Generator {
    private DefaultRandomizer randomizer = new DefaultRandomizer();
    private SourceOfRandomness r = randomizer.getSourceOfRandomness();
    private GeometricDistribution distribution = new GeometricDistribution();
    private GenerationStatus status = new SimpleGenerationStatus(distribution, r, 0);
    private Map<Class, Callable<?>> map = new HashMap<>();
    // eventually will need another unique map that is just row -> set<obj> instead of row,col -> set<obj>,
    // for types which have no properties, or for which we don't care about generating their individual properties.
    // this will require a new type that's like Unique, i think.
    // ^^^ this will just be for convenience, really.
    private Map<Class, Unique> uniquesByType = new HashMap<>();

    public DefaultGenerator() {
        CustomStringGenerator customStringGenerator = new CustomStringGenerator();

        map.put(Integer.class, () -> r.nextInt());
        map.put(int.class, () -> r.nextInt());
        map.put(Boolean.class, () -> r.nextBoolean());
        map.put(boolean.class, () -> r.nextBoolean());
        map.put(Long.class, () -> r.nextLong());
        map.put(long.class, () -> r.nextLong());
        map.put(Float.class, () -> r.nextFloat());
        map.put(float.class, () -> r.nextFloat());
        map.put(Double.class, () -> r.nextDouble());
        map.put(double.class, () -> r.nextDouble());
        map.put(Short.class, () -> r.nextShort(Short.MIN_VALUE, Short.MAX_VALUE));
        map.put(short.class, () -> r.nextShort(Short.MIN_VALUE, Short.MAX_VALUE));
        map.put(BigInteger.class, () -> r.nextBigInteger(32));
        map.put(BigDecimal.class, () -> BigDecimal.valueOf(r.nextDouble()));
        map.put(Character.class, () -> r.nextChar(Character.MIN_VALUE, Character.MAX_VALUE));
        map.put(char.class, () -> r.nextChar(Character.MIN_VALUE, Character.MAX_VALUE));
        map.put(Byte.class, () -> r.nextByte((byte) -128, (byte) 127));
        map.put(byte.class, () -> r.nextByte((byte) -128, (byte) 127));
        map.put(Byte[].class, () -> r.nextBytes(r.nextInt(0, 64)));
        map.put(byte[].class, () -> r.nextBytes(r.nextInt(0, 64)));
        map.put(String.class, () -> customStringGenerator.generate(r, status));
        map.put(Timestamp.class, () -> new Timestamp(r.nextInstant(Instant.MIN, Instant.MAX).getNano()));
        map.put(Duration.class, () -> Duration.of(r.nextLong(), ChronoUnit.MILLIS));
        map.put(java.util.Date.class, () -> new java.util.Date(r.nextInstant(Instant.MIN, Instant.MAX).getNano()));
        map.put(java.sql.Date.class, () -> new java.sql.Date(r.nextInstant(Instant.MIN, Instant.MAX).getNano()));
    }

    private DefaultGenerator(Map<Class, Callable<?>> map) {
        this.map = map;
    }

    private DefaultGenerator(long seed, Map<Class, Callable<?>> map) {
        reSeed(seed);
        this.map = map;
    }

    public long getSeed() {
        return r.seed();
    }

    public long reSeed(long seed) {
        r = new SourceOfRandomness(new Random());
        r.setSeed(seed);
        distribution = new GeometricDistribution();
        status = new SimpleGenerationStatus(distribution, r, 0);
        randomizer = new DefaultRandomizer(r);
        uniquesByType = new HashMap<>();

        return r.seed();
    }

    public DefaultGenerator withSeed(long seed) {
        return new DefaultGenerator(seed, map);
    }

    @Override
    public Randomizer getRandomizer() {
        return randomizer;
    }

    @Override
    public Map<Class, Unique> getUniquesByType() {
        return uniquesByType;
    }

    @Override
    public Object generate(Class schema) throws Exception {
        if (map.containsKey(schema)) {
            return map.get(schema).call();
        } else if (Enum.class.isAssignableFrom(schema)) {
            return new EnumGenerator(schema).generate(r, status);
        } else {
            // TODO: this call to newInstance() will only work for classes with a nullary ctor.
            //       need to make this work even if a class doesn't have one of those.
            Object instance = schema.newInstance();
            Field[] fields = schema.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                field.setAccessible(true);
                Class<?> classOfField = field.getClass();
                Object generatedValueOfField = generate(classOfField);
                field.set(instance, generatedValueOfField);
            }

            return instance;
        }
    }

    @Override
    public Object generate(Class schema, boolean shouldTryAncestor) throws Exception {
        Map<Class, Callable<?>> ancestors = map.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(schema)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        boolean ancestorsExist = ancestors.size() > 0;

        if (map.containsKey(schema)) {
            return map.get(schema).call();
        } else if (ancestorsExist) {
            return map.get(ancestors.values().iterator().next()).call();
        } else if(Enum.class.isAssignableFrom(schema)) {
            return new EnumGenerator(schema).generate(r, status);
        } else {
            // TODO: this call to newInstance() will only work for classes with a nullary ctor.
            //       need to make this work even if a class doesn't have one of those.
            Object instance = schema.newInstance();
            Field[] fields = schema.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                Class<?> classOfField = field.getClass();
                Object generatedValueOfField = generate(classOfField);
                field.set(instance, generatedValueOfField);
            }

            return instance;
        }
    }

    public DefaultGenerator with(Class schema, Callable<?> generatorFunction) {
        DefaultGenerator generator = new DefaultGenerator();
        generator.map.put(schema, generatorFunction);
        return generator;
    }

    public DefaultGenerator with(Class schema, Generatable<?> generatorFunction) {
        DefaultGenerator generator = new DefaultGenerator(map);
        generator.map.put(schema, () -> generatorFunction.arbitrary(r, status));
        return generator;
    }

    public DefaultGenerator withUnique(Class schema, UniqueGeneratable<?> generatorFunction) {
        DefaultGenerator generator = new DefaultGenerator(map);

        uniquesByType.putIfAbsent(schema, new Unique());

        generator.map.put(schema, () -> generatorFunction.arbitrary(r, status, uniquesByType.get(schema)));
        return generator;
    }

    // Should be called before any generation has happened, though it doesn't have to be.
    public static Randomizer unify(List<Generator> generators) {
        return unify(generators, new Random().nextLong());
    }

    // Should be called before any generation has happened, though it doesn't have to be.
    public static Randomizer unifyWithSeed(List<Generator> generators, long seed) {
        return unify(generators, seed);
    }

    public static Randomizer unify(List<Generator> generators, long seed) {
        // Use the same Random instance across all generators, so that they all have the same seed.
        for (Generator generator : generators) {
            generator.reSeed(seed);
        }

        // For each generator, for each class, merge uniques, so that each generator has the same unique for each type
        Map<Class, List<Unique>> uniqueListsByType = new HashMap<>();
        for (Generator generator : generators) {
            Map<Class, Unique> uniques = generator.getUniquesByType();
            for (Map.Entry<Class, Unique> uniqueForType : uniques.entrySet()) {
                Class key = uniqueForType.getKey();
                Unique value = uniqueForType.getValue();
                if (uniqueListsByType.containsKey(key)) {
                    uniqueListsByType.get(key).add(value);
                } else {
                    uniqueListsByType.put(key, new ArrayList<>(Arrays.asList(value)));
                }
            }
        }

        // Now unify the unique list for each type
        for (Map.Entry<Class, List<Unique>> uniqueListForType : uniqueListsByType.entrySet()) {
            Unique.unify(uniqueListForType.getValue());
        }

        return generators.iterator().next().getRandomizer();
    }
}
