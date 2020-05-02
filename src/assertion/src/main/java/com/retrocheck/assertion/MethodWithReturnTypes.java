package com.retrocheck.assertion;

import java.lang.reflect.Method;
import java.util.List;

public class MethodWithReturnTypes {
    private Method assertionMethod;
    // The return type of the assertion.
    private Class assertionReturnType;
    // The return type of the method with which the assertion is associated.
    private Class monitoredReturnType;
    // Whether or not the method signature has parameters for the class instance and exception.
    private List<ParameterKind> parameterKinds;

    public MethodWithReturnTypes(Method assertionMethod, Class assertionReturnType, Class monitoredReturnType, List<ParameterKind> parameterKinds) {
        this.assertionMethod = assertionMethod;
        this.assertionReturnType = assertionReturnType;
        this.monitoredReturnType = monitoredReturnType;
        this.parameterKinds = parameterKinds;
    }

    public Method getAssertionMethod() {
        return assertionMethod;
    }

    public Class getAssertionReturnType() {
        return assertionReturnType;
    }

    public Class getMonitoredReturnType() {
        return monitoredReturnType;
    }

    public List<ParameterKind> getParameterKinds() {
        return parameterKinds;
    }
}
