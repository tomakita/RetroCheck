package com.retrocheck.graph;

import java.util.UUID;

public class Entity<T> {
    private Class schema;
    private T instance;
    private String instanceName;
    private String loaderName;
    private boolean isEntryPoint;
    private boolean isTransient;
    private UUID id;

    public Entity(Class schema, T instance, String instanceName, String loaderName, boolean isEntryPoint, boolean isTransient, UUID id) {
        this.schema = schema;
        this.instance = instance;
        this.instanceName = instanceName;
        this.loaderName = loaderName;
        this.isEntryPoint = isEntryPoint;
        this.isTransient = isTransient;
        this.id = id;
    }

    public Class getSchema() {
        return schema;
    }

    public T getInstance() {
        return instance;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getLoaderName() {
        return loaderName;
    }

    public boolean isEntryPoint() {
        return isEntryPoint;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public UUID getId() {
        return id;
    }
}
