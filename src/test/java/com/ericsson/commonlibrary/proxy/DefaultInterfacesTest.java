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
package com.ericsson.commonlibrary.proxy;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class DefaultInterfacesTest {

    @Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = ".*no implementation of method: getString.*")
    public void interfaceNoImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(InterfaceWithDefault.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println(invocation.getMethodName());
                return invocation.invoke();
            }
        });

        o.getString();
    }

    @Test
    public void interfaceCallsDefaultImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(InterfaceWithDefault.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println(invocation.getMethodName());
                return invocation.invoke();
            }
        });

        assertTrue(1 == o.getParam(1));
        assertTrue(2 == o.getParam(2));
    }

    @Test
    public void interfaceOverideDefaultImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(InterfaceWithDefault.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethodName().contains("getParam")) {
                    return (int) invocation.getParameter0() + 1;
                }
                return invocation.invoke();
            }
        });

        assertTrue(2 == o.getParam(1));
        assertTrue(3 == o.getParam(2));
    }

    @Test
    public void interfaceExtensionNoImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(Extension.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println(invocation.getMethodName());
                return invocation.invoke();
            }
        });

        assertTrue(o.getString().isEmpty());
    }

    @Test
    public void interfaceExtensionCallsDefaultImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(Extension.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println(invocation.getMethodName());
                return invocation.invoke();
            }
        });

        assertTrue(1 == o.getParam(1));
        assertTrue(2 == o.getParam(2));
    }

    @Test
    public void interfaceExtensionOverideDefaultImplementationTest() throws Exception {

        InterfaceWithDefault o = Proxy.intercept(Extension.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethodName().contains("getParam")) {
                    return (int) invocation.getParameter0() + 1;
                }
                return invocation.invoke();
            }
        });

        assertTrue(2 == o.getParam(1));
        assertTrue(3 == o.getParam(2));
    }

    private class Extension implements InterfaceWithDefault {

        @Override
        public String getString() {
            return "";
        }

    }

    private interface InterfaceWithDefault {

        String getString();

        default int getParam(int i) {
            return i;
        }
    }

}
