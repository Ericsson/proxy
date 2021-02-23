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
package com.ericsson.commonlibrary.proxy.issues;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class DisabledIssueMissingGenericTypeInfoForInterceptedTest {

    // TODO should work when https://issues.jboss.org/browse/JASSIST-219 is fixed.
    @Test(enabled = false)
    public void verifySameReturnTypeObject() throws SecurityException, NoSuchMethodException {
        MinimalObject minimal = new MinimalObject();
        Method method = minimal.getClass().getMethod("getReferences");
        final String typeBefore = method.toGenericString().toString();
        System.out.println("before     :" + typeBefore);

        MinimalObject modified = Proxy.intercept(minimal, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println("interceptor:" + invocation.getMethod().toGenericString());
                System.out.println("interceptor:" + invocation.getMethodName());
                Assert.assertEquals(invocation.getMethod().toGenericString(), typeBefore);
                return null;
            }
        });
        modified.getReferences();

        method = modified.getClass().getMethod("getReferences");
        System.out.println("after      :" + method.toGenericString());
        String typeAfter = method.getGenericReturnType().toString();
        Assert.assertEquals(typeAfter, typeBefore);

    }

    // TODO should work when https://issues.jboss.org/browse/JASSIST-219 is fixed.
    @Test(enabled = false)
    public void verifySameReturnTypeInterface() throws SecurityException, NoSuchMethodException {
        Method method = MinimalInterface.class.getMethod("getReferences");
        final String typeBefore = method.toGenericString().toString();
        System.out.println("before     :" + typeBefore);

        MinimalInterface modified = Proxy.intercept(MinimalInterface.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                System.out.println("interceptor:" + invocation.getMethod().toGenericString());
                System.out.println("interceptor:" + invocation.getMethodName());
                Assert.assertEquals(invocation.getMethod().toGenericString(), typeBefore);
                return null;
            }
        });

        modified.getReferences();

        method = modified.getClass().getMethod("getReferences");
        System.out.println("after      :" + method.toGenericString());
        String typeAfter = method.getGenericReturnType().toString();
        Assert.assertEquals(typeAfter, typeBefore);

    }

    @Test
    public void verifySameReturnTypeLocalExt() throws SecurityException, NoSuchMethodException {
        MinimalObject minimal = new MinimalObject();
        Method method = minimal.getClass().getMethod("getReferences");
        String typeBefore = method.getGenericReturnType().toString();

        System.out.println("before     :" + method.toGenericString());

        MinimalObject modified = new MinimalObject() {

            @Override
            public List<? extends MinimalReference> getReferences() {
                return null;
            }
        };

        method = modified.getClass().getMethod("getReferences");
        System.out.println("after      :" + method.toGenericString());
        String typeAfter = method.getGenericReturnType().toString();
        Assert.assertEquals(typeAfter, typeBefore);

    }

    public static class MinimalObject {

        public List<? extends MinimalReference> getReferences() {
            List<MinimalReference> result = new ArrayList<MinimalReference>();
            result.add(new MinimalReference());
            return result;
        }
    }

    public static interface MinimalInterface {

        public List<? extends MinimalReference> getReferences();
    }

    public static class MinimalReference {

        public MinimalReference() {
        }
    }
}
