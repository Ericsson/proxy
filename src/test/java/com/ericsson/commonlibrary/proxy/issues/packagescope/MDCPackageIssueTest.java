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
package com.ericsson.commonlibrary.proxy.issues.packagescope;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.Proxy;
import com.ericsson.commonlibrary.proxy.ProxyException;

public class MDCPackageIssueTest {

    private static final String KEY = "id";

    @Test(expectedExceptions = { ProxyException.class })
    public void testFinalClass() {
        PublicInterface f = ClassFactory.newFinalClass();
        f = Proxy.mdcLogging(f, KEY, FinalClass.class.getSimpleName());
        f.foo();
    }

    @Test
    public void testPublicClass() {
        PublicInterface f = ClassFactory.newPublicClass();
        f = Proxy.mdcLogging(f, KEY, PublicClass.class.getSimpleName());
        Assert.assertNotNull(f);
        f.foo();
    }

    @Test(expectedExceptions = { ProxyException.class })
    public void testPublicFinalClass() {
        PublicInterface f = ClassFactory.newPublicFinalClass();
        f = Proxy.mdcLogging(f, KEY, PublicFinalClass.class.getSimpleName());
    }

    @Test
    public void testPackageClass() {
        PublicInterface f = ClassFactory.newPackageClass();
        f = Proxy.mdcLogging(f, KEY, PackageClass.class.getSimpleName());
        Assert.assertNotNull(f);
        f.foo();
    }

    @Test
    public void testPackageClassWithDelegate() {
        PublicInterface f = ClassFactory.newPublicFinalClass();
        PublicInterface newF = Proxy.delegate(PublicInterface.class, f);
        newF = Proxy.mdcLogging(newF, KEY, PackageClass.class.getSimpleName());
        Assert.assertNotNull(newF);
        newF.foo();
    }

    @Test
    public void testPackageClassWithChangingInterface() {
        PublicInterface f = ClassFactory.newPublicFinalClass();
        PublicInterface newF = Proxy.changeInterface(PublicInterface.class, f);
        newF = Proxy.mdcLogging(newF, KEY, PackageClass.class.getSimpleName());
        Assert.assertNotNull(newF);
        newF.foo();
    }

    @Test
    public void testPackageMethodMdcIssue() {
        SmallClass small = new SmallClass();
        small.inList("");
        small.contains("");
        small = Proxy.mdcLogging(small, "id", "any");
        small.inList("");
        small.contains(""); //This throwed exception before
    }

    public static class SmallClass {

        private List<String> objectList = new ArrayList<String>();

        public boolean inList(String s) {
            return objectList.contains(s);
        }

        boolean contains(String s) {
            return objectList.contains(s);
        }
    }

    //    @Test
    //    public void testFind() throws NoSuchMethodException, SecurityException {
    //        PackageMethod f = ClassFactory.newPackageMethod();
    //
    //        f = Proxy.intercept(f, new Interceptor() {
    //
    //            @Override
    //            public Object intercept(Invocation invocation) throws Throwable {
    //                System.out.println("m:" + invocation.getMethodName());
    //                return invocation.invoke();
    //            }
    //        });
    //        f.foo();
    //        Method method = f.getClass().getDeclaredMethod("foo");
    //        Method findMethodWithSignatureInClass = Util.findMethodWithSignatureInClass(method, PackageMethod.class);
    //        System.out.println(Util.isToStringOrHashcodeOrEqualsMethod(method));
    //        System.out.println(findMethodWithSignatureInClass);
    //    }
}
