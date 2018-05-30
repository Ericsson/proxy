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
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The proxy library's only entry point. All features that Proxy library has can
 * be reached from here.
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
public final class Proxy {

    private static final InterceptorMethodTimer INTERCEPTOR_METHOD_TIMER = new InterceptorMethodTimer();
    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

    private Proxy() {
        // hidden
    }

    public static <T> ProxyFluent<T> with(Class<T> c) {
        return new ProxyFluent<T>(c);
    }

    public static <T> ProxyFluent<T> with(Class<T> c, Object... constructorArguments) {
        return new ProxyFluent<T>(c, constructorArguments);
    }

    public static <T> ProxyFluent<T> with(T o) {
        return new ProxyFluent<T>(o);
    }

    /**
     * Create a object with a specific interface/class that delegates all method calls
     * to the delegator objects passed in as parameters. Notice that you can get polymorphic
     * behavior with this delegation method.
     * 
     * @param proxyInterface interface/class of the returned class.
     * @param delegates Objects that implements the methods that should be used as delegate methods.
     * @return a proxy object that delegates method calls to it's delegates.
     */
    public static <T> T delegate(Class<T> proxyInterface, Object... delegates) {
        if (proxyInterface.isInterface()) {
            return delegateInterface(proxyInterface, delegates);
        }
        return delegateClass(proxyInterface, delegates);
    }

    private static <T> T delegateClass(Class<T> proxyInterface, Object... delegates) {
        T proxy = InterceptableProxyFactory.createANewClassProxy(proxyInterface,
                Util.getInterfacesImplementedByObjects(delegates));

        addInterceptor(proxy, new InterceptorDelegator(delegates));
        return proxy;
    }

    private static <T> T delegateInterface(Class<T> proxyInterface, Object... delegates) {
        Class<?>[] interfaceArray = Util.concatArrays(
                new Class[] { proxyInterface },
                Util.getInterfacesImplementedByObjects(delegates));

        T proxy = InterceptableProxyFactory.createANewInterfaceProxy(
                interfaceArray);
        addInterceptor(proxy,
                new InterceptorDelegator(delegates));
        return proxy;
    }

    /**
     * Let a existing object delegate all or some of its method calls to some
     * delegate objects passed in as parameters.
     * Warning your proxy will not behave polymorphically.
     * 
     * @param objectThatShouldDelegate object that should delegate to other
     *        objects.
     * @param delegates Objects that that implements some methods.
     * @return a proxy object that delegates all/some method calls to it's
     *         delegates.
     */
    public static <T> T delegate(T objectThatShouldDelegate,
            Object... delegates) {
        Class<?>[] interfaceArray = Util.concatArrays(
                Util.getInterfacesImplementedByObjects(objectThatShouldDelegate),
                Util.getInterfacesImplementedByObjects(delegates));
        T proxy = InterceptableProxyFactory.createANewObjectProxyIfNeeded(
                objectThatShouldDelegate, interfaceArray);
        addInterceptor(proxy, new InterceptorDelegator(
                delegates));
        return proxy;
    }

    /**
     * Creates a java bean object from a interface or a abstract class. (java bean=class with
     * getX(),setX(),isX() methods)
     * If the interface/abstract class does not contain corresponding setX method for a getX method
     * the setX method will be generated.
     * These generated set methods will only be available with reflection.
     * 
     * @param classToProxy the interface/abstract you want to create a java bean from.
     * @return a concrete usable java bean object.
     */
    public static <T> T javaBean(Class<T> classToProxy) {
        return javaBean(classToProxy, false);
    }

