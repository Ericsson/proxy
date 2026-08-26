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

import com.ericsson.commonlibrary.proxy.ProxyConfiguration;
import com.ericsson.commonlibrary.proxy.ProxyConfiguration.Engine;

public class ProxyEngineExample {

    public static class SomeImpl { // sample class

        public String greet(String name) {
            return "hello " + name;
        }
    }

    public static void main(String[] args) throws Exception {

        // Javassist is the default engine, nothing has to be configured.
        System.out.println(intercepted().greet("javassist"));
        // Console output:
        // before greet
        // hello javassist

        // Switch the whole JVM over to byte-buddy. The API and the behavior are identical.
        ProxyConfiguration.setEngine(Engine.BYTEBUDDY);
        System.out.println(intercepted().greet("bytebuddy"));
        // Console output:
        // before greet
        // hello bytebuddy

        // The generated classes differ, the behavior does not.
        System.out.println(intercepted().getClass().getName());
        // Console output:
        // examples.ProxyEngineExample_SomeImpl$ProxyByByteBuddy$2

        // Back to whatever -Dproxy.engine says, or javassist when it says nothing.
        ProxyConfiguration.reset();

        // A single call can pick its own engine without disturbing any other thread.
        SomeImpl onlyThisOne = ProxyConfiguration.callWithEngine(Engine.BYTEBUDDY, ProxyEngineExample::intercepted);
        System.out.println(onlyThisOne.greet("scoped"));
        // Console output:
        // before greet
        // hello scoped
    }

    private static SomeImpl intercepted() {
        return with(SomeImpl.class).interceptAll(i -> {
            System.out.println("before " + i.getMethodName());
            return i.invoke();
        }).get();
    }
}
