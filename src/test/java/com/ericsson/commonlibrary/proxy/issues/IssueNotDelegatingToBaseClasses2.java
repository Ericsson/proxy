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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

/**
 * Not delegating correctly in cases of a base class. Was a problem with latest javaassit 3.18. going back to 3.12
 * solves the problem. This is also solved in: 3.21.0-GA
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
public class IssueNotDelegatingToBaseClasses2 {

    @Test
    public void delegateToBaseClassTest() throws Exception {
        Extended obj = Proxy.intercept(new Extended("theValue"), new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                return invocation.invoke();
            }
        });
        doAsserts(obj, "with proxy");
    }

    @Test
    public void noProxyTest() throws Exception {
        Extended obj = new Extended("theValue");
        doAsserts(obj, "without proxy");
    }

    private void doAsserts(Extended obj, String suffix) {
        Assert.assertEquals(obj.extendedMethod(), "theValue-extended");
        System.out.println("The extended method works " + suffix);

        Assert.assertEquals(obj.baseMethod(), "theValue");
        System.out.println("The base method works " + suffix);
    }

    private static abstract class Base {

        protected String returnValue;

        Base(String returnValue) {
            System.out.println("Constructor param:" + returnValue);
            Thread.dumpStack();
            this.returnValue = returnValue;
        }

        public String baseMethod() {
            Thread.dumpStack();
            System.out.println("baseMethod returnValue:" + returnValue);
            return returnValue;
        }
    }

    public static class Extended extends Base {

        public Extended(String returnValue) {
            super(returnValue);
        }

        public String extendedMethod() {
            System.out.println("returnValue Of baseMethod in Extended: " + baseMethod());
            System.out.println("value Of returnValue var in Extended: " + returnValue);
            return returnValue + "-extended";
        }
    }
}