    /**
     * Creates a java bean object from a interface or a abstract class. (java bean=class with
     * getX(),setX(),isX() methods)
     * If the interface/abstract class does not contain corresponding setX method for a getX method
     * the setX method will be generated.
     * These generated set methods will only be available with reflection.
     * 
     * @param classToProxy the interface/abstract you want to create a java bean from.
     * @param primitiveDefaultIsException if primitives should default to exceptions instead of (0,
     *        false 0L etc)
     * @return a concrete usable java bean object.
     */
    public static <T> T javaBean(Class<T> classToProxy, boolean primitiveDefaultIsException) {
        if (!classToProxy.isInterface()) {
            if (Modifier.isAbstract(classToProxy.getModifiers())) {
                T proxy = InterceptableProxyFactory.createANewClassJavaBeanProxy(classToProxy);
                addInterceptor(proxy, new InterceptorJavaBean(proxy.getClass(), primitiveDefaultIsException));
                return proxy;
            }
            // was a normal class
            throw new IllegalArgumentException(
                    "The passed class was not a interface or a abstract class. You can only create javabeans with interfaces or abstract classes");

        }
        T proxy = InterceptableProxyFactory.createANewInterfaceJavaBeanProxy(classToProxy);
        addInterceptor(proxy, new InterceptorJavaBean(proxy.getClass(), primitiveDefaultIsException));
        return proxy;
    }

    /**
     * Add a performance timer to a object. The performance timer uses slf4j to
     * print stats about a method invocations.
     * 
     * @param objectToBenchmark
     * @return
     */
    public static <T> T addTimerToMethods(T objectToBenchmark) {
        return intercept(objectToBenchmark, INTERCEPTOR_METHOD_TIMER);
    }

    
    /**
     * Set a MDC key to a specific value while inside the object.
     * 
     * @param objectToLogg
     * @return
     */
    public static <T> T mdcLogging(T objectToLog, String key, String value) {
        if (Util.isClassSafeFromPublicVariableProblems(objectToLog.getClass())) {
            return intercept(objectToLog, new InterceptorMdc(key, value));
        } else {
            logger.debug("Was not able to add mdc logging to {} ", objectToLog.getClass());
            return objectToLog;
        }
    }

    /**
     * Let's you add an interceptor recursively to a existing object. Meaning that the interceptor
     * will add itself to every returned object.
     * 
     * @param objectToIntercept the object you what do add a interceptor recursively to.
     * @param interceptor the interceptor you want to add.
     * @return a proxy object with the interceptor added.
     */
    public static <T> T recursiveIntercept(T objectToIntercept, Interceptor interceptor) {
        if (Util.isClassSafeFromPublicVariableProblems(objectToIntercept.getClass())) {
            return intercept(objectToIntercept, new InterceptorRecursive(interceptor));
        } else {
            logger.warn("Was not able to add recusrive logging to {} ", objectToIntercept.getClass());
            return objectToIntercept;
        }
    }

    /**
     * Let's you add a interceptor to a existing object. It's possible to specify
     * which methods the inteceptor should intercept and if no method is
     * specified all methods will be intercepted.
     * 
     * @param objectToIntercept the object you what do add a interceptor to.
     * @param interceptor the interceptor you want to add.
     * @param methodsToIntercept varargs of the methods you want the interceptor
     *        to intercept. Specifying none means that it will intercept all.
     * @return a proxy object with the interceptor added.
     */
    public static <T> T intercept(T objectToIntercept, Interceptor interceptor,
            Method... methodsToIntercept) {
        T proxy = InterceptableProxyFactory
                .createANewObjectProxyIfNeeded(objectToIntercept);

        addMethodInterceptor(proxy, interceptor, methodsToIntercept);
        return proxy;
    }

    private static <T> void addMethodInterceptor(T proxy, Interceptor interceptor, Method... methodsToIntercept) {
        Interceptor interceptorToAdd = interceptor;

        // if it should be a method interceptor
        if (methodsToIntercept != null && methodsToIntercept.length != 0) {
            interceptorToAdd = new InterceptorMethod(interceptor,
                    methodsToIntercept);
        }
        addInterceptor(proxy, interceptorToAdd);
    }

