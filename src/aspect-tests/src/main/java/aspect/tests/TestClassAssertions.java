package aspect.tests;

import com.retrocheck.assertion.AssertionResult;

import java.nio.channels.OverlappingFileLockException;
import java.util.IllegalFormatWidthException;

public class TestClassAssertions {
    public boolean constructor(int x, int y, int z, TestClass instance) {
        throw new ClassCastException("jjjjjj");
    }

    public boolean constructor(int x, String y, TestClass instance) {
        System.out.println("ctor normal");
        return true;
    }

    public boolean constructor(int x, String y, Exception result, TestClass instance) {
        System.out.println("ctor exception");
        return result instanceof ArithmeticException;
    }

    public boolean constructor(int x, TestClass instance) {
        System.out.println("soadupsaudosasa");
        return x == instance.field + 1;
    }

    public boolean constructor(int x, int y, TestClass instance) {
        System.out.println("ooooooooooooooooo");
        System.out.println(instance.field);
        return y == instance.field;
    }

    public boolean constructor(boolean b, Exception result, TestClass instance) {
        return result instanceof StringIndexOutOfBoundsException;
    }

    public boolean constructor(boolean a, boolean b, Exception result, TestClass instance) {
        return !(result instanceof StringIndexOutOfBoundsException);
    }

    public boolean constructor(boolean a, boolean b, boolean c, Exception result, TestClass instance) {
        return !(result instanceof StringIndexOutOfBoundsException);
    }

    public boolean constructor(Double d, Exception result, TestClass instance) throws CloneNotSupportedException {
        throw new CloneNotSupportedException("yyyyy");
    }

    public boolean constructor(Double c, Double d, Exception result, TestClass instance) throws CloneNotSupportedException {
        throw new CloneNotSupportedException("zzzzz");
    }

    public boolean toString(String result, TestClass instance) {
        System.out.println("asserting on toString");
        return result.equals("aspect.tests.TestClass of " + instance.field);
    }

    public boolean addOne(int input, int result, App instance) {
        System.out.println("addOne normal");
        return result == input + 1 && result > input;
    }

    public boolean addOne(int input, Exception result, App instance) {
        System.out.println("addOne exception");
        throw new OverlappingFileLockException();
        //return result instanceof RuntimeException;
    }

    public boolean name(String result, TestClass instance) {
        return result == "aspect.tests.TestClass";
    }

    public boolean name(String input, String result, TestClass instance) {
        return result == input.concat("testclass");
    }

    public boolean createException(Exception result, TestClass instance) {
        return result instanceof NumberFormatException;
    }

    public boolean createException(int j, Exception result, TestClass instance) {
        return result instanceof IllegalFormatWidthException;
    }

    public boolean createException(String s, Exception result, TestClass instance) {
        return false;
    }

    public boolean createException(String s, String t, Exception result, TestClass instance) {
        return false;
    }

    public boolean isItRaining(Exception result, TestClass instance) {
        throw new NegativeArraySizeException();
    }

    public AssertionResult jjjjjjjjjjEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFF(long j, long result, TestClass instance) {
        return new AssertionResult(j + 1 == result, "baltimore kansas excel spreadsheet");
    }

    public boolean mutateField(TestClass instance) {
        System.out.println("hello yes");
        return instance.field == 5;
    }

    public AssertionResult doThingsWithAString(boolean b, String result, Exception ex) {
        if (ex != null) {
            System.out.println("exception was thrown in doThingsWithAString assertion");
            return new AssertionResult(false);
        }

        return new AssertionResult(true);
    }

    public AssertionResult doThingsWithAnInt(boolean b, int result, Exception ex, TestClass instance) {
        if (ex != null) {
            System.out.println("exception was thrown in doThingsWithAnInt assertion");
            return new AssertionResult(false);
        }

        return new AssertionResult(true);
    }

    public boolean doThingsWithAnObject(boolean b, Object result, TestClass instance, Exception ex) {
        if (ex != null) {
            System.out.println("exception was thrown in doThingsWithAnObject assertion");
            return false;
        }

        return true;
    }

    public boolean doThingsWithNothing(boolean b, TestClass instance, Exception ex) {
        if (ex != null) {
            System.out.println("exception was thrown in doThingsWithNothing assertion");
            return false;
        }

        return true;
    }

    public boolean doThingsWithNothing2(TestClass instance) {
        return true;
    }
}
