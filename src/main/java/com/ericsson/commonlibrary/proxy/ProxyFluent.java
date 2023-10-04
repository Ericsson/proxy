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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A fluent API to intercerpt an object or class
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 *
 * @param <T>
 *            the actual type of the proxy object being built.
 */
public final class ProxyFluent<T> {

    private InterceptableProxy proxy;

    /*
     * TODO proxy(class).constructorWithArgs(args...) proxy(class).recursiveSafeApplyOnReturnValues()
     * proxy().defaultConstructorOnly() proxy().setClassnamePrefix proxy().setClassnameSuffix
     * proxy.build(wantedInterface)
     */
    // TODO do not create the proxy until get() is called, to enable delegate() to be able to behave polymorphically on
    // Class proxies

    ProxyFluent(T o) {
        proxy = (InterceptableProxy) InterceptableProxyFactory.createANewObjectProxyIfNeeded(o);
    }

    ProxyFluent(Class<T> clazz) {
        if (clazz.isInterface()) {
            proxy = InterceptableProxyFactory.createANewInterfaceProxy(clazz);
        } else { // was a class
            proxy = (InterceptableProxy) InterceptableProxyFactory.createANewClassProxy(clazz);
        }
    }

    ProxyFluent(Class<T> clazz, Object... constructorArgs) {
        if (clazz.isInterface()) {
            throw new ProxyException("the provided interface: " + clazz.getName()
                    + " does not need constructor arguments as it can not use them!");
        } else { // was a class
            proxy = (InterceptableProxy) InterceptableProxyFactory.createANewClassProxyWithArguments(clazz,
                    constructorArgs);
        }
    }

    /**
     * @return the intercepted proxy.
     */
    public T get() {
        return (T) proxy;
    }

    /**
     * Add an {@link Interceptor} that intercepts all methods.
     *
     * @param interceptor
     *            to add
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptAll(Interceptor interceptor) {
        Proxy.intercept(proxy, interceptor);
        return this;
    }

    /**
     * Add an {@link InterceptorConsumer} is adapted to {@link Interceptor} without a return value, that intercepts all
     * methods.
     *
     * @param interceptor
     *            to add
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptAll(InterceptorConsumer interceptor) {
        Proxy.intercept(proxy, (i) -> {
            interceptor.intercept(i);
            return null;
        });
        return this;
    }

    /**
     * Add an {@link InterceptorConsumer} is adapted to {@link Interceptor} without a return value, that intercepts one
     * or more specific methods
     *
     * @param interceptor
     *            to add
     * @param methodsToIntercept
     *            varargs of the methods you want the interceptor to intercept. Specifying none means that it will
     *            intercept all.
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptMethod(InterceptorConsumer interceptor, Method... methodsToIntercept) {
        Proxy.intercept(proxy, (i) -> {
            interceptor.intercept(i);
            return null;
        }, methodsToIntercept);
        return this;
    }

    /**
     * Add an {@link Interceptor} that intercepts one or more specific methods
     *
     * @param interceptor
     *            to add
     * @param methodsToIntercept
     *            varargs of the methods you want the interceptor to intercept. Specifying none means that it will
     *            intercept all.
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptMethod(Interceptor interceptor, Method... methodsToIntercept) {
        Proxy.intercept(proxy, interceptor, methodsToIntercept);
        return this;
    }

    /**
     * Add an {@link InvocationHandler} that intercepts all methods.
     *
     * @param interceptor
     *            to add
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptAll(InvocationHandler interceptor) {
        Proxy.intercept(proxy, interceptor);
        return this;
    }

    /**
     * Add an {@link InvocationHandler} that intercepts one or more specific methods
     *
     * @param interceptor
     *            to add
     * @param methodsToIntercept
     *            varargs of the methods you want the interceptor to intercept. Specifying none means that it will
     *            intercept all.
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> interceptMethod(InvocationHandler interceptor, Method... methodsToIntercept) {
        Proxy.intercept(proxy, interceptor, methodsToIntercept);
        return this;
    }

    /**
     * Delegate all method calls to the provided delegator objects passed in as parameters. Warning your proxy will not
     * behave polymorphically.
     *
     * @param delegates
     *            objects to merge into one.
     *
     * @return the API itself (used for chaining)
     */
    public ProxyFluent<T> delegate(Object... delegates) {
        Proxy.delegate(proxy, delegates);
        return this;
    }
}
