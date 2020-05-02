package com.retrocheck.mock;


// TODO: DELETE THIS FILE EVENTUALLY; JUST HERE FOR TESTING


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        //Actions.getTestEvent().add(Actions::fail);
        //int incremented = new App().addOne(1);
        //new App().addTwo(incremented);
//        Actions.setServiceLocator(schema -> {
//            try {
//                return schema.getConstructor(Integer.class).newInstance(56);
//            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
//                throw new RuntimeException(e);
//            }
//        });

        new Test().write();
        System.out.println("hi");
    }

    // Not too hard to create one class per class under test, but we lose IDE compiler check
    // due to having to match the method names with reflection.
//    @MonitorWith(AppAssertions.class)
//    private int addOne(int input) {
//        return input + 1;
//    }
//
//    @MonitorWith(AppAssertions.class)
//    private int addTwo(int input) {
//        int once = addOne(input);
//        int twice = addOne(input);
//        return twice;
//    }
}
