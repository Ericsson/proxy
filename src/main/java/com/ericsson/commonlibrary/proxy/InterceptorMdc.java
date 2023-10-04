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
import org.slf4j.MDC;

/**
 * A {@link Interceptor} that add/restores/removes MDC logging information to the context of your object and its child
 * objects
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptorMdc implements Interceptor {

    // TODO use recursiveIntercept instead, InterceptorMdc have to be modified.
    private static final Logger LOG = LoggerFactory.getLogger(InterceptorMdc.class);

    private final String key;
    private final String value;

    public InterceptorMdc(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String originalValue = MDC.get(key);
        MDC.put(key, value);
        try {
            return Util.tryToAddInterceptorToObject(invocation.invoke(), this, invocation, false);
        } finally {
            if (originalValue == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, originalValue);
            }
        }
    }
}
