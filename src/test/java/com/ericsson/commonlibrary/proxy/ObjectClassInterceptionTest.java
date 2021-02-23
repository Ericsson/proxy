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

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.Equals;
import com.ericsson.commonlibrary.proxy.helpobjects.HashCode;
import com.ericsson.commonlibrary.proxy.helpobjects.MyInterface;
import com.ericsson.commonlibrary.proxy.helpobjects.MySubImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.MySubImpl2;
import com.ericsson.commonlibrary.proxy.helpobjects.PersonBean;
import com.ericsson.commonlibrary.proxy.helpobjects.ToString;

public class ObjectClassInterceptionTest {

    @Test
    public void interfaceDelegateDefaultObjectMethodIfNotOverriden() throws Exception {
        final MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new MySubImpl());
        assertTrue(interfaceDelegator.toString().contains("MyInterface"));
        assertTrue(interfaceDelegator.equals(interfaceDelegator));

        final MyInterface interfaceDelegator2 = Proxy.delegate(MyInterface.class, new MySubImpl2(), new MySubImpl());
        assertFalse(interfaceDelegator.equals(interfaceDelegator2));

        assertNotEquals(interfaceDelegator.hashCode(), interfaceDelegator2.hashCode());

    }

    @Test
    public void interfaceDelegateToString() throws Exception {
        final MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new ToString(),
                new MySubImpl());
        assertEquals(interfaceDelegator.toString(), "ToString");
    }

    @Test
    public void objectDelegateToString() throws Exception {
        final ToString toString = new ToString("NOTPROXY");
        final ToString toStringProxy = Proxy.delegate(toString, new HashCode(123456), new ToString("PROXY"));
        assertEquals(toString.toString(), "NOTPROXY");
        assertEquals(toStringProxy.toString(), "PROXY");
        assertEquals(toStringProxy.hashCode(), 123456);
        assertNotEquals(toStringProxy.hashCode(), toString.hashCode());
    }

    @Test
    public void interfaceDelegateEquals() throws Exception {
        // both true
        MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(true),
                new MySubImpl());

        MyInterface interfaceDelegator2 = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(true),
                new MySubImpl());

        assertTrue(interfaceDelegator.equals(interfaceDelegator2));
        assertTrue(interfaceDelegator.equals(interfaceDelegator));
        assertTrue(interfaceDelegator2.equals(interfaceDelegator));
        assertTrue(interfaceDelegator2.equals(interfaceDelegator2));

        // One false.
        interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(true), new MySubImpl());

        interfaceDelegator2 = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(false), new MySubImpl());

        assertTrue(interfaceDelegator.equals(interfaceDelegator2));
        assertTrue(interfaceDelegator.equals(interfaceDelegator));
        assertFalse(interfaceDelegator2.equals(interfaceDelegator));
        assertFalse(interfaceDelegator2.equals(interfaceDelegator2));

        // both false.
        interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(false), new MySubImpl());

        interfaceDelegator2 = Proxy.delegate(MyInterface.class, new MySubImpl2(), new Equals(false), new MySubImpl());

        assertFalse(interfaceDelegator.equals(interfaceDelegator2));
        assertFalse(interfaceDelegator.equals(interfaceDelegator));
        assertFalse(interfaceDelegator2.equals(interfaceDelegator));
        assertFalse(interfaceDelegator2.equals(interfaceDelegator2));
    }

    @Test
    public void objectDelegateEquals() throws Exception {

        // both true
        Equals equals = new Equals(false);
        Equals equalsProxy = Proxy.delegate(equals, new MySubImpl2(), new Equals(true), new MySubImpl());
        Equals equalsProxy2 = Proxy.delegate(equals, new MySubImpl2(), new Equals(true), new MySubImpl());

        assertTrue(equalsProxy.equals(equalsProxy));
        assertTrue(equalsProxy.equals(equalsProxy2));

        assertTrue(equalsProxy2.equals(equalsProxy2));
        assertTrue(equalsProxy2.equals(equalsProxy));

        // One false.
        // both false.
        equals = new Equals(true);
        equalsProxy = Proxy.delegate(equals, new MySubImpl2(), new Equals(true), new MySubImpl());
        equalsProxy2 = Proxy.delegate(equals, new MySubImpl2(), new Equals(false), new MySubImpl());

        assertTrue(equalsProxy.equals(equalsProxy));
        assertTrue(equalsProxy.equals(equalsProxy2));

        assertFalse(equalsProxy2.equals(equalsProxy2));
        assertFalse(equalsProxy2.equals(equalsProxy));

        // both false.
        equals = new Equals(true);
        equalsProxy = Proxy.delegate(equals, new MySubImpl2(), new Equals(false), new MySubImpl());
        equalsProxy2 = Proxy.delegate(equals, new MySubImpl2(), new Equals(false), new MySubImpl());

        assertFalse(equalsProxy.equals(equalsProxy));
        assertFalse(equalsProxy.equals(equalsProxy2));

        assertFalse(equalsProxy2.equals(equalsProxy2));
        assertFalse(equalsProxy2.equals(equalsProxy));

    }

    @Test
    public void interfaceDelegateHashCode() throws Exception {

        final MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl2(), new HashCode(123456),
                new MySubImpl());

        assertEquals(interfaceDelegator.hashCode(), 123456);
        assertEquals(interfaceDelegator.hashCode(), 123456);
    }

    @Test
    public void objectDelegateHashCode() throws Exception {
        final HashCode hashCode = new HashCode(111111);
        final HashCode hashCodeProxy = Proxy.delegate(hashCode, new HashCode(123456));
        assertEquals(hashCode.hashCode(), 111111);
        assertEquals(hashCodeProxy.hashCode(), 123456);
    }

    @Test
    public void objectDelegateHashCode2() throws Exception {
        final HashCode hashCode = new HashCode(111111);
        final HashCode hashCodeProxy = Proxy.intercept(hashCode, new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                return invocation.invoke();
            }
        });

        assertEquals(hashCode.hashCode(), hashCodeProxy.hashCode());
    }

    @Test
    public static void javaBeanToStringTest() throws Exception {
        final PersonBean person = Proxy.javaBean(PersonBean.class);
        person.setName("elis");
        person.setMale(true);
        assertEquals(person.getName(), "elis");
        assertTrue(person.isMale());

        assertFalse(person.toString().isEmpty());
        assertTrue(person.toString().contains("PersonBean"));
    }
}
