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

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Every method of a byte-buddy generated proxy is compiled into a call to {@link #dispatch}. The class therefore has to
 * be public, because a generated proxy class is placed in the package of the type it proxies, which is normally not the
 * package of this library.
 * <p>
 * <b>This class is an implementation detail and is not part of the supported API. Never call it directly.</b>
 */
public final class ByteBuddyProxyDispatcher {

    /**
     * Name of the synthetic field that holds the interceptor state of a single proxy instance.
     */
    public static final String DISPATCHER_FIELD = "proxy$interceptorDispatcher";

    private ByteBuddyProxyDispatcher() {
        // hidden
    }

    /**
     * Called by every method of a byte-buddy generated proxy.
     *
     * @param self
     *            the proxy instance
     * @param dispatcher
     *            value of the {@value #DISPATCHER_FIELD} field, {@code null} while a super constructor is still running
     * @param method
     *            the method that was called
     * @param superMethod
     *            an accessor for the real implementation, {@code null} when there is none
     * @param arguments
     *            the arguments of the call
     *
     * @return whatever the interceptor chain or the real implementation returns
     *
     * @throws Throwable
     *             anything the interceptor chain or the real implementation throws
     */
    @RuntimeType
    public static Object dispatch(@This Object self, @FieldValue(DISPATCHER_FIELD) Object dispatcher,
            @Origin Method method, @SuperMethod(nullIfImpossible = true) Method superMethod,
            @AllArguments Object[] arguments) throws Throwable {
        return ByteBuddyInterceptorDispatcher.dispatch(dispatcher, self, method, superMethod, arguments);
    }
}
