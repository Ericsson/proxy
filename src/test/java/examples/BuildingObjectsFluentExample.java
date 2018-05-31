
package examples;

import static com.ericsson.commonlibrary.proxy.Proxy.with;

import java.lang.reflect.Method;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;

public class BuildingObjectsFluentExample {

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {

        Algorithm algorithm = with(Algorithm.class).delegate(new Sort1(), new Reverse1()).get();
        System.out.println(algorithm);
        System.out.println(algorithm.algorithmReverse("fluent"));
        System.out.println(algorithm.algorithmSort("fluent"));

        Method method = Algorithm.class.getMethod("algorithmSort", String.class);

        with(algorithm).interceptAll(new StringInterceptor()).interceptMethod(new StringInterceptor(), method);
        System.out.println(algorithm);
        System.out.println(algorithm.algorithmReverse("fluent"));
        System.out.println(algorithm.algorithmSort("fluent"));
    }

    public static class StringInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            if (invocation.getMethod().getReturnType() == String.class) {
                return invocation.invoke() + "Intercepted";
            }
            return invocation.invoke();
        }
    }

    public interface Algorithm {

        String algorithmSort(String arg);

        String algorithmReverse(String arg);
    }

    public static class Sort1 {

        public String algorithmSort(String arg) {
            return "sort1: " + arg;
        }
    }

    public static class Reverse1 {

        public String algorithmReverse(String arg) {
            return "reverse1: " + arg;
        }
    }
}
