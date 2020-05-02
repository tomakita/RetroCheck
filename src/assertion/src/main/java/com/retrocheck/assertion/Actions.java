package com.retrocheck.assertion;

import org.aspectj.lang.Signature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Actions {
    public static boolean areAssertionsEnabled = true;
    private static boolean monitorOnlyMode = false;
    private static boolean testOnlyMode = false;
    private static boolean failureOnlyMode = true;

    private static AssertionEvent testSuccessEvent = new AssertionEvent();
    private static AssertionEvent monitorSuccessEvent = new AssertionEvent();
    private static AssertionEvent testFailureEvent = new AssertionEvent();
    private static AssertionEvent monitorFailureEvent = new AssertionEvent();
    private static AssertionExceptionEvent assertionExceptionEvent = new AssertionExceptionEvent();
    private static GeneralExceptionEvent generalExceptionEvent = new GeneralExceptionEvent();

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

    public static boolean isMonitorOnlyMode() {
        return monitorOnlyMode;
    }

    public static void setMonitorOnlyMode(boolean isMonitorOnlyMode) {
        Actions.monitorOnlyMode = isMonitorOnlyMode;
    }

    public static boolean isTestOnlyMode() {
        return testOnlyMode;
    }

    public static void setTestOnlyMode(boolean testOnlyMode) {
        Actions.testOnlyMode = testOnlyMode;
    }

    public static boolean isFailureOnlyMode() {
        return failureOnlyMode;
    }

    public static void setFailureOnlyMode(boolean failureOnlyMode) {
        Actions.failureOnlyMode = failureOnlyMode;
    }

    public static AssertionEvent getTestSuccessEvent() {
        return testSuccessEvent;
    }

    public static AssertionEvent getMonitorSuccessEvent() {
        return monitorSuccessEvent;
    }

    public static AssertionEvent getTestFailureEvent() {
        return testFailureEvent;
    }

    public static AssertionEvent getMonitorFailureEvent() {
        return monitorFailureEvent;
    }

    public static AssertionExceptionEvent getAssertionExceptionEvent() {
        return assertionExceptionEvent;
    }

    public static GeneralExceptionEvent getGeneralExceptionEvent() {
        return generalExceptionEvent;
    }

    public static void addExternalTestSuccessEvent(ExternalAssertionCallback externalCallback) {
        getTestSuccessEvent().add((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> externalCallback.execute(signature.getName(), invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete));
    }

    public static void addExternalTestFailureEvent(ExternalAssertionCallback externalCallback) {
        getTestFailureEvent().add((signature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> externalCallback.execute(signature.getName(), invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete));
    }

    public static void succeed(Signature calledSignature, Object result, List<Tuple<String, Object>> signatureTypesAndValues, Object target) {
        String message = calledSignature.toLongString() + "'s assertion succeeded.  Target: " + target.toString() + ".  Inputs: " + signatureTypesAndValues.toString() + ".  Result: " + result;
        System.out.println(message);
    }

    public static void fail(Signature calledSignature, Object result, List<Tuple<String, Object>> signatureTypesAndValues, Object target, String continuation) {
        String message = calledSignature.toLongString() + "'s assertion failed.  Target: " + target.toString() + ".  Inputs: " + signatureTypesAndValues.toString() + ".  Result: " + result + ". Continuation: " + continuation;
        System.out.println(message);
    }

    public static void handleAssertionException(Signature calledSignature, List<Tuple<String, Object>> signatureTypesAndValues, Object target, Exception ex) {
        String message = "Error during execution of assertion: " + calledSignature.toLongString() + ", target: " + target.toString() + "  inputs: " + signatureTypesAndValues.toString() + ", exception: " + ex.toString();
        System.out.println(message);
    }

    public static void handleGeneralException(Signature calledSignature, ArrayList<Object> calledArgumentsWithReturnValue, Object target, Exception ex) {
        String message = "Error during execution of aspect for: " + calledSignature.toLongString() + ", target: " + target.toString() + "  inputs: " + calledArgumentsWithReturnValue.toString() + ", exception: " + ex.toString();
        System.out.println(message);
    }

    public static void reset() {
        testSuccessEvent.clear();
        monitorSuccessEvent.clear();
        testFailureEvent.clear();
        monitorFailureEvent.clear();
        assertionExceptionEvent.clear();
        generalExceptionEvent.clear();
    }
}
