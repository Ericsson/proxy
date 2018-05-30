/*
Copyright (c) 2018 Ericsson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package examples;

import static com.ericsson.commonlibrary.proxy.Proxy.with;

import java.lang.reflect.Method;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;

public class FluentExample {

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
