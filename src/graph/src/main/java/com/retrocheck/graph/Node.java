package com.retrocheck.graph;

import java.util.UUID;
import java.util.function.Function;

public class Node<T> {
    private UUID id;
    private String name;
    private Class<T> entitySchema;
    private T entityInstance;
    private Function<T, T> refinement;
    private Generator generator;
    private String dataLoaderName;
    private boolean isEntryPoint;
    private boolean isTransient;
    private int probability;
    private String subgraphName = "ROOT";

    public Node(String name, Class<T> entitySchema, Generator generator, String dataLoaderName) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.generator = generator;
        this.refinement = x -> x;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = false;
        this.probability = 100;
        this.isTransient = false;
    }

    public Node(String name, Class<T> entitySchema, Generator generator, String dataLoaderName, boolean isEntryPoint) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.generator = generator;
        this.refinement = x -> x;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = isEntryPoint;
        this.probability = 100;
        this.isTransient = false;
    }

    public Node(String name, Class<T> entitySchema, Generator generator, String dataLoaderName, boolean isEntryPoint, Probability probability) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.generator = generator;
        this.refinement = x -> x;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = isEntryPoint;
        this.probability = probability.getValue();
        this.isTransient = false;
    }

    public Node(String name, Class<T> entitySchema, Generator generator, String dataLoaderName, boolean isEntryPoint, Probability probability, boolean isTransient) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.generator = generator;
        this.refinement = x -> x;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = isEntryPoint;
        this.probability = probability.getValue();
        this.isTransient = isTransient;
    }

    public Node(String name, Class<T> entitySchema, Function<T, T> refinement, Generator generator, String dataLoaderName, Probability probability, boolean isTransient) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.refinement = refinement;
        if (refinement == null) {
            this.refinement = x -> x;
        }
        this.generator = generator;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = false;
        this.probability = probability.getValue();
        this.isTransient = isTransient;
    }

    public Node(String name, Class<T> entitySchema, Function<T, T> refinement, Generator generator, String dataLoaderName, boolean isEntryPoint, Probability probability, boolean isTransient) {
        this.name = name;
        this.entitySchema = entitySchema;
        this.refinement = refinement;
        if (refinement == null) {
            this.refinement = x -> x;
        }
        this.generator = generator;
        id = UUID.randomUUID();
        this.dataLoaderName = dataLoaderName;
        this.isEntryPoint = isEntryPoint;
        this.probability = probability.getValue();
        this.isTransient = isTransient;
    }

    public Generator getGenerator() {
        return generator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubgraphName() {
        return subgraphName;
    }

    public void setSubgraphName(String subgraphName) {
        this.subgraphName = subgraphName;
    }

    public T getEntityInstance() {
        return entityInstance;
    }

    public void setEntityInstance(T entityInstance) {
        this.entityInstance = entityInstance;
    }

    
    public void refine() {
        // TODO: it seems like this class really only needs to know about generators.get(entitySchema),
        //       and not the entire generators map...
        try {
            Object generated = generator.generate(entitySchema);
            entityInstance = entitySchema.cast(generated);
        } catch (Exception e) {
            e.printStackTrace();
        }

        entityInstance = refinement.apply(entityInstance);
    }

    public static <T> Node<T> getNull() {
        return new Node<>("_", null, null, null, false);
    }

    public void nullify() {
        this.name = "_";
        this.isEntryPoint = false;
        this.probability = 100;
        this.refinement = x -> x;
    }

    public Node<T> copy() {
        Node<T> copy = new Node<>(this.name, this.entitySchema, this.refinement, this.generator, this.dataLoaderName, this.isEntryPoint, new Probability(this.probability), this.isTransient);
        copy.subgraphName = this.subgraphName;
        copy.id = this.id;
        return copy;
    }

    public Node<T> copyWithNewId() {
        Node<T> copy = new Node<>(this.name, this.entitySchema, this.refinement, this.generator, this.dataLoaderName, this.isEntryPoint, new Probability(this.probability), this.isTransient);
        return copy;
    }


    public boolean isEntryPoint() {
        return isEntryPoint;
    }


    public Entity<Object> export() {
        return new Entity<>(entitySchema, entityInstance, name, dataLoaderName, isEntryPoint, isTransient, id);
    }


    public String toString() {
        return name;
    }


    public UUID identify() {
        return id;
    }


    public Edge<T, Object> terminate(int terminationProbability) {
        return new Edge<>(this, getNull(), (u, v) -> v, Probability.ALWAYS, Edge.NULL_SET_ID);
    }


    public int probability() {
        return probability;
    }
}
