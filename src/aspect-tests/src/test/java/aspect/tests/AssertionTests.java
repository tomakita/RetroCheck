package aspect.tests;

import com.retrocheck.assertion.Actions;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.IllegalFormatWidthException;

import static org.junit.Assert.*;

public class AssertionTests {
    private boolean hasActionBeenCalled = false;

    // RULES FOR WRITING TESTS HERE:
    // --Actions.setTestOnlyMode(true) is meant to make it so the Monitor callbacks aren't triggered,
    //   since they may contain logic that can mess up the results of our tests, via both the logic
    //   that they contain, and the assertions that can execute as a result of those callbacks being
    //   triggered.
    // --In JUnit, assertions (e.g. assertTrue) are short-circuiting, so the first failing assertion
    //   will stop the test.  This means that we shouldn't write these tests in such a way that
    //   assertions will be chained.  This basically means we should only call a single function
    //   in these tests -- don't do things like 'new X().f().g()', where 'new X()', 'f()', and g()'
    //   all have their own assertions defined, because then all three of those assertions will need
    //   to have the desired result in order for the test to pass.

    @Before
    public void resetActions() {
        Actions.reset();
        Actions.setFailureOnlyMode(false);
        Actions.setTestOnlyMode(true);
        hasActionBeenCalled = false;
    }

    private void setActions(boolean assertOnSuccess, boolean assertOnFailure, boolean assertOnException) {
        long assertionCount = Arrays.asList(assertOnSuccess, assertOnFailure, assertOnException).stream().filter(a -> a == true).count();
        if (assertionCount != 1) {
            throw new IllegalArgumentException("Exactly one assertion is allowed to be used at once.");
        }

        Actions.getTestSuccessEvent().add(((calledSignature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> {
            System.out.println("success");
            hasActionBeenCalled = true;
            assertTrue(assertOnSuccess);
        }));
        Actions.getTestFailureEvent().add(((calledSignature, invocationResult, signatureTypesAndValues, instance, continuation, isExecutionComplete) -> {
            System.out.println("failure");
            hasActionBeenCalled = true;
            assertTrue(assertOnFailure);
        }));
        // need to be able to pass in lambda here, b/c otherwise this could pass when the exception was one that happened elsewhere in the system
        // and has nothing to do with the desired behavior.
        Actions.getAssertionExceptionEvent().add(((calledSignature, signatureTypesAndValues, target, ex) -> {
            hasActionBeenCalled = true;
            System.out.println(ex);
            assertTrue(assertOnException);}));
    }

    @Test
    public void method_AppDoesntThrow_AspectDoesntThrow_AssertionSucceeds_MonitorWith() {
        // despite what it might look like, the listeners defined in setActions run
        // synchronously with respect to the code in this test, i.e. they run exactly
        // after the assertions themselves have finished, e.g. at "((HERE))", below.
        setActions(true, false, false);
        new TestClass().toString();

        // ((HERE))

        // we do this assertion to make sure that one of the assertions being listened to
        // in setActions has actually been called.
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void method_AppDoesntThrow_AspectDoesntThrow_AssertionFails_MonitorWith() {
        setActions(false, true, false);
        TestClass.name();
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void method_AppDoesntThrow_AspectThrows_AssertionException_MonitorWith() {
        setActions(false, false, true);
        TestClass.name(null);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void ctor_AppDoesntThrow_AspectDoesntThrow_AssertionFails_MonitorWith()
    {
        setActions(false, true, false);
        new TestClass(1);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void ctor_AppDoesntThrow_AspectDoesntThrow_AssertionSucceeds_MonitorWith()
    {
        setActions(true, false, false);
        new TestClass(1, 3);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void ctor_AppDoesntThrow_AspectThrows_AssertionException_MonitorWith() {
        setActions(false, false, true);
        new TestClass(0, 0, 0);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void method_AppThrows_AspectDoesntThrow_AssertionSucceeds_MonitorWith() {
        setActions(true, false, false);
        try {
            TestClass.createException();
        }
        catch (NumberFormatException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        // should never get here
        fail();
    }

    @Test
    public void method_AppThrows_AspectDoesntThrow_AssertionSucceeds_ExceptionOnlyMonitorWith() {
        setActions(true, false, false);
        try {
            TestClass.createException(1);
        }
        catch (IllegalFormatWidthException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void method_AppThrows_AspectDoesntThrow_AssertionFails_MonitorWith() {
        setActions(false, true, false);
        try {
            TestClass.createException("hi");
        }
        catch (IllegalAccessException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void method_AppThrows_AspectDoesntThrow_AssertionFails_ExceptionOnlyMonitorWith() {
        setActions(false, true, false);
        try {
            TestClass.createException("hi", "bye");
        }
        catch (IllegalAccessException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void method_AppThrows_AspectThrows_AssertionException_MonitorWith()
    {
        setActions(false, false, true);
        try {
            TestClass.addOne(1);
        }
        catch (RuntimeException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void method_AppThrows_AspectThrows_AssertionException_ExceptionOnlyMonitorWith() {
        setActions(false, false, true);
        try {
            TestClass.isItRaining();
        }
        catch (SecurityException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectDoesntThrow_AssertionSucceeds_MonitorWith()
    {
        setActions(true, false, false);
        try {
            new TestClass(1, "jeff");
        }
        catch (RuntimeException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectDoesntThrow_AssertionSucceeds_ExceptionOnlyMonitorWith() {
        setActions(true, false, false);
        try {
            new TestClass(false);
        }
        catch (StringIndexOutOfBoundsException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectDoesntThrow_AssertionFails_MonitorWith() {
        setActions(false, true, false);
        try {
            new TestClass(true, false);
        }
        catch (StringIndexOutOfBoundsException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectDoesntThrow_AssertionFails_ExceptionOnlyMonitorWith() {
        setActions(false, true, false);
        try {
            new TestClass(true, false, false);
        }
        catch (StringIndexOutOfBoundsException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectThrows_AssertionException_MonitorWith() {
        setActions(false, false, true);
        try {
            new TestClass(0.009);
        }
        catch (InterruptedException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void ctor_AppThrows_AspectThrows_AssertionException_ExceptionOnlyMonitorWith() {
        setActions(false, false, true);
        try {
            new TestClass(0.009, 1.999);
        }
        catch (InterruptedException ex) {
            assertTrue(hasActionBeenCalled);
            return;
        }

        fail();
    }

    @Test
    public void methodAssertion_CanReturnAssertionResult() {
        setActions(true, false, false);
        new TestClass().jjjjjjjjjjEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFF(5);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void methodAssertion_CanFindExceptionOnlySignatureAssertion() {
        setActions(true, false, false);
        new TestClass().doThingsWithAString(true);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void methodAssertion_CanFindExceptionInstanceSignatureAssertion() {
        setActions(true, false, false);
        new TestClass().doThingsWithAnInt(true);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void methodAssertion_CanFindInstanceExceptionSignatureAssertion() {
        setActions(true, false, false);
        new TestClass().doThingsWithAnObject(true);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void methodAssertion_CanFindVoidInstanceExceptionSignatureAssertion() {
        setActions(true, false, false);
        new TestClass().doThingsWithNothing(true);
        assertTrue(hasActionBeenCalled);
    }

    @Test
    public void methodAssertion_CanFindVoidInstanceSignatureAssertion() {
        setActions(true, false, false);
        new TestClass().doThingsWithNothing2();
        assertTrue(hasActionBeenCalled);
    }
}
