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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Represents a single invocation of a method. It also holds all related data to the invocations with could be useful
 * for the interceptor that wants to alter the invocation behavior in some way.
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
public final class Invocation {

    private final Object target;
    private final Object[] parameters;
    private final Deque<Interceptor> interceptorStack;
    private final Method method;
    private final Method proceed;

    private static Method interceptMethod;
    static {
        try {
            interceptMethod = Interceptor.class.getMethod("intercept", Invocation.class);
        } catch (SecurityException | NoSuchMethodException e) {
            ProxyException.throwThisShouldNeverHappen(e);
        }
    }

    Invocation(Object target, Method method, Method proceed, Object[] targetArgs, Deque<Interceptor> interceptorStack) {
        this.target = target;
        this.method = method;
        this.proceed = proceed;
        this.parameters = targetArgs;
        this.interceptorStack = interceptorStack;
    }

    /**
     * @return the object that was intercepted.
     */
    public Object getThis() {
        return target;
    }

    /**
     * @return the method that was intercepted.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return the methodName of the method that was intercepted.
     */
    public String getMethodName() {
        return method.getName();
    }

    /**
     * @return passed parameters to the intercepted method.
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @return the first parameter.
     */
    public Object getParameter0() {
        return parameters[0];
    }

    /**
     * @return the second parameter.
     */
    public Object getParameter1() {
        return parameters[1];
    }

    /**
     * @return the third parameter.
     */
    public Object getParameter2() {
        return parameters[2];
    }

    /**
     * Invokes the method. Which may be another interceptor or the concrete object
     *
     * @return the return value from next interceptor or original object method call.
     *
     * @throws Throwable
     *             any type of exception/error including actual ones from the method called.
     */
    public Object invoke() throws Throwable {

        if (interceptorStack.isEmpty()) {
            if (Util.isMethodWithImplementation(method)) {
                try {
                    return proceed.invoke(target, parameters); // invoke original
                } catch (InvocationTargetException e) {
                    Util.filterExceptionAndRethrowCorrect(e);
                }
            }
            throw new UnsupportedOperationException(
                    "There exist's no implementation of method: " + this.getMethodName() + "(...) to delegate to.");
        }

        // TODO handle if null was returned for a primitive.
        try {
            // if concrete object has been used for proxy creation the last interceptor is a InterceptorDelegator to
            // this object
            return interceptMethod.invoke(interceptorStack.pop(), this); // invokes next interceptor.
        } catch (InvocationTargetException e) {
            Util.filterExceptionAndRethrowCorrect(e);
            throw ProxyException.returnThisShouldNeverHappen("Failed to re-throw real exception: ", e);
        }
    }

}
