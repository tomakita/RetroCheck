package aspect.tests;

// TODO: THIS FILE IS JUST HERE FOR TESTING

public class AppAssertions {


    public boolean addTwo(int input, int result, App instance) {
        return result == input + 2 && result > input;
    }
}
