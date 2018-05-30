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
            //possible do something before calling the original method
            Object returnObject = invocation.invoke();
            //possible do something after calling the original method
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
            Object returnObject = invocation.invoke(); //original method invocation.
            System.out.println("Method exit: " + invocation.getMethodName());
            return returnObject;
        }
    }

    //Roughly what Proxy creates: 
    public class SomeImplCreatedByProxy extends SomeImpl {

        @Override
        public void log(String log) {
            //now it's up to the interceptors to decide what this method should do.
            getInterceptorList().invokeAll();//delegation to original "SomeImpl" is the last interceptor
        }
    }

    //IGNORE these.
    public static Invoke getInterceptorList() {
        return null;
    }
    
    public interface Invoke {
        void invokeAll();
    }
}
