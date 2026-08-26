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

/**
 * Strategy interface for the bytecode manipulation engine that backs the proxy library.
 * <p>
 * Every implementation has to fulfil the exact same contract so that the observable behavior of the public
 * {@link Proxy} API is independent of the selected engine:
 * <ul>
 * <li>Every returned proxy implements {@link InterceptableProxy}.</li>
 * <li>{@code toString()}, {@code hashCode()} and {@code equals(Object)} are intercepted, all other {@link Object}
 * methods are not.</li>
 * <li>An {@link Invocation} handed to an {@link Interceptor} is able to proceed to the real (super) implementation when
 * one exists, and throws {@link UnsupportedOperationException} when it does not.</li>
 * </ul>
 *
 * @see ProxyConfiguration
 */
interface ProxyEngineFactory {

    /**
     * Creates a new proxy implementing the given interfaces.
     *
     * @param <T>
     *            the type of the proxy
     * @param interfaces
     *            the interfaces to implement. The first element is used as the "host" type that decides in which
     *            package the generated class ends up.
     *
     * @return a new proxy instance
     */
    <T> T createInterfaceProxy(Class<?>... interfaces);

    /**
     * Creates a new proxy for the given interface with generated setter methods for every getter that is missing one.
     * The generated setters are only reachable through reflection.
     *
     * @param <T>
     *            the type of the proxy
     * @param toJavaBeanify
     *            the interface to turn into a java bean
     *
     * @return a new java bean proxy instance
     */
    <T> T createInterfaceJavaBeanProxy(Class<?> toJavaBeanify);

    /**
     * Creates a new proxy that extends the given class.
     *
     * @param <T>
     *            the type of the proxy
     * @param classToIntercept
     *            the class to extend
     * @param interfaces
     *            additional interfaces to implement
     *
     * @return a new proxy instance
     */
    <T> T createClassProxy(Class<T> classToIntercept, Class<?>... interfaces);

    /**
     * Creates a new proxy that extends the given class, constructed with the given constructor arguments.
     *
     * @param <T>
     *            the type of the proxy
     * @param classToIntercept
     *            the class to extend
     * @param constructorArgs
     *            arguments that are passed on to the matching super constructor
     *
     * @return a new proxy instance
     */
    <T> T createClassProxyWithArguments(Class<T> classToIntercept, Object... constructorArgs);

    /**
     * Creates a new proxy for the given abstract class with generated setter methods for every getter that is missing
     * one. The generated setters are only reachable through reflection.
     *
     * @param <T>
     *            the type of the proxy
     * @param toJavaBeanify
     *            the abstract class to turn into a java bean
     * @param interfaces
     *            additional interfaces to implement
     *
     * @return a new java bean proxy instance
     */
    <T> T createClassJavaBeanProxy(Class<?> toJavaBeanify, Class<?>... interfaces);

    /**
     * Creates a new proxy that delegates to the given object, unless the object already is a proxy that implements all
     * the requested interfaces, in which case the object itself is returned.
     *
     * @param <T>
     *            the type of the object
     * @param objectToIntercept
     *            the object to proxy
     * @param interfaces
     *            additional interfaces to implement
     *
     * @return a proxy instance, or the unchanged object
     */
    <T> T createObjectProxyIfNeeded(T objectToIntercept, Class<?>... interfaces);

    /**
     * @return the name of this engine, matching {@link ProxyConfiguration.Engine#getName()}
     */
    String getEngineName();
}
