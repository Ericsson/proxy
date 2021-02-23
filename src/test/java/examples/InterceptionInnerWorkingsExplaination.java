
package examples;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class InterceptionInnerWorkingsExplaination {

    public static class SomeImpl {

        public void log(String log) {
            System.out.println(log);
        }
    }

    public static class MyInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            // possible do something before calling the original method
            Object returnObject = invocation.invoke();
            // possible do something after calling the original method
            return returnObject;
        }
    }

    public static void main(String[] args) {
        SomeImpl proxy = Proxy.intercept(new SomeImpl(), new MyInterceptor());
        proxy.log("hello world");
    }

    public static class MyInterceptor2 implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            System.out.println("Method entered: " + invocation.getMethodName());
            Object returnObject = invocation.invoke(); // original method invocation.
            System.out.println("Method exit: " + invocation.getMethodName());
            return returnObject;
        }
    }

    // Roughly what Proxy creates:
    public class SomeImplCreatedByProxy extends SomeImpl {

        @Override
        public void log(String log) {
            // now it's up to the interceptors to decide what this method should do.
            getInterceptorList().invokeAll();// delegation to original "SomeImpl" is the last interceptor
        }
    }

    // IGNORE these.
    public static Invoke getInterceptorList() {
        return null;
    }

    public interface Invoke {
        void invokeAll();
    }
}
