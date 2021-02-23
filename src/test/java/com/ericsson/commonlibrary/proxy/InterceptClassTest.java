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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.AbstractClassWithImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.ConstructorThrowsException;
import com.ericsson.commonlibrary.proxy.helpobjects.NonEmptyConstructor;
import com.ericsson.commonlibrary.proxy.helpobjects.NonEmptyConstructorWithObject;
import com.ericsson.commonlibrary.proxy.helpobjects.PolymorfismOnClassInterception;
import com.ericsson.commonlibrary.proxy.helpobjects.PrivateConstructor;

public class InterceptClassTest {

    Interceptor size10Interceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            if (data.getMethod().getName().equals("size")) {
                return 10;
            }
            return data.invoke();
        }
    };
    Interceptor return10InterceptorWithoutMethodFiltering = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            return 10;
        }
    };

    Interceptor returnFalseInterceptorWithoutMethodFiltering = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            return false;
        }
    };

    Interceptor sizeTimesTwoInterceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            if (data.getMethod().getName().equals("size")) {
                return ((Integer) data.invoke()) * 2;
            }
            return data.invoke();
        }
    };

    Interceptor emptyInterceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            return data.invoke();
        }
    };

    @Test
    public void multipleOfSameInterceptorThree() throws Exception {
        final List<Integer> interceptorCreatedList = new ArrayList<Integer>();

        Interceptor addInterceptor = new Interceptor() {

            int i = 2;

            @Override
            public Object intercept(Invocation data) throws Throwable {
                if (data.getMethod().getName().equals("add")) {
                    i *= 2;
                    interceptorCreatedList.add(i);
                    Object returnValue = data.invoke();
                    i -= 1;
                    interceptorCreatedList.add(i);
                    return returnValue;
                }
                return data.invoke();
            }
        };

        List<String> list = Proxy.intercept(ArrayList.class, addInterceptor);
        Proxy.intercept(list, addInterceptor);
        Proxy.intercept(list, addInterceptor);
        list.add("hello");

        List<Integer> correctList = new ArrayList<Integer>();
        correctList.add(4);
        correctList.add(8);
        correctList.add(16);
        correctList.add(15);
        correctList.add(14);
        correctList.add(13);

        assertEquals(interceptorCreatedList, correctList);
    }

    @Test
    public void multipleOfSameInterceptorTwo() throws Exception {
        final List<Integer> interceptorCreatedList = new ArrayList<Integer>();

        Interceptor addInterceptor = new Interceptor() {

            int i = 2;

            @Override
            public Object intercept(Invocation data) throws Throwable {
                if (data.getMethod().getName().equals("add")) {
                    i *= 2;
                    interceptorCreatedList.add(i);
                    System.out.println("+");
                    Object returnValue = null;
                    try {
                        returnValue = data.invoke();
                    } catch (Exception e) {
                        System.out.println("-");
                        e.printStackTrace();
                        throw e;
                    }
                    i -= 1;
                    interceptorCreatedList.add(i);
                    return returnValue;
                }
                return data.invoke();
            }
        };

        List<String> list = Proxy.intercept(ArrayList.class, addInterceptor);
        Proxy.intercept(list, addInterceptor);

        // System.out.println("" + list);
        list.add("hello");

        List<Integer> correctList = new ArrayList<Integer>();
        correctList.add(4);
        correctList.add(8);
        correctList.add(7);
        correctList.add(6);

        System.out.println(interceptorCreatedList);
        System.out.println(correctList);
        assertEquals(interceptorCreatedList, correctList);
    }

    @Test
    public void multipleOfSameInterceptorButAnotherInBetween() throws Exception {
        final List<Integer> interceptorCreatedList = new ArrayList<Integer>();

        Interceptor addInterceptor = new Interceptor() {

            int i = 2;

            @Override
            public Object intercept(Invocation data) throws Throwable {
                if (data.getMethod().getName().equals("add")) {
                    i *= 2;
                    interceptorCreatedList.add(i);
                    Object returnValue = data.invoke();
                    i -= 1;
                    interceptorCreatedList.add(i);
                    return returnValue;
                }
                return data.invoke();
            }
        };

        List<String> list = Proxy.intercept(ArrayList.class, addInterceptor);
        Proxy.intercept(list, emptyInterceptor);
        Proxy.intercept(list, addInterceptor);

        list.add("hello");

        List<Integer> correctList = new ArrayList<Integer>();
        correctList.add(4);
        correctList.add(8);
        correctList.add(7);
        correctList.add(6);

        assertEquals(interceptorCreatedList, correctList);
    }

    @Test
    public void oneInterceptor() throws Exception {
        List<String> list = Proxy.intercept(ArrayList.class, size10Interceptor);
        assertEquals(list.size(), 10);
    }

    @Test
    public void ableToInterceptObjectWithNonEmptyConstructor() throws Exception {
        NonEmptyConstructor obj = Proxy.intercept(NonEmptyConstructor.class, emptyInterceptor);
    }

    @Test
    public void ableToInterceptObjectWithNonEmptyConstructorObjectParam() throws Exception {
        NonEmptyConstructorWithObject obj = Proxy.intercept(NonEmptyConstructorWithObject.class, emptyInterceptor);
    }

    @Test
    public void defaultValueIfOnFields() throws Exception {
        NonEmptyConstructor obj = Proxy.intercept(NonEmptyConstructor.class, emptyInterceptor,
                NonEmptyConstructor.class.getMethod("get"));
        assertNotEquals(obj.get(), "");
        assertEquals(obj.get(), null);
    }

    @Test
    public void oneSingleMethodInterceptor() throws Exception {
        List<String> list = Proxy.intercept(ArrayList.class, return10InterceptorWithoutMethodFiltering,
                List.class.getMethod("size"));
        assertEquals(list.size(), 10);
        assertTrue(list.add("hello"));
    }

    @Test
    public void twoSingleMethodInterceptor() throws Exception {
        List<String> list = Proxy.intercept(ArrayList.class, return10InterceptorWithoutMethodFiltering,
                List.class.getMethod("size"));
        assertEquals(list.size(), 10);
        assertTrue(list.add("hello"));

        list = Proxy.intercept(list, returnFalseInterceptorWithoutMethodFiltering,
                List.class.getMethod("add", Object.class));

        assertEquals(list.size(), 10);
        assertFalse(list.add("hello"));
    }

    @Test(expectedExceptions = ProxyException.class)
    public void throwsExceptionFinalObjects() throws Exception {
        String string = Proxy.intercept(String.class, emptyInterceptor);
        fail();
    }

    @Test
    public void failingConstructorNotCalled() throws Exception {
        ConstructorThrowsException obj = Proxy.intercept(ConstructorThrowsException.class, emptyInterceptor);
    }

    @Test
    public void twoInterceptors() {
        List<String> list = Proxy.intercept(ArrayList.class, size10Interceptor);
        Proxy.intercept(list, sizeTimesTwoInterceptor);
        assertEquals(list.size(), 20);

        List<String> list2 = Proxy.intercept(ArrayList.class, sizeTimesTwoInterceptor);
        Proxy.intercept(list2, size10Interceptor);
        assertEquals(list2.size(), 10);
    }

    @Test
    public void sameInterceptorOnTwoObjects() {
        List<String> list = Proxy.intercept(ArrayList.class, size10Interceptor);
        assertEquals(list.size(), 10);

        List<String> list2 = Proxy.intercept(ArrayList.class, size10Interceptor);
        assertEquals(list2.size(), 10);
        assertEquals(list.size(), 10);
    }

    @Test
    public void implInAbstractClassTest() {
        final String notimpl = "notimpl";
        AbstractClassWithImpl impl = Proxy.intercept(AbstractClassWithImpl.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethodName().contains("getNotImpl")) {
                    return notimpl;
                }
                return invocation.invoke();
            }
        });
        assertEquals(impl.getNotImpl(), notimpl);
        assertEquals(impl.getImpl(), "impl");
    }

    @Test
    public void polymorfismBehaviorOnClassInterceptionTest() throws Exception {
        // without interception
        PolymorfismOnClassInterception normalObject = new PolymorfismOnClassInterception();
        assertEquals(normalObject.returnString1(), "1");
        assertEquals(normalObject.returnString2(), "2");
        assertEquals(normalObject.returnString1Times2(), "11");
        assertEquals(normalObject.returnString2Times2(), "22");

        PolymorfismOnClassInterception proxy = Proxy.intercept(PolymorfismOnClassInterception.class, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethodName().equals("returnString1")) {
                    return "3";
                }
                return invocation.invoke();
            }
        });
        assertEquals(proxy.returnString1(), "3");
        assertEquals(proxy.returnString2(), "2");
        assertEquals(proxy.returnString1Times2(), "33");
        assertEquals(proxy.returnString2Times2(), "22");
    }

    @Test
    public void canProxyClassWithPrivateConstructorTest() throws Exception {
        PrivateConstructor pc = Proxy.intercept(PrivateConstructor.class, emptyInterceptor);
        assertFalse(pc.wasConstuctorCalled()); // constructor should not be called.
    }
}
