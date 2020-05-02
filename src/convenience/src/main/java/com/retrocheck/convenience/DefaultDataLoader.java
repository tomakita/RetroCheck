package com.retrocheck.convenience;

import com.retrocheck.graph.Entity;
import com.retrocheck.graph.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

// This is the class that does orchestration for you -- you can also not use this class,
// and do all of this manually, if this class isn't expressive enough.
public class DefaultDataLoader implements DataLoader {
    private Map<String, Function<Object, Object>> loader;
    private Map<String, Function<Object, Object>> unloader;
    private Map<String, Runnable> truncater;
    private boolean isComplete; // not method-local b/c it's used in a lambda
    private String outcome; // not method-local b/c it's used in a lambda
    List<AssertionResultStrings> failures = new ArrayList<>();
    private long timeoutMillis = 60000;
    private Redis redis;
    private RedisListener listener;
    private boolean isListening = false;
    private boolean persistOnFailure = false;

    public DefaultDataLoader(Map<String, Function<?, ?>> loader, Map<String, Function<?, ?>> unloader) {
        makeLoadersLessTypeSafe(loader, unloader);
    }

    public DefaultDataLoader(Map<String, Function<?, ?>> loader, Map<String, Function<?, ?>> unloader, Map<String, Runnable> truncater) {
        makeLoadersLessTypeSafe(loader, unloader);
        this.truncater = truncater;
    }

    public DefaultDataLoader(Map<String, Function<?, ?>> loader, Map<String, Function<?, ?>> unloader, Redis redis) {
        makeLoadersLessTypeSafe(loader, unloader);
        this.redis = redis;
        listener = new RedisListener(redis);
        listener.start();
    }

    public DefaultDataLoader(Map<String, Function<?, ?>> loader, Map<String, Function<?, ?>> unloader, Map<String, Runnable> truncater, Redis redis) {
        makeLoadersLessTypeSafe(loader, unloader);
        this.truncater = truncater;
        this.redis = redis;
        listener = new RedisListener(redis);
        listener.start();
    }

    private void makeLoadersLessTypeSafe(Map<String, Function<?, ?>> loader, Map<String, Function<?, ?>> unloader) {
        this.loader = loader.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kvp -> (Function<Object, Object>)kvp.getValue()));
        this.unloader = unloader.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kvp -> (Function<Object, Object>)kvp.getValue()));;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setPersistOnFailure(boolean persistOnFailure) {
        this.persistOnFailure = persistOnFailure;
    }

    @Override
    public TestResult orchestrate(Workflow workflow) {
        return orchestrateWithListener(workflow, listener, () -> start(workflow));
    }

    public TestResult orchestrateWithListener(Workflow workflow, Listener listener, Runnable starter) {
        TestResult result = new TestResult(failures, workflow.getSeed());
        failures.clear();

        insertSafely(workflow);

        outcome = workflow.getOutcome().getName();
        if (!isListening) {
            listener.getAssertionSuccessEvent().add(assertionResult -> {
                String eventName = assertionResult.getMethodName();

                if (assertionResult.isExecutionComplete()) {
                    isComplete = true;
                } else if (eventName.equals(outcome)) {
                    outcome = assertionResult.getContinuation();
                    if (outcome == null) {
                        isComplete = true;
                    } else {
                        outcome = assertionResult.getContinuation();
                    }
                }
            });
            listener.getAssertionFailureEvent().add(assertionResult -> {
                String eventName = assertionResult.getMethodName();

                failures.add(assertionResult);

                if (assertionResult.isExecutionComplete()) {
                    isComplete = true;
                } else if (eventName.equals(outcome)) {
                    outcome = assertionResult.getContinuation();
                    if (outcome == null) {
                        isComplete = true;
                    } else {
                        outcome = assertionResult.getContinuation();
                    }
                }
            });
            listener.getInvocationCompletionEvent().add(assertionResult -> {
                isComplete = true;
            });

            isListening = true;
        }

        starter.run();

        if (!workflow.getOutcome().isCompleteImmediately()) {
            await().atMost(Duration.ofMillis(timeoutMillis)).until(() -> isComplete);
        }

        isComplete = false; // this can't be a method-local var, b/c it's used in a lambda.
        boolean isCompleteSuccess = failures.size() == 0;
        if (!(persistOnFailure && !isCompleteSuccess)) {
            delete(workflow);
        }

        return result;
    }

    @Override
    public void start(Workflow workflow) {
        List<Entity<Object>> entryPoints = workflow.getDataSetup().stream().filter(entity -> entity.isEntryPoint()).collect(Collectors.toList());
        for (Entity<Object> entity : entryPoints) {
            loader.get(entity.getLoaderName()).apply(entity.getInstance());
        }
    }

    @Override
    public void insert(Workflow workflow) {
        List<Entity<Object>> nonEntryPoints = workflow.getDataSetup().stream().filter(entity -> !entity.isEntryPoint()).collect(Collectors.toList());
        for (Entity<Object> entity : nonEntryPoints) {
            loader.get(entity.getLoaderName()).apply(entity.getInstance());
        }
    }

    private void insertSafely(Workflow workflow) {
        // If any errors happen when we load data, we do our best to unload
        // that data.
        try {
            insert(workflow);
        } catch (Exception insertionException) {
            System.out.println("Error occurred during data loading:");
            insertionException.printStackTrace();

            try {
                delete(workflow);
            } catch (Exception deletionException) {}
        }
    }

    @Override
    public void delete(Workflow workflow) {
        List<Entity<Object>> nonTransients = workflow.getDataSetup().stream().filter(entity -> !entity.isTransient()).collect(Collectors.toList());
        Collections.reverse(nonTransients);
        Map<String, AtomicLong> entityCountsByLoaderName =
                nonTransients
                        .stream()
                        .collect(Collectors.groupingBy(Entity::getLoaderName, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, kvp -> new AtomicLong(kvp.getValue())));

        for (Entity<Object> entity : nonTransients) {
            String loaderName = entity.getLoaderName();

            if (!unloader.containsKey(loaderName)) {
                throw new RuntimeException("No unloader was found for key " + loaderName + ".  Did you mean to use a transient Node for this entity, instead?");
            }

            unloader.get(loaderName).apply(entity.getInstance());
            entityCountsByLoaderName.get(loaderName).decrementAndGet();

            if (truncater != null && truncater.containsKey(loaderName)) {
                if (entityCountsByLoaderName.get(loaderName).get() == 0) {
                    truncater.get(loaderName).run();
                }
            }
        }
    }

    public void destroy() {
        if (listener != null) {
            listener.destroy();
        }
        if (redis != null) {
            redis.destroy();
        }
    }
}
