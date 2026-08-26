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

import java.lang.reflect.Method;
import java.util.Deque;

/**
 * Implements the three {@link InterceptableProxy} methods on behalf of every engine, so that they behave identically no
 * matter which engine generated the proxy.
 */
final class InterceptableProxyInvoker {

    /**
     * Returned when the intercepted method is not one of the {@link InterceptableProxy} methods. A dedicated sentinel
     * is needed because {@code null} is a valid return value.
     */
    static final Object NOT_HANDLED = new Object();

    private static Method addInterceptorMethod;
    private static Method removeInterceptorMethod;
    private static Method getInterceptorListMethod;

    static {
        try {
            addInterceptorMethod = InterceptableProxy.class.getMethod("addInterceptor", Interceptor.class);
            removeInterceptorMethod = InterceptableProxy.class.getMethod("removeInterceptor", Interceptor.class);
            getInterceptorListMethod = InterceptableProxy.class.getMethod("getInterceptorList");
        } catch (SecurityException | NoSuchMethodException e) {
            ProxyException.throwThisShouldNeverHappen(e);
        }
    }

    private InterceptableProxyInvoker() {
        // hidden
    }

    static Object handleInterceptableProxyMethod(Deque<Interceptor> interceptorStack, Method method, Object[] args) {
        if (Util.methodSignatureEquals(method, addInterceptorMethod)) {
            interceptorStack.push((Interceptor) args[0]);
            return null;
        }
        if (Util.methodSignatureEquals(method, removeInterceptorMethod)) {
            interceptorStack.remove(args[0]);
            return null;
        }
        if (Util.methodSignatureEquals(method, getInterceptorListMethod)) {
            return interceptorStack;
        }
        return NOT_HANDLED;
    }
}
