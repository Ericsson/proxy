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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;

/**
 * Holds the interceptor stack of one byte-buddy generated proxy instance. This is the byte-buddy counterpart of
 * javassist's {@code MethodHandler}.
 */
final class ByteBuddyInterceptorDispatcher {

    private final ArrayDeque<Interceptor> interceptorStack = new ArrayDeque<>();

    /**
     * @param dispatcher
     *            the dispatcher field value of the proxy, {@code null} while a super constructor is still running
     * @param self
     *            the proxy instance
     * @param method
     *            the method that was called
     * @param superMethod
     *            accessor for the real implementation, {@code null} when there is none
     * @param arguments
     *            the arguments of the call
     *
     * @return whatever the interceptor chain or the real implementation returns
     *
     * @throws Throwable
     *             anything the interceptor chain or the real implementation throws
     */
    static Object dispatch(Object dispatcher, Object self, Method method, Method superMethod, Object[] arguments)
            throws Throwable {
        Method accessibleSuperMethod = makeAccessible(superMethod);
        if (dispatcher == null) {
            // A super constructor is calling an overridable method before the proxy is fully wired up. Javassist
            // behaves the same way and simply runs the real implementation.
            return invokeSuper(self, accessibleSuperMethod, method, arguments);
        }
        return ((ByteBuddyInterceptorDispatcher) dispatcher).dispatch(self, method, accessibleSuperMethod, arguments);
    }

    private Object dispatch(Object self, Method method, Method superMethod, Object[] arguments) throws Throwable {
        Object handled = InterceptableProxyInvoker.handleInterceptableProxyMethod(interceptorStack, method, arguments);
        if (handled != InterceptableProxyInvoker.NOT_HANDLED) {
            return handled;
        }
        return new Invocation(self, method, superMethod, arguments, interceptorStack.clone()).invoke();
    }

    private static Object invokeSuper(Object self, Method superMethod, Method method, Object[] arguments)
            throws Throwable {
        if (superMethod == null) {
            throw new UnsupportedOperationException(
                    "There exist's no implementation of method: " + method.getName() + "(...) to delegate to.");
        }
        try {
            return superMethod.invoke(self, arguments);
        } catch (InvocationTargetException e) {
            Util.filterExceptionAndRethrowCorrect(e);
            throw ProxyException.returnThisShouldNeverHappen("Failed to re-throw real exception: ", e);
        }
    }

    @SuppressWarnings({ "deprecation", "squid:S3011" })
    private static Method makeAccessible(Method superMethod) {
        if (superMethod != null && !superMethod.isAccessible()) {
            superMethod.setAccessible(true);
        }
        return superMethod;
    }
}
