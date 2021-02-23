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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.objenesis.ObjenesisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptableProxyFactory {

    private static final String ADDITIONAL_METHODS_SUFFIX = "ExtendedByProxy";

    private static final Logger LOG = LoggerFactory
            .getLogger(InterceptableProxyFactory.class);

    private enum ProxyType {
        OBJECT,
        INTERFACE,
        CLASS
    }

    private static final MethodFilter METHOD_FILTER = new MethodFilter() {

        @Override
        public boolean isHandled(final Method method) {
            // if not overridable Object.class method
            if (method.getDeclaringClass().equals(Object.class)
                    && !Util.isToStringOrHashcodeOrEqualsMethod(method)) {
                return false;
            }
            return true;
        }
    };

    private final ProxyFactory factory = new ProxyFactory();
    private final ProxyType type;
    private Object[] constructorArgs = null;

    InterceptableProxyFactory(ProxyType type) {
        this.type = type;
        factory.setFilter(METHOD_FILTER);
    }

    <T> T build() {
        try {
            return createProxyObject(type);
        } catch (Exception e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }

    void setInterfaces(Class<?>... interfaces) {
        factory.setInterfaces(makeAValidInterfaceArray(interfaces));
    }

    void setSuperclass(Class<?> classToIntercept) {
        if (ProxyObject.class.isAssignableFrom(classToIntercept)) { // Because it's not possible to proxy a proxy class because of setHandler duplicate exception.
            factory.setSuperclass(classToIntercept.getSuperclass());
        } else {
            factory.setSuperclass(classToIntercept);
        }
    }

    private static Class<?> addAdditionalSetMethodsToClass(Class<?> javaBean) {

        CtClass cc = null;
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(javaBean.getCanonicalName() + ADDITIONAL_METHODS_SUFFIX);
        } catch (ClassNotFoundException e) { //NOSONAR
            LOG.trace(javaBean.getCanonicalName() + ADDITIONAL_METHODS_SUFFIX + " did not exist. Creates one");
            cc = createNewClass(javaBean);
            addAdditionalSetMethodsTo(cc);
        } catch (RuntimeException e) { //NOSONAR (workaround) Powermock's classloader throws RuntimeException.
            if (e.getCause() != null && e.getCause() instanceof NotFoundException) {
                LOG.trace(javaBean.getCanonicalName() + ADDITIONAL_METHODS_SUFFIX + " did not exist. Creates one");
                cc = createNewClass(javaBean);
                addAdditionalSetMethodsTo(cc);
            }
        }
        try {
        	double javaSpecVersion = Double.parseDouble(System.getProperty("java.specification.version"));
        	if (javaSpecVersion > 10) { // Use different API on Java11 and later versions.
        		return cc.toClass(javaBean);
        	} else {        		
        		return cc.toClass(Thread.currentThread().getContextClassLoader(), javaBean.getProtectionDomain());
        	}
        } catch (CannotCompileException e) {
            LOG.warn(
                    "Was not able to create new proxy class. Will use the provided one instead which won't have the additional methods.",
                    e);
        }
        return javaBean;
    }

    private static CtClass createNewClass(Class<?> javaBean) {

        final ClassPool pool = ClassPool.getDefault();

        CtClass cc = null;
        try {
            cc = pool.get(getJavaBeanClassName(javaBean));
            cc.setName(javaBean.getCanonicalName() + ADDITIONAL_METHODS_SUFFIX);
        } catch (final NotFoundException e) {
            ProxyException.throwThisShouldNeverHappen(e);
        }
        try {
            cc.setSuperclass(pool.get(getJavaBeanClassName(javaBean)));
        } catch (CannotCompileException e) {
            throw new ProxyException("Was not able to create new proxy class", e);
        } catch (NotFoundException e) {
            ProxyException.throwThisShouldNeverHappen(e);
        }
        return cc;
    }

    private static String getJavaBeanClassName(Class<?> javaBean) {
        String className = javaBean.getCanonicalName();
        if (javaBean.getEnclosingClass() != null) {
            className = javaBean.getEnclosingClass().getCanonicalName() + "$" + javaBean.getSimpleName();
        }
        return className;
    }

    private static void addAdditionalSetMethodsTo(CtClass classToAddMethodTo) {
        for (CtMethod method : classToAddMethodTo.getMethods()) {
            String methodName = method.getName();
            if ((methodName.startsWith("get") || methodName.startsWith("is"))
                    && !"getClass".contentEquals(methodName)) {
                String newMethodName = methodName.replaceFirst("get|is", "set");
                CtMethod newMethod = null;
                try {
                    CtClass[] param = new CtClass[] { method.getReturnType() };
                    newMethod = CtNewMethod.abstractMethod(
                            ClassPool.getDefault().get(Void.class.getCanonicalName()), newMethodName, param, null,
                            classToAddMethodTo);
                } catch (NotFoundException e) {
                    ProxyException.throwThisShouldNeverHappen(e);
                }
                try {
                    classToAddMethodTo.addMethod(newMethod);
                } catch (CannotCompileException e) {
                    LOG.debug("Was not able to add a new method to proxy class, method: " + newMethodName);
                    LOG.trace("Reason why it was not able to add method:", e);
                }
            }
        }
    }

    private static Class<?>[] filterOnlyAccessableInterfaces(final Class<?> classToIntercept, Class<?>... interfaces) {
        List<Class<?>> newClassList = new ArrayList<Class<?>>();
        for (Class<?> inter : interfaces) {
            int modifiers = inter.getModifiers();
            boolean accessable = Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);

            if (accessable || classToIntercept.getPackage().equals(inter.getPackage())) {
                newClassList.add(inter);
            }
        }
        return newClassList.toArray(new Class<?>[newClassList.size()]);
    }

    private static Class<?>[] makeAValidInterfaceArray(Class<?>... interfaces) {
        // makes a ordered set of the interfaces. (removes duplicates)
        Set<Class<?>> set = new LinkedHashSet<Class<?>>(Arrays.asList(interfaces));
        set.remove(InterceptableProxy.class); // remove is exists
        set.add(InterceptableProxy.class); // needed by this library.
        set.remove(ProxyObject.class); // not allowed by javassit.
        set.remove(ProxyObject.class); // not allowed by javassit.
        return set.toArray(new Class<?>[set.size()]);
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxyObject(ProxyType type) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if (type == ProxyType.OBJECT) {
            try {
                return createProxyWithObjenesis(); //will not call a constructor.
            } catch (Throwable t) { //NOSONAR
                return createProxyObject(ProxyType.CLASS);
            }
        } else if (type == ProxyType.INTERFACE) {
            return (T) factory.create(null, null,
                    new JavassistInterceptorMethodHandler());
        } else { //ProxyType.CLASS

            //DONE 1 use constructor arguments.
            //DONE 2 use default constructor
            //DONE 3 use objenesis
            if (constructorArgs != null) {
                return (T) factory.create(findConstructorParameterTypes(factory.getSuperclass(), constructorArgs),
                        constructorArgs,
                        new JavassistInterceptorMethodHandler());
            }

            try {
                // Create object with methodHandler, used the empty constructor.
                return (T) factory.create(null, null,
                        new JavassistInterceptorMethodHandler());
            } catch (NoSuchMethodException e) {
                LOG
                        .debug("Was not able to create proxy with constructor or it does not exist. Will try to construct without constructor");
                return createProxyWithObjenesis();
            }
        }
    }

    private Class<?>[] findConstructorParameterTypes(Class<?> clazz, Object... args) {

        for (Constructor<?> constructor : clazz
                .getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            boolean matchingArgs = false;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (args[i] == null && !parameterTypes[i].isPrimitive()) {
                    matchingArgs = true; //null should match all Object types
                } else if (parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    matchingArgs = true;
                } else {
                    matchingArgs = false;
                    break;
                }
            }
            if (matchingArgs) {
                return parameterTypes;
            }
        }
        throw new ProxyException(
                "Did not find any constructor matching the provided arguments: " + Arrays.asList(args));
    }

    private <T> T createProxyWithObjenesis() {
        Object obj = ObjenesisHelper.newInstance(factory.createClass());
        ((ProxyObject) obj).setHandler(new JavassistInterceptorMethodHandler());
        return (T) obj;
    }

    static <T> T createANewInterfaceProxy(
            Class<?>... interfaces) {
        InterceptableProxyFactory builder = new InterceptableProxyFactory(ProxyType.INTERFACE);
        builder.setInterfaces(filterOnlyAccessableInterfaces(interfaces[0], interfaces));
        return builder.build();
    }

    static <T> T createANewInterfaceJavaBeanProxy(
            Class<?> toJavaBeanify) {
        return createANewInterfaceProxy(addAdditionalSetMethodsToClass(toJavaBeanify));
    }

    static <T> T createANewClassJavaBeanProxy(
            Class<?> toJavaBeanify, Class<?>... interfaces) {
        Class<?> modifiedClass = addAdditionalSetMethodsToClass(toJavaBeanify);
        return (T) createANewClassProxy(modifiedClass, interfaces);
    }

    static <T> T createANewObjectProxyIfNeeded(final T objectToIntercept,
            Class<?>... interfaces) {
        if (!Util.isNewProxyNeeded(objectToIntercept, makeAValidInterfaceArray(interfaces))) {
            return objectToIntercept;
        }

        // was not a already proxy object -> create new one.
        InterceptableProxyFactory builder = new InterceptableProxyFactory(ProxyType.OBJECT);

        builder.setSuperclass(objectToIntercept.getClass());
        builder.setInterfaces(filterOnlyAccessableInterfaces(objectToIntercept.getClass(), interfaces));
        T proxy = builder.build();
        Proxy.getProxyInterface(proxy).addInterceptor(
                new InterceptorDelegator(objectToIntercept));
        return proxy;
    }

    static <T> T createANewClassProxy(final Class<T> classToIntercept,
            Class<?>... interfaces) {
        InterceptableProxyFactory builder = new InterceptableProxyFactory(ProxyType.CLASS);
        builder.setSuperclass(classToIntercept);
        builder.setInterfaces(filterOnlyAccessableInterfaces(classToIntercept, interfaces));
        return builder.build();
    }

    static <T> T createANewClassProxyWithArguments(final Class<T> classToIntercept, Object... constructorArgs) {
        InterceptableProxyFactory builder = new InterceptableProxyFactory(ProxyType.CLASS);
        builder.constructorArgs = constructorArgs;
        builder.setSuperclass(classToIntercept);
        builder.setInterfaces(new Class<?>[0]);
        return builder.build();
    }

    private static class JavassistInterceptorMethodHandler implements
            MethodHandler {

        private static Method addInterceptorMethod;
        private static Method removeInterceptorMethod;
        private static Method getInterceptorListMethod;
        static {
            try {
                addInterceptorMethod = InterceptableProxy.class.getMethod(
                        "addInterceptor", Interceptor.class);
                removeInterceptorMethod = InterceptableProxy.class.getMethod(
                        "removeInterceptor", Interceptor.class);
                getInterceptorListMethod = InterceptableProxy.class
                        .getMethod("getInterceptorList");
            } catch (SecurityException | NoSuchMethodException e) {
                ProxyException.throwThisShouldNeverHappen(e);
            }
        }

        private final ArrayDeque<Interceptor> interceptorStack = new ArrayDeque<>();

        @Override
        public Object invoke(Object self, Method method, Method proceed,
                Object[] args) throws Throwable {

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

            return new Invocation(self, method, proceed, args, interceptorStack.clone()).invoke();
            //TODO wrap the checked exception if it is thrown even if its not declared in the interface.
        }
    }
}
