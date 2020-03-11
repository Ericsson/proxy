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

import java.io.PrintStream;
import java.lang.reflect.Method;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.GetBuffer;
import com.ericsson.commonlibrary.proxy.helpobjects.InterceptorSystemOut;
import com.ericsson.commonlibrary.proxy.helpobjects.InterceptorSystemOutOnlyPrintln;
import com.ericsson.commonlibrary.proxy.helpobjects.PrintlnDelegate;

public class SystemOutTest {

    private PrintStream out;

    @BeforeClass
    public void beforeClass() {
        out = System.out;
    }

    @BeforeMethod
    public void beforeMethod() throws InterruptedException {
        System.setOut(out);//restore
    }

    //    @Test
    //    public void interceptingSystemOut() throws Exception {
    //        InterceptorSystemOut interceptor = new InterceptorSystemOut();
    //
    //        Method methodToIntercept = System.out.getClass().getMethod("println", String.class);
    //        PrintStream newOut = Proxy.intercept(System.out,
    //                interceptor, methodToIntercept);
    //
    //        System.out.println("1");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), ""); //new system.out not set yet.
    //
    //        System.setOut(newOut);
    //
    //        System.out.println("2");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "2");
    //        System.out.println("3");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "23");
    //        System.out.println("4");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "234");
    //
    //        //only println(String) and not println(int)
    //        System.out.println(5);
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "234");
    //
    //    }
    //
    //    @Test
    //    public void interceptingSystemOut2() throws Exception {
    //        InterceptorSystemOutOnlyPrintln interceptor = new InterceptorSystemOutOnlyPrintln();
    //
    //        PrintStream newOut = Proxy.intercept(System.out,
    //                interceptor); //the interceptor itself filters the method println
    //
    //        System.out.println("1");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), ""); //new system.out not set yet.
    //
    //        System.setOut(newOut);
    //
    //        System.out.println("2");
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "2");
    //
    //        //work not just on string
    //        System.out.println(3);
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "23");
    //
    //        System.out.println(4L);
    //        AssertJUnit.assertEquals(interceptor.getBuffer(), "234");
    //    }
    //
    //    @Test
    //    public void delegateSystemOut() throws Exception {
    //        PrintlnDelegate systemOutDelegate = new PrintlnDelegate();
    //        GetBuffer buffer = systemOutDelegate;
    //
    //        PrintStream newOut = Proxy.delegate(System.out, systemOutDelegate);
    //
    //        System.out.println("1");
    //        AssertJUnit.assertEquals(buffer.getBuffer(), ""); //new system.out not set yet.
    //
    //        System.setOut(newOut);
    //
    //        System.out.println("2");
    //        AssertJUnit.assertEquals(buffer.getBuffer(), "2");
    //        System.out.println("3");
    //        AssertJUnit.assertEquals(buffer.getBuffer(), "23");
    //        System.out.println("4");
    //        AssertJUnit.assertEquals(buffer.getBuffer(), "234");
    //
    //        //only println(String) and not println(int)
    //        System.out.println(5);
    //        AssertJUnit.assertEquals(buffer.getBuffer(), "234");
    //    }

}
