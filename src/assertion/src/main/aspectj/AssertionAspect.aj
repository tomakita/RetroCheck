import com.retrocheck.assertion.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public aspect AssertionAspect {
    // TODO: this should eventually have an eviction strategy (like LRU), just in case this is used with a gigantic app that
    //       has tons and tons of methods that would cause memory problems if they were all added to this map.
    static HashMap<String, MethodWithReturnTypes> cache = new HashMap<>();
    pointcut monitorableMethod(MonitorWith monitorWith): execution(* *.*(..)) && @annotation(monitorWith);

    // TODO: move this into its own class?
    pointcut monitorableConstructor(MonitorWith monitorWith): execution(*.new(..)) && @annotation(monitorWith);

    private OutcomeHandlingMetadata createOutcomeMetadata(AssertionResult assertionResult, Signature calledSignature, Object[] calledArguments, Object returnValue) {
        if (!assertionResult.isSuccess() || !Actions.isFailureOnlyMode()) {
            MethodSignature methodUnderTestSignature = (MethodSignature) calledSignature; // this line is duplicated from the "is not cached" case, but for the sake of code simplicity, that's ok for now.
            Class returnType = methodUnderTestSignature.getReturnType();
            Class[] signatureTypesWithoutReturnType = methodUnderTestSignature.getParameterTypes();
            List<Tuple<String, Object>> signatureTypesAndValues = IntStream.range(0, calledArguments.length).mapToObj(i -> new Tuple<String, Object>(signatureTypesWithoutReturnType[i].getName(), calledArguments[i])).collect(Collectors.toList());
            Tuple<String, Object> invocationResults = new Tuple<String, Object>(returnType.getName(), returnValue);

            return new OutcomeHandlingMetadata(calledSignature, invocationResults, signatureTypesAndValues, assertionResult.getContinuation(), assertionResult.isExecutionComplete());
        }

        return null;
    }

    // this is for use when processing monitorableConstructor
    private OutcomeHandlingMetadata createOutcomeMetadataForConstructors(AssertionResult assertionResult, Signature calledSignature, Object[] calledArguments, Object returnValue) {
        if (!assertionResult.isSuccess() || !Actions.isFailureOnlyMode()) {
            ConstructorSignature methodUnderTestSignature = (ConstructorSignature) calledSignature; // this line is duplicated from the "is not cached" case, but for the sake of code simplicity, that's ok for now.
            Class[] signatureTypesWithoutReturnType = methodUnderTestSignature.getParameterTypes();
            List<Tuple<String, Object>> signatureTypesAndValues = IntStream.range(0, calledArguments.length).mapToObj(i -> new Tuple<String, Object>(signatureTypesWithoutReturnType[i].getName(), calledArguments[i])).collect(Collectors.toList());

            return new OutcomeHandlingMetadata(calledSignature, null, signatureTypesAndValues, assertionResult.getContinuation(), assertionResult.isExecutionComplete());
        }

        return null;
    }

    private void handleOutcome(AssertionResult assertionResult, Signature calledSignature, OutcomeHandlingMetadata outcomeMetadata, Object instance) {
        if (assertionResult.isSuccess() && !Actions.isFailureOnlyMode()) {
            if (!Actions.isTestOnlyMode()) {
                if (Actions.getMonitorSuccessEvent().size() > 0) {
                    Actions.getMonitorSuccessEvent().trigger(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance, outcomeMetadata.getContinuation(), outcomeMetadata.isExecutionComplete());
                } //else {
                //  Actions.succeed(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance);
                //}
            }

            if (!Actions.isMonitorOnlyMode()) {
                Actions.getTestSuccessEvent().trigger(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance, outcomeMetadata.getContinuation(), outcomeMetadata.isExecutionComplete());
            }
        }
        else if (!assertionResult.isSuccess()) {
            if (!Actions.isTestOnlyMode()) {
                if (Actions.getMonitorFailureEvent().size() > 0) {
                    Actions.getMonitorFailureEvent().trigger(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance, outcomeMetadata.getContinuation(), outcomeMetadata.isExecutionComplete());
                } else {
                    Actions.fail(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance, outcomeMetadata.getContinuation());
                }
            }

            if (!Actions.isMonitorOnlyMode()) {
                Actions.getTestFailureEvent().trigger(calledSignature, outcomeMetadata.getInvocationResults(), outcomeMetadata.getSignatureTypesAndValues(), instance, outcomeMetadata.getContinuation(), outcomeMetadata.isExecutionComplete());
            }
        }
    }

    private void handleAssertionException(Signature calledSignature, Object target, Object[] arguments, OutcomeHandlingMetadata outcomeMetadata, Exception ex) {
        List<Tuple<String, Object>> signatureTypesAndValues = outcomeMetadata.getSignatureTypesAndValues();
        if (Actions.getAssertionExceptionEvent().size() > 0) {
            Actions.getAssertionExceptionEvent().trigger(calledSignature, signatureTypesAndValues, target, ex);
        } else {
            Actions.handleAssertionException(calledSignature, signatureTypesAndValues, target, ex);
        }
    }

    private AssertionResult invoke(Callable<Object> assertionMethodCall, Class assertionReturnType) throws Exception {
        Object result = assertionMethodCall.call();
        if (assertionReturnType.equals(boolean.class)) {
            boolean assertionSucceeded = (boolean) result;
            return new AssertionResult(assertionSucceeded, null);
        } else if (assertionReturnType.equals(AssertionResult.class)) {
            return (AssertionResult) result;
        } else {
            throw new RuntimeException("Assertion returns unexpected type -- type must be either boolean or AssertionResult.");
        }
    }

    private ParameterContainer<Method> findMethodAssertion(String name, Class cls, List<Class> signatureTypes, Class targetType) {
        ParameterContainer<List<Class>> withoutInstanceOrException = new ParameterContainer<>(new ArrayList<>(signatureTypes));

        List<Class> signatureWithInstance = new ArrayList<>(signatureTypes);
        signatureWithInstance.add(targetType);
        ParameterContainer<List<Class>> withInstance = new ParameterContainer<>(signatureWithInstance);
        withInstance.add(ParameterKind.INSTANCE);

        List<Class> signatureWithException = new ArrayList<>(signatureTypes);
        signatureWithException.add(Exception.class);
        ParameterContainer<List<Class>> withException = new ParameterContainer<>(signatureWithException);
        withException.add(ParameterKind.EXCEPTION);

        List<Class> signatureWithInstanceAndException = new ArrayList<>(signatureTypes);
        signatureWithInstanceAndException.add(targetType);
        signatureWithInstanceAndException.add(Exception.class);
        ParameterContainer<List<Class>> withInstanceAndException = new ParameterContainer<>(signatureWithInstanceAndException);
        withInstanceAndException.add(ParameterKind.INSTANCE);
        withInstanceAndException.add(ParameterKind.EXCEPTION);

        List<Class> signatureWithExceptionAndInstance = new ArrayList<>(signatureTypes);
        signatureWithExceptionAndInstance.add(Exception.class);
        signatureWithExceptionAndInstance.add(targetType);
        ParameterContainer<List<Class>> withExceptionAndInstance = new ParameterContainer<>(signatureWithExceptionAndInstance);
        withExceptionAndInstance.add(ParameterKind.EXCEPTION);
        withExceptionAndInstance.add(ParameterKind.INSTANCE);

        List<ParameterContainer<List<Class>>> containers = new ArrayList<>();
        containers.add(withoutInstanceOrException);
        containers.add(withInstance);
        containers.add(withException);
        containers.add(withInstanceAndException);
        containers.add(withExceptionAndInstance);

        ParameterContainer<Method> assertionMethodContainer = null;
        for (ParameterContainer<List<Class>> container : containers) {
            try {
                Method assertionMethod = cls.getMethod(name, container.get().toArray(new Class[0]));
                assertionMethodContainer = new ParameterContainer<>(assertionMethod, container.getParameterMetadata());
                break;
            } catch (NoSuchMethodException ex) {}
        }

        if (assertionMethodContainer == null) {
            throw new RuntimeException("No assertion method was found for name " + name + " with valid argument list." +
                    "  Valid argument lists for an assertion on a method T f(...) defined on class C are:" +
                    " fAssertion(..., T result)," +
                    " fAssertion(..., T result, C instance)," +
                    " fAssertion(..., T result, Exception ex)," +
                    " fAssertion(..., T result, C instance, Exception ex), or" +
                    " fAssertion(..., T result, Exception ex, C instance).  `T result` must be" +
                    " omitted if T = void. ");
        }

        return assertionMethodContainer;
    }

    private ParameterContainer<Method> findConstructorAssertion(String name, Class cls, List<Class> signatureTypes, Class targetType) {
        List<Class> signatureWithInstance = new ArrayList<>(signatureTypes);
        signatureWithInstance.add(targetType);
        ParameterContainer<List<Class>> withInstance = new ParameterContainer<>(signatureWithInstance);
        withInstance.add(ParameterKind.INSTANCE);

        List<Class> signatureWithInstanceAndException = new ArrayList<>(signatureTypes);
        signatureWithInstanceAndException.add(targetType);
        signatureWithInstanceAndException.add(Exception.class);
        ParameterContainer<List<Class>> withInstanceAndException = new ParameterContainer<>(signatureWithInstanceAndException);
        withInstanceAndException.add(ParameterKind.INSTANCE);
        withInstanceAndException.add(ParameterKind.EXCEPTION);

        List<Class> signatureWithExceptionAndInstance = new ArrayList<>(signatureTypes);
        signatureWithExceptionAndInstance.add(Exception.class);
        signatureWithExceptionAndInstance.add(targetType);
        ParameterContainer<List<Class>> withExceptionAndInstance = new ParameterContainer<>(signatureWithExceptionAndInstance);
        withExceptionAndInstance.add(ParameterKind.EXCEPTION);
        withExceptionAndInstance.add(ParameterKind.INSTANCE);

        List<ParameterContainer<List<Class>>> containers = new ArrayList<>();
        containers.add(withInstance);
        containers.add(withInstanceAndException);
        containers.add(withExceptionAndInstance);

        ParameterContainer<Method> assertionMethodContainer = null;
        for (ParameterContainer<List<Class>> container : containers) {
            try {
                Method assertionMethod = cls.getMethod(name, container.get().toArray(new Class[0]));
                assertionMethodContainer = new ParameterContainer<>(assertionMethod, container.getParameterMetadata());
                break;
            } catch (NoSuchMethodException ex) {}
        }

        if (assertionMethodContainer == null) {
            throw new RuntimeException("No assertion method was found for name " + name + " with valid argument list." +
                    "  Valid argument lists for an assertion on a constructor C f(...) defined on class C are:" +
                    " fAssertion(..., C instance)," +
                    " fAssertion(..., C instance, Exception ex), or" +
                    " fAssertion(..., Exception ex, C instance)");
        }

        return assertionMethodContainer;
    }

    private void augmentArgumentList(List<Object> arguments, List<ParameterKind> requiredParameterKinds, HashMap<ParameterKind, Object> presentParameterKinds) {
        for (ParameterKind kind : requiredParameterKinds) {
            if (presentParameterKinds.containsKey(kind)) {
                arguments.add(presentParameterKinds.get(kind));
            } else {
                // This is safe to do, because the only time this will ever happen is when an
                // exception is required by the signature, but not present in the advice (since
                // no exception has been thrown).  Exceptions are nullable, so this is OK.
                arguments.add(null);
            }
        }
    }

    private void adviseExceptionForMethods(JoinPoint thisJoinPoint, Class assertionClass, Exception exception) {
        if (!Actions.areAssertionsEnabled) {
            return;
        }

        Signature calledSignature = null;
        final ArrayList<Object> calledArgumentsWithReturnValue = new ArrayList<>();
        Object target = null;

        try {
            calledSignature = thisJoinPoint.getSignature();
            Object[] arguments = thisJoinPoint.getArgs();
            calledArgumentsWithReturnValue.addAll(Arrays.asList(arguments));

            target = thisJoinPoint.getTarget();
            HashMap<ParameterKind, Object> auxArguments = new HashMap<>();
            auxArguments.put(ParameterKind.EXCEPTION, exception);
            auxArguments.put(ParameterKind.INSTANCE, target);
            String cacheKey = calledSignature.toLongString() + "_exception";

            try {
                if (cache.containsKey(cacheKey)) {
                    MethodWithReturnTypes assertionMethodWithReturnTypes = cache.get(cacheKey);
                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodWithReturnTypes.getParameterKinds(), auxArguments);
                    Callable<Object> assertionMethodCall = () -> assertionMethodWithReturnTypes.getAssertionMethod().invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionMethodWithReturnTypes.getAssertionReturnType());

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(result, calledSignature, arguments, exception);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                } else {
                    // TODO: note in the documentation that the assertion classes must be stateless and have a parameterless ctor in order for the below
                    //       code to work
                    MethodSignature methodUnderTestSignature = (MethodSignature) calledSignature;
                    List<Class> signatureTypes = new ArrayList<>(Arrays.asList(methodUnderTestSignature.getParameterTypes()));

                    Class targetType = methodUnderTestSignature.getDeclaringType();

                    String assertionMethodName = calledSignature.getName();
                    ParameterContainer<Method> assertionMethodContainer = findMethodAssertion(assertionMethodName, assertionClass, signatureTypes, targetType);
                    Method assertionMethod = assertionMethodContainer.get();
                    Class assertionReturnType = assertionMethod.getReturnType();

                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodContainer.getParameterMetadata(), auxArguments);
                    cache.put(cacheKey, new MethodWithReturnTypes(assertionMethod, assertionReturnType, null, assertionMethodContainer.getParameterMetadata()));
                    Callable<Object> assertionMethodCall = () -> assertionMethod.invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionReturnType);

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(result, calledSignature, arguments, exception);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                }
            } catch (InvocationTargetException ex) {
                Exception innerException = (Exception)ex.getCause();
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(AssertionResult.failed, calledSignature, arguments, exception);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, innerException);
            } catch (Exception ex) {
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(AssertionResult.failed, calledSignature, arguments, exception);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, ex);
            }
        } catch (Exception ex) {
            if (Actions.getGeneralExceptionEvent().size() > 0) {
                Actions.getGeneralExceptionEvent().trigger(calledSignature, calledArgumentsWithReturnValue, target, ex);
            } else {
                Actions.handleGeneralException(calledSignature, calledArgumentsWithReturnValue, target, ex);
            }
        }
    }

    private void adviseExceptionForConstructors(JoinPoint thisJoinPoint, Class assertionClass, Exception exception) {
        if (!Actions.areAssertionsEnabled) {
            return;
        }

        Signature calledSignature = null;
        final ArrayList<Object> calledArgumentsWithReturnValue = new ArrayList<>();
        Object target = null;

        try {
            calledSignature = thisJoinPoint.getSignature();
            Object[] arguments = thisJoinPoint.getArgs();
            calledArgumentsWithReturnValue.addAll(Arrays.asList(arguments));
            target = thisJoinPoint.getTarget();
            HashMap<ParameterKind, Object> auxArguments = new HashMap<>();
            auxArguments.put(ParameterKind.EXCEPTION, exception);
            auxArguments.put(ParameterKind.INSTANCE, target);
            String cacheKey = calledSignature.toLongString() + "_exception";

            try {
                if (cache.containsKey(cacheKey)) {
                    MethodWithReturnTypes assertionMethodWithReturnTypes = cache.get(cacheKey);
                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodWithReturnTypes.getParameterKinds(), auxArguments);
                    Callable<Object> assertionMethodCall = () -> assertionMethodWithReturnTypes.getAssertionMethod().invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionMethodWithReturnTypes.getAssertionReturnType());

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(result, calledSignature, arguments, exception);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                } else {
                    ConstructorSignature methodUnderTestSignature = (ConstructorSignature) calledSignature;
                    List<Class> signatureTypes = new ArrayList<>(Arrays.asList(methodUnderTestSignature.getParameterTypes()));

                    Class targetType = methodUnderTestSignature.getDeclaringType();

                    ParameterContainer<Method> assertionMethodContainer = findConstructorAssertion("constructor", assertionClass, signatureTypes, targetType);
                    Method assertionMethod = assertionMethodContainer.get();
                    Class assertionReturnType = assertionMethod.getReturnType();

                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodContainer.getParameterMetadata(), auxArguments);
                    cache.put(cacheKey, new MethodWithReturnTypes(assertionMethod, assertionReturnType, null, assertionMethodContainer.getParameterMetadata()));
                    Callable<Object> assertionMethodCall = () -> assertionMethod.invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionReturnType);

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(result, calledSignature, arguments, exception);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                }
            } catch (InvocationTargetException ex) {
                Exception innerException = (Exception)ex.getCause();
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(AssertionResult.failed, calledSignature, arguments, exception);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, innerException);
            } catch (Exception ex) {
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(AssertionResult.failed, calledSignature, arguments, exception);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, ex);
            }
        } catch (Exception ex) {
            if (Actions.getGeneralExceptionEvent().size() > 0) {
                Actions.getGeneralExceptionEvent().trigger(calledSignature, calledArgumentsWithReturnValue, target, ex);
            } else {
                Actions.handleGeneralException(calledSignature, calledArgumentsWithReturnValue, target, ex);
            }
        }
    }

    after(MonitorWith monitorWith) returning(Object returnValue) : monitorableMethod(monitorWith) {
        if (!Actions.areAssertionsEnabled) {
            return;
        }

        Signature calledSignature = null;
        final ArrayList<Object> calledArgumentsWithReturnValue = new ArrayList<>();
        Object target = null;

        try {
            calledSignature = thisJoinPoint.getSignature();
            Class assertionClass = monitorWith.value();
            Object[] arguments = thisJoinPoint.getArgs();
            calledArgumentsWithReturnValue.addAll(Arrays.asList(arguments));
            calledArgumentsWithReturnValue.add(returnValue); // note: returnValue is null if the method is a void method.
            target = thisJoinPoint.getTarget();
            HashMap<ParameterKind, Object> auxArguments = new HashMap<>();
            auxArguments.put(ParameterKind.INSTANCE, target);
            String cacheKey = calledSignature.toLongString();

            try {
                if (cache.containsKey(cacheKey)) {
                    MethodWithReturnTypes assertionMethodWithReturnTypes = cache.get(cacheKey);
                    if (assertionMethodWithReturnTypes.getMonitoredReturnType().equals(void.class)) {
                        int indexOfDummyNullReturnValue = calledArgumentsWithReturnValue.size() - 1;
                        calledArgumentsWithReturnValue.remove(indexOfDummyNullReturnValue);
                    }

                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodWithReturnTypes.getParameterKinds(), auxArguments);
                    Callable<Object> assertionMethodCall = () -> assertionMethodWithReturnTypes.getAssertionMethod().invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionMethodWithReturnTypes.getAssertionReturnType());

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(result, calledSignature, arguments, returnValue);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                } else {
                    // TODO: note in the documentation that the assertion classes must be stateless and have a parameterless ctor in order for the below
                    //       code to work
                    MethodSignature methodUnderTestSignature = (MethodSignature) calledSignature;
                    List<Class> signatureTypes = new ArrayList<>(Arrays.asList(methodUnderTestSignature.getParameterTypes()));
                    Class methodReturnType = methodUnderTestSignature.getReturnType();

                    if (!methodReturnType.equals(void.class)) {
                        signatureTypes.add(methodReturnType);
                    } else {
                        int indexOfDummyNullReturnValue = calledArgumentsWithReturnValue.size() - 1;
                        calledArgumentsWithReturnValue.remove(indexOfDummyNullReturnValue);
                    }

                    Class targetType = methodUnderTestSignature.getDeclaringType();
                    String assertionMethodName = calledSignature.getName();
                    ParameterContainer<Method> assertionMethodContainer = findMethodAssertion(assertionMethodName, assertionClass, signatureTypes, targetType);
                    Method assertionMethod = assertionMethodContainer.get();
                    Class assertionReturnType = assertionMethod.getReturnType();

                    cache.put(cacheKey, new MethodWithReturnTypes(assertionMethod, assertionReturnType, methodReturnType, assertionMethodContainer.getParameterMetadata()));
                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodContainer.getParameterMetadata(), auxArguments);
                    Callable<Object> assertionMethodCall = () -> assertionMethod.invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionReturnType);

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(result, calledSignature, arguments, returnValue);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                }
            } catch (InvocationTargetException ex) {
                Exception innerException = (Exception)ex.getCause();
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(AssertionResult.failed, calledSignature, arguments, returnValue);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, innerException);
            } catch (Exception ex) {
                // note, getSignatureTypesAndHandleFailure could throw an exception here, in which case it will be caught by the general exception handler catch block at the
                // bottom of this file.
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadata(AssertionResult.failed, calledSignature, arguments, returnValue);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, ex);
            }
        } catch (Exception ex) {
            if (Actions.getGeneralExceptionEvent().size() > 0) {
                Actions.getGeneralExceptionEvent().trigger(calledSignature, calledArgumentsWithReturnValue, target, ex);
            } else {
                Actions.handleGeneralException(calledSignature, calledArgumentsWithReturnValue, target, ex);
            }
        }
    }

    // TODO: the logic here is like 97% the same as above, so see if you can find a way to refactor both so that code can be reused.
    after(MonitorWith monitorWith) throwing(Exception exception) : monitorableMethod(monitorWith)  {
        adviseExceptionForMethods(thisJoinPoint, monitorWith.value(), exception);
    }

    // TODO: the logic here is like 97% the same as above, so see if you can find a way to refactor both so that code can be reused.
    after(MonitorWith monitorWith) returning : monitorableConstructor(monitorWith)  {
        if (!Actions.areAssertionsEnabled) {
            return;
        }

        Signature calledSignature = null;
        final ArrayList<Object> calledArgumentsWithReturnValue = new ArrayList<>();
        Object target = null;

        try {
            calledSignature = thisJoinPoint.getSignature();
            Class assertionClass = monitorWith.value();
            Object[] arguments = thisJoinPoint.getArgs();
            calledArgumentsWithReturnValue.addAll(Arrays.asList(arguments));
            target = thisJoinPoint.getTarget();
            HashMap<ParameterKind, Object> auxArguments = new HashMap<>();
            auxArguments.put(ParameterKind.INSTANCE, target);
            String cacheKey = calledSignature.toLongString();

            try {
                if (cache.containsKey(cacheKey)) {
                    MethodWithReturnTypes assertionMethodWithReturnTypes = cache.get(cacheKey);
                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodWithReturnTypes.getParameterKinds(), auxArguments);
                    Callable<Object> assertionMethodCall = () -> assertionMethodWithReturnTypes.getAssertionMethod().invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionMethodWithReturnTypes.getAssertionReturnType());

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(result, calledSignature, arguments, null);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                } else {
                    ConstructorSignature methodUnderTestSignature = (ConstructorSignature) calledSignature;
                    List<Class> signatureTypes = new ArrayList<>(Arrays.asList(methodUnderTestSignature.getParameterTypes()));
                    Class targetType = methodUnderTestSignature.getDeclaringType();

                    ParameterContainer<Method> assertionMethodContainer = findConstructorAssertion("constructor", assertionClass, signatureTypes, targetType);
                    Method assertionMethod = assertionMethodContainer.get();
                    Class assertionReturnType = assertionMethod.getReturnType();

                    augmentArgumentList(calledArgumentsWithReturnValue, assertionMethodContainer.getParameterMetadata(), auxArguments);
                    cache.put(cacheKey, new MethodWithReturnTypes(assertionMethod, assertionReturnType, null, assertionMethodContainer.getParameterMetadata()));
                    Callable<Object> assertionMethodCall = () -> assertionMethod.invoke(Actions.getServiceLocator().apply(assertionClass), calledArgumentsWithReturnValue.toArray());
                    AssertionResult result = invoke(assertionMethodCall, assertionReturnType);

                    OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(result, calledSignature, arguments, null);
                    handleOutcome(result, calledSignature, outcomeMetadata, target);
                }
            } catch (InvocationTargetException ex) {
                Exception innerException = (Exception)ex.getCause();
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(AssertionResult.failed, calledSignature, arguments, null);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, innerException);
            } catch (Exception ex) {
                OutcomeHandlingMetadata outcomeMetadata = createOutcomeMetadataForConstructors(AssertionResult.failed, calledSignature, arguments, null);
                handleAssertionException(calledSignature, target, arguments, outcomeMetadata, ex);
            }
        } catch (Exception ex) {
            if (Actions.getGeneralExceptionEvent().size() > 0) {
                Actions.getGeneralExceptionEvent().trigger(calledSignature, calledArgumentsWithReturnValue, target, ex);
            } else {
                Actions.handleGeneralException(calledSignature, calledArgumentsWithReturnValue, target, ex);
            }
        }
    }

    // TODO: the logic here is like 97% the same as above, so see if you can find a way to refactor both so that code can be reused.
    after(MonitorWith monitorWith) throwing(Exception exception) : monitorableConstructor(monitorWith)  {
        adviseExceptionForConstructors(thisJoinPoint, monitorWith.value(), exception);
    }
}