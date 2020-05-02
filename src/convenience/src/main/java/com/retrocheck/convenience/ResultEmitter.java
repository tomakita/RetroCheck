package com.retrocheck.convenience;

import com.retrocheck.assertion.Actions;

import java.util.function.Function;

public class ResultEmitter {
    public static void connect(String url, boolean isFailureOnlyMode, Function<Class, Object> locator) {
        Actions.setFailureOnlyMode(isFailureOnlyMode);
        Redis_ redis = new Redis_(url);
        Emitter emitter = new Emitter(redis.getSingleton());

        Actions.addExternalTestSuccessEvent((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> emitter.emitSuccess(new AssertionResultStrings(signature, invocationResult.toString(), signatureTypesAndValues.toString(), continuation, isExecutionComplete)));
        Actions.addExternalTestFailureEvent((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> emitter.emitFailure(new AssertionResultStrings(signature, invocationResult.toString(), signatureTypesAndValues.toString(), continuation, isExecutionComplete)));

        Actions.setServiceLocator(schema -> {
            try {
                return locator.apply(schema);
            } catch (Exception e) {
                try {
                    return schema.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    return null;
                }
            }
        });
    }

    public static void connect(String url, boolean isFailureOnlyMode) {
        Actions.setFailureOnlyMode(isFailureOnlyMode);
        Redis_ redis = new Redis_(url);
        Emitter emitter = new Emitter(redis.getSingleton());

        Actions.addExternalTestSuccessEvent((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> emitter.emitSuccess(new AssertionResultStrings(signature, invocationResult.toString(), signatureTypesAndValues.toString(), continuation, isExecutionComplete)));
        Actions.addExternalTestFailureEvent((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> emitter.emitFailure(new AssertionResultStrings(signature, invocationResult.toString(), signatureTypesAndValues.toString(), continuation, isExecutionComplete)));
    }
}
