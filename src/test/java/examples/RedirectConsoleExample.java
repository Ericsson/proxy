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
package examples;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class RedirectConsoleExample {

    public static void main(String[] args) {
        //Anonymous interceptor class, should usually be put in a separate class.
        Interceptor redirectPrintStream = new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethod().getName().contains("println")) {
                    String classNameWhereItWasCalled = Thread.currentThread().getStackTrace()[1]
                            .getClassName();
                    Logger logger = LoggerFactory.getLogger(classNameWhereItWasCalled);
                    logger.info("Console: " + invocation.getParameter0());

                    //The console would be empty because the println was intercepted. removing "return null;" in the interceptor would mean
                    //that the original println would be called and 123 would appear in both slf4j and in the console.
                    return null;
                }
                return invocation.invoke(); //invokes other methods as normal
            }
        };

        //add the interceptor to the current System.out
        PrintStream newOut = Proxy.intercept(System.out, redirectPrintStream);
        System.setOut(newOut);

        //you can use the interceptor on multiple object.
        PrintStream newErr = Proxy.intercept(System.err, redirectPrintStream);
        System.setErr(newErr);

        System.out.println("123");
        System.err.println("some error");
    }
}
