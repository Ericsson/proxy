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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.JavaBean;
import com.ericsson.commonlibrary.proxy.helpobjects.JavaBeanAbstract;
import com.ericsson.commonlibrary.proxy.helpobjects.JavaBeanAbstractWithSet;

public class JavaBeanWithoutSettersProxyTest {

    @Test
    public void javaBeanSetMethodExists() {
        JavaBean proxyBean = Proxy.javaBean(JavaBean.class);
        assertNull(proxyBean.getName());
        invokeSetName(proxyBean, "nisse");
        assertEquals(proxyBean.getName(), "nisse");
        invokeSetName(proxyBean, "kalle");
        assertEquals(proxyBean.getName(), "kalle");

    }

    @Test
    public void javaBeanInnerStaticTwice() {
        JavaBean2 proxyBean = Proxy.javaBean(JavaBean2.class);
        JavaBean2 proxyBean2 = Proxy.javaBean(JavaBean2.class);

        assertNull(proxyBean.getName());
        assertNull(proxyBean2.getName());

        invokeSetName(proxyBean, "nisse");
        invokeSetName(proxyBean2, "nisse2");
        assertEquals(proxyBean.getName(), "nisse");
        assertEquals(proxyBean2.getName(), "nisse2");
        invokeSetName(proxyBean, "kalle");
        invokeSetName(proxyBean2, "kalle2");
        assertEquals(proxyBean.getName(), "kalle");
        assertEquals(proxyBean2.getName(), "kalle2");
    }

    @Test
    public void javaBeanInnerStatic() {
        JavaBean2 proxyBean = Proxy.javaBean(JavaBean2.class);
        assertNull(proxyBean.getName());
        invokeSetName(proxyBean, "nisse");
        assertEquals(proxyBean.getName(), "nisse");
        invokeSetName(proxyBean, "kalle");
        assertEquals(proxyBean.getName(), "kalle");
    }

    @Test
    public void javaBeanInner() {
        JavaBean3 proxyBean = Proxy.javaBean(JavaBean3.class);
        assertNull(proxyBean.getName());
        invokeSetName(proxyBean, "nisse");
        assertEquals(proxyBean.getName(), "nisse");
        invokeSetName(proxyBean, "kalle");
        assertEquals(proxyBean.getName(), "kalle");
    }

    @Test
    public void javaBeanAbstract() {
        JavaBeanAbstract proxyBean = Proxy.javaBean(JavaBeanAbstract.class);
        assertNull(proxyBean.getName());
        invokeSetName(proxyBean, "nisse");
        assertEquals(proxyBean.getName(), "nisse");
        invokeSetName(proxyBean, "kalle");
        assertEquals(proxyBean.getName(), "kalle");
    }

    @Test
    public void javaBeanAbstractSetMethodExists() {
        JavaBeanAbstractWithSet proxyBean = Proxy.javaBean(JavaBeanAbstractWithSet.class);
        assertNull(proxyBean.getName());
        invokeSetName(proxyBean, "nisse");
        assertEquals(proxyBean.getName(), "nisse");
        invokeSetName(proxyBean, "kalle");
        assertEquals(proxyBean.getName(), "kalle");
    }

    @Test
    public void javaBeanIsMethod() {
        JavaBeanIsMethod proxyBean = Proxy.javaBean(JavaBeanIsMethod.class);
        assertFalse(proxyBean.isTrue());
        invokeSetTrue(proxyBean, true);
        assertTrue(proxyBean.isTrue());
        invokeSetTrue(proxyBean, false);
        assertFalse(proxyBean.isTrue());
    }

    private static void invokeSetName(Object proxyBean, String name) {
        Method method = null;
        try {
            method = proxyBean.getClass().getMethod("setName", String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            method.invoke(proxyBean, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void invokeSetTrue(Object proxyBean, boolean bool) {
        Method method = null;
        try {
            method = proxyBean.getClass().getMethod("setTrue", boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            method.invoke(proxyBean, bool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