    /**
     * This method allows you do use java.lang.reflect.InvocationHandler to
     * intercept method calls. InvocationHandler is part of the java API and can
     * not be used to intercept concrete objects (with this library it can).
     * Lets you add a InvocationHandler to a existing object. It possible to
     * specify which methods the invocationHandler should intercept and if no
     * method is specified all methods will be intercepted.
     * 
     * @param objectToIntercept the object you what do add a interceptor to.
     * @param InvocationHandler the interceptor you want to add.
     * @param methodsToIntercept varargs of the methods you want the interceptor
     *        to intercept. Specifying none means that it will intercept all.
     * @return a proxy object with the interceptor added.
     */
    public static <T> T intercept(T objectToIntercept,
            InvocationHandler invocationHandler, Method... methodsToIntercept) {
        return intercept(objectToIntercept, new InterceptorInvocationHandler(
                invocationHandler), methodsToIntercept);
    }

    /**
     * Let's you add a interceptor to a proxy object created from the specified class/interface.
     * It's
     * possible to specify which methods the inteceptor should intercept. if no method is
     * specified all methods will be intercepted.
     * 
     * @param classToIntercept the class/interface you what do add a interceptor to.
     * @param interceptor the interceptor you want to add.
     * @param methodsToIntercept varargs of the methods you want the interceptor
     *        to intercept. Specifying none means that it will intercept all.
     * @return a proxy object with the interceptor added.
     */
    public static <T> T intercept(Class<T> classToIntercept, Interceptor interceptor,
            Method... methodsToIntercept) {
        T proxy;
        if (classToIntercept.isInterface()) {
            proxy = InterceptableProxyFactory
                    .createANewInterfaceProxy(classToIntercept);
        } else { // was a class
            proxy = InterceptableProxyFactory
                    .createANewClassProxy(classToIntercept);
        }
        addMethodInterceptor(proxy, interceptor, methodsToIntercept);
        return proxy;
    }

    /**
     * This method allows you do use java.lang.reflect.InvocationHandler to
     * intercept method calls. InvocationHandler is part of the java API and can
     * not be used to intercept concrete objects or for non interfaces (with this library it can).
     * Lets you add a InvocationHandler to a classes/interfaces. It possible to
     * specify which methods the invocationHandler should intercept and if no
     * method is specified all methods will be intercepted.
     * 
     * @param classToIntercept the class/interface you what do add a interceptor to.
     * @param InvocationHandler the interceptor you want to add.
     * @param methodsToIntercept varargs of the methods you want the interceptor
     *        to intercept. Specifying none means that it will intercept all.
     * @return a proxy object with the interceptor added.
     */
    public static <T> T intercept(Class<T> classToIntercept,
            InvocationHandler invocationHandler, Method... methodsToIntercept) {
        return intercept(classToIntercept, new InterceptorInvocationHandler(
                invocationHandler), methodsToIntercept);
    }

    /**
     * Returns the same proxy object as you passed in but with the {@link InterceptableProxy}
     * interface
     * 
     * @param proxy
     * @return the proxy but with the {@link InterceptableProxy} interface.
     */
    public static InterceptableProxy getProxyInterface(Object proxy) {
        if (proxy instanceof InterceptableProxy) {
            return (InterceptableProxy) proxy;
        }
        throw new IllegalArgumentException(
                "Not possible to get the proxy interface of a non proxy object");
    }

    /**
     * Allows you to change the interface of a object to one that it does not
     * implement. Duck Typing,
     * 
     * @param newInterface
     * @param objectToChangeInterfaceOn
     * @return
     */
    public static <T> T changeInterface(Class<T> newInterface,
            final Object objectToChangeInterfaceOn) {
        // TODO include the interfaces of objectToChangeInterfaceOn? so casting is possible?
        T proxy = InterceptableProxyFactory.createANewInterfaceProxy(newInterface);
        addInterceptor(proxy, new InterceptorDelegator(
                objectToChangeInterfaceOn));
        return proxy;
    }

    private static <T> void addInterceptor(T proxy, Interceptor interceptor) {
        Proxy.getProxyInterface(proxy).addInterceptor(interceptor);
    }
}
