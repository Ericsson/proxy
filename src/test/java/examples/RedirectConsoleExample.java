package examples;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class RedirectConsoleExample {

    public static void main(String[] args) {
        // Anonymous interceptor class, should usually be put in a separate class.
        Interceptor redirectPrintStream = new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethod().getName().contains("println")) {
                    String classNameWhereItWasCalled = Thread.currentThread().getStackTrace()[1].getClassName();
                    Logger logger = LoggerFactory.getLogger(classNameWhereItWasCalled);
                    logger.info("Console: " + invocation.getParameter0());

                    // The console would be empty because the println was intercepted. removing "return null;" in the
                    // interceptor would mean
                    // that the original println would be called and 123 would appear in both slf4j and in the console.
                    return null;
                }
                return invocation.invoke(); // invokes other methods as normal
            }
        };

        // add the interceptor to the current System.out
        PrintStream newOut = Proxy.intercept(System.out, redirectPrintStream);
        System.setOut(newOut);

        // you can use the interceptor on multiple object.
        PrintStream newErr = Proxy.intercept(System.err, redirectPrintStream);
        System.setErr(newErr);

        System.out.println("123");
        System.err.println("some error");
    }
}
