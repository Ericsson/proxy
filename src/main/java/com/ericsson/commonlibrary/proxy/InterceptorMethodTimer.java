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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Interceptor that measures execution time of every method invocation it intercepts This could be useful to for
 * performance debugging purposes by adding it to specific objects/classes that you think are likely to be sucking up
 * performance
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptorMethodTimer implements Interceptor {

    private static final int LARGEST_ARG_STRING = 20;
    private static final int NANO_PER_MILLI = 1000000;
    private static final Logger LOG = LoggerFactory.getLogger(InterceptorMethodTimer.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.nanoTime();
        Object result = invocation.invoke();
        long end = System.nanoTime();
        printTimeMessage(invocation, start, end);
        return result;
    }

    private void printTimeMessage(Invocation invocation, long start, long end) {
        long timeNs = end - start;
        long timeMs = timeNs / NANO_PER_MILLI;
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("%s.%s(%s) took %d ns | %s ms", getTargetClass(invocation),
                    invocation.getMethod().getName(), createArgsString(invocation.getParameters()), timeNs, timeMs));
        }
    }

    private String getTargetClass(Invocation invocation) {
        String name = invocation.getThis().getClass().getSimpleName();
        return name.replaceAll("_.*\\d", "");
    }

    private String createArgsString(Object... args) {
        StringBuilder argsString = new StringBuilder();
        for (Object object : args) {
            if (object.toString().length() > LARGEST_ARG_STRING) {
                if (object.getClass().getSimpleName().isEmpty()) {
                    argsString.append("UNKNOWN");
                } else {
                    argsString.append(object.getClass().getSimpleName());
                }
            } else {
                argsString.append(object.toString());
            }
            argsString.append(",");
        }
        return argsString.toString();
    }

}
