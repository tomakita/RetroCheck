package aspect.tests;

import com.retrocheck.assertion.MonitorWith;

import java.util.IllegalFormatWidthException;

public class TestClass {
    public int field = 0;

    public TestClass() {}

    @MonitorWith(TestClassAssertions.class)
    public TestClass(int x, String j) {
        System.out.println("making a new aspect.tests.TestClass");
        field = 5;
        throw new ArithmeticException("threw an exception in a ctor");
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(int x) {
        System.out.println("making making");
        field = x;
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(int x, int y) {
        System.out.println("making making making");
        field = y;
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(int x, int y, int z) {
        field = z;
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(boolean b) {
        throw new StringIndexOutOfBoundsException();
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(boolean a, boolean b) {
        throw new StringIndexOutOfBoundsException();
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(boolean a, boolean b, boolean c) {
        throw new StringIndexOutOfBoundsException();
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(Double d) throws InterruptedException {
        throw new InterruptedException();
    }

    @MonitorWith(TestClassAssertions.class)
    public TestClass(Double c, Double d) throws InterruptedException {
        throw new InterruptedException();
    }

    @MonitorWith(TestClassAssertions.class)
    public void mutateField() {
        field = 5;
    }

    // Not too hard to create one class per class under test, but we lose IDE compiler check
    // due to having to match the method names with reflection.
    @MonitorWith(TestClassAssertions.class)
    public static int addOne(int input) throws RuntimeException {
        throw new RuntimeException("OH NO AN EXCEPTION HAPPENING[[[D[S[D[SA[DA[]]]");
        //return input + 1;
    }

    @MonitorWith(TestClassAssertions.class)
    public static String name() {
        return "testclass";
    }

    @MonitorWith(TestClassAssertions.class)
    public static String name(String prefix) {
        return prefix + "testclass";
    }

    @MonitorWith(TestClassAssertions.class)
    public static void createException() {
        throw new NumberFormatException();
    }

    @MonitorWith(TestClassAssertions.class)
    public static void createException(int j) {
        throw new IllegalFormatWidthException(j);
    }

    @MonitorWith(TestClassAssertions.class)
    public static void createException(String s) throws IllegalAccessException {
        throw new IllegalAccessException(s);
    }

    @MonitorWith(TestClassAssertions.class)
    public static void createException(String s, String t) throws IllegalAccessException {
        throw new IllegalAccessException(t);
    }

    @MonitorWith(TestClassAssertions.class)
    public static boolean isItRaining() {
        throw new SecurityException();
    }

    @MonitorWith(TestClassAssertions.class)
    public String toString() {
        return "aspect.tests.TestClass of " + field;
    }

    @MonitorWith(TestClassAssertions.class)
    public long jjjjjjjjjjEEEEEEEEEEEEEEEEEFFFFFFFFFFFFFFFFF(long j) {
        return j + 1;
    }

    @MonitorWith(TestClassAssertions.class)
    public String doThingsWithAString(boolean b) {
        if (b) {
            return "smooth";
        } else {
            throw new RuntimeException("not so smooth");
        }
    }

    @MonitorWith(TestClassAssertions.class)
    public int doThingsWithAnInt(boolean b) {
        return b ? 1000 : -1000;
    }

    @MonitorWith(TestClassAssertions.class)
    public Object doThingsWithAnObject(boolean b) {
        return b ? null : new Object();
    }

    @MonitorWith(TestClassAssertions.class)
    public void doThingsWithNothing(boolean b) {
        return;
    }

    @MonitorWith(TestClassAssertions.class)
    public void doThingsWithNothing2() {
        return;
    }
}
