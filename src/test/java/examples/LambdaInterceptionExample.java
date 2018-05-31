package examples;

import static com.ericsson.commonlibrary.proxy.Proxy.with;

public class LambdaInterceptionExample {

    public static class SomeImpl { //sample class

        public void log(String log) {
            System.out.println(log);
        }
    }

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {

        //lambda on class
        SomeImpl obj = with(SomeImpl.class)
                .interceptAll(i -> {
                    System.out.println("before method: " + i.getMethodName() + " param: " + i.getParameter0());
                    return i.invoke();
                }).get();
        obj.log("123");
        //Console output:
        //        before method: log param: 123
        //        123

        //lambda on object
        SomeImpl obj2 = with(new SomeImpl())
                .interceptAll(i -> {
                    Object result = i.invoke();
                    System.out.println("after method: " + i.getMethodName() + " param: " + i.getParameter0());
                    return result;
                }).get();
        obj2.log("321");
        //Console output:
        //        321
        //        after method: log param: 321

        //lambda without return.
        SomeImpl obj3 = with(SomeImpl.class)
                .interceptAll((i) -> System.out.println("Replace method invocation: " + i.getMethodName()))
                .get();
        obj3.log("12345");
        //Console output:
        //        Replace method invocation: log
    }
}
