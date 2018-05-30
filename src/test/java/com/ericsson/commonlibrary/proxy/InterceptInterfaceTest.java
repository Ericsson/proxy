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

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.PersonBean;
import com.ericsson.commonlibrary.proxy.helpobjects.PersonBeanClass;

public class InterceptInterfaceTest {

    Interceptor interceptor = new Interceptor() {

        @Override
        public Object intercept(Invocation data) throws Throwable {
            return data.invoke();
        }
    };

    @Test
    public void interceptToString17TimesIssue() throws Exception {
        PersonBean bean = Proxy.intercept(PersonBean.class,
                interceptor);
        for (int i = 0; i < 10; i++) {
            System.out.println("i:" + i + bean.toString());
        }
        Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            System.out.println("i:" + i + bean.toString());
        }
    }

    @Test
    public void interceptToString17TimesIssueClass() throws Exception {
        PersonBeanClass bean = Proxy.intercept(PersonBeanClass.class,
                interceptor);
        for (int i = 0; i < 10; i++) {
            System.out.println("i:" + i + bean.toString());
        }
        Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            System.out.println("i:" + i + bean.toString());
        }
    }

}
