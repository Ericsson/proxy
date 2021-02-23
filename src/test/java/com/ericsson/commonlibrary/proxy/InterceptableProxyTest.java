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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.PersonBean;

public class InterceptableProxyTest {

    Interceptor empty = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            return data.invoke();
        }
    };

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsExceptionOnNonProxyObjects() throws Exception {
        InterceptableProxy proxy = Proxy.getProxyInterface("noProxy");
    }

    @Test
    public void ableToGetInterceptorList() throws Exception {
        PersonBean bean = Proxy.javaBean(PersonBean.class);
        InterceptableProxy proxy = Proxy.getProxyInterface(bean);
        assertFalse(proxy.getInterceptorList().isEmpty());
        // should only contain javabeanInterceptor and no delegate interctor.
        assertEquals(proxy.getInterceptorList().size(), 1);
        assertTrue(proxy.getInterceptorList().peekFirst() instanceof InterceptorJavaBean);

    }

    @Test
    public void ableToAddInterceptors() throws Exception {
        PersonBean bean = Proxy.javaBean(PersonBean.class);
        InterceptableProxy proxy = Proxy.getProxyInterface(bean);
        assertFalse(proxy.getInterceptorList().contains(empty));
        proxy.addInterceptor(empty);
        assertTrue(proxy.getInterceptorList().contains(empty));
    }

    @Test
    public void ableToRemoveInterceptors() throws Exception {
        PersonBean bean = Proxy.javaBean(PersonBean.class);
        InterceptableProxy proxy = Proxy.getProxyInterface(bean);
        assertFalse(proxy.getInterceptorList().contains(empty));
        proxy.addInterceptor(empty);
        assertTrue(proxy.getInterceptorList().contains(empty));
        proxy.removeInterceptor(empty);
        assertFalse(proxy.getInterceptorList().contains(empty));

        // do it again 2X
        proxy.addInterceptor(empty);
        proxy.addInterceptor(empty);
        assertTrue(proxy.getInterceptorList().contains(empty));
        proxy.removeInterceptor(empty);
        assertTrue(proxy.getInterceptorList().contains(empty)); // still one left.
        proxy.removeInterceptor(empty);
        assertFalse(proxy.getInterceptorList().contains(empty));
    }
}
