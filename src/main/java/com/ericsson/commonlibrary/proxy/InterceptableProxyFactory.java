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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entry point that the rest of the library uses to create proxies. All work is delegated to the
 * {@link ProxyEngineFactory} that {@link ProxyConfiguration} currently selects.
 * <p>
 * The helpers in this class are shared between all engines so that they behave identically no matter which engine is
 * used.
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptableProxyFactory {

    /**
     * Interfaces that must never be added to a generated proxy, because they are engine internals of an already
     * existing proxy that is being proxied again.
     */
    private static final String JAVASSIST_PROXY_OBJECT = "javassist.util.proxy.ProxyObject";
    private static final String JAVASSIST_PROXY = "javassist.util.proxy.Proxy";

    private InterceptableProxyFactory() {
        // hidden
    }

    static <T> T createANewInterfaceProxy(Class<?>... interfaces) {
        return ProxyEngineProvider.getCurrentFactory().createInterfaceProxy(interfaces);
    }

    static <T> T createANewInterfaceJavaBeanProxy(Class<?> toJavaBeanify) {
        return ProxyEngineProvider.getCurrentFactory().createInterfaceJavaBeanProxy(toJavaBeanify);
    }

    static <T> T createANewClassJavaBeanProxy(Class<?> toJavaBeanify, Class<?>... interfaces) {
        return ProxyEngineProvider.getCurrentFactory().createClassJavaBeanProxy(toJavaBeanify, interfaces);
    }

    static <T> T createANewObjectProxyIfNeeded(final T objectToIntercept, Class<?>... interfaces) {
        return ProxyEngineProvider.getCurrentFactory().createObjectProxyIfNeeded(objectToIntercept, interfaces);
    }

    static <T> T createANewClassProxy(final Class<T> classToIntercept, Class<?>... interfaces) {
        return ProxyEngineProvider.getCurrentFactory().createClassProxy(classToIntercept, interfaces);
    }

    static <T> T createANewClassProxyWithArguments(final Class<T> classToIntercept, Object... constructorArgs) {
        return ProxyEngineProvider.getCurrentFactory().createClassProxyWithArguments(classToIntercept, constructorArgs);
    }

    /**
     * Drops the interfaces that the generated proxy class would not be allowed to implement, which are the ones that
     * are neither public/protected nor declared in the same package as the type being proxied.
     *
     * @param classToIntercept
     *            the type being proxied, decides which package is "the same package"
     * @param interfaces
     *            the candidate interfaces
     *
     * @return the accessible subset of the given interfaces
     */
    static Class<?>[] filterOnlyAccessableInterfaces(final Class<?> classToIntercept, Class<?>... interfaces) {
        List<Class<?>> newClassList = new ArrayList<Class<?>>();
        for (Class<?> inter : interfaces) {
            int modifiers = inter.getModifiers();
            boolean accessable = Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);

            if (accessable || samePackage(classToIntercept, inter)) {
                newClassList.add(inter);
            }
        }
        return newClassList.toArray(new Class<?>[newClassList.size()]);
    }

    private static boolean samePackage(Class<?> one, Class<?> other) {
        if (one.getPackage() == null) {
            return other.getPackage() == null;
        }
        return one.getPackage().equals(other.getPackage());
    }

    /**
     * Removes duplicates, removes engine internal interfaces of proxies that are being proxied again, and makes sure
     * that {@link InterceptableProxy} is present and last.
     *
     * @param interfaces
     *            the candidate interfaces
     *
     * @return an ordered array of interfaces that a proxy can be generated for
     */
    static Class<?>[] makeAValidInterfaceArray(Class<?>... interfaces) {
        // makes a ordered set of the interfaces. (removes duplicates)
        Set<Class<?>> set = new LinkedHashSet<Class<?>>(Arrays.asList(interfaces));
        set.remove(InterceptableProxy.class); // remove if it exists
        set.removeIf(InterceptableProxyFactory::isEngineInternalInterface);
        set.add(InterceptableProxy.class); // needed by this library.
        return set.toArray(new Class<?>[set.size()]);
    }

    private static boolean isEngineInternalInterface(Class<?> candidate) {
        String name = candidate.getName();
        return JAVASSIST_PROXY_OBJECT.equals(name) || JAVASSIST_PROXY.equals(name);
    }

    /**
     * Finds the parameter types of the constructor that matches the given arguments.
     *
     * @param clazz
     *            the class that declares the constructor
     * @param args
     *            the arguments that the constructor should accept
     *
     * @return the parameter types of the first matching constructor
     */
    static Class<?>[] findConstructorParameterTypes(Class<?> clazz, Object... args) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }
            boolean matchingArgs = false;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (args[i] == null && !parameterTypes[i].isPrimitive()) {
                    matchingArgs = true; // null should match all Object types
                } else if (args[i] != null && parameterTypes[i].isAssignableFrom(args[i].getClass())) {
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

    /**
     * The {@link Object} methods that a proxy is allowed to intercept. All the other ones are final and can not be
     * overridden anyway.
     *
     * @param method
     *            the method to check
     *
     * @return true when the method should be intercepted
     */
    static boolean isInterceptableMethod(Method method) {
        return !method.getDeclaringClass().equals(Object.class) || Util.isToStringOrHashcodeOrEqualsMethod(method);
    }

    /**
     * Collects the setter methods that a java bean proxy is missing, so that an engine can add them.
     *
     * @param beanType
     *            the interface or abstract class that is turned into a java bean
     *
     * @return one entry per setter that has to be generated
     */
    static List<JavaBeanSetter> findMissingJavaBeanSetters(Class<?> beanType) {
        Set<String> existing = new LinkedHashSet<>();
        for (Method method : beanType.getMethods()) {
            existing.add(signatureKey(method.getName(), method.getParameterTypes()));
        }
        List<JavaBeanSetter> missing = new ArrayList<>();
        for (Method method : beanType.getMethods()) {
            if (method.getParameterCount() != 0 || method.getReturnType() == void.class
                    || "getClass".equals(method.getName()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String setterName = toSetterName(method.getName());
            if (setterName == null) {
                continue;
            }
            Class<?> valueType = method.getReturnType();
            if (existing.add(signatureKey(setterName, new Class<?>[] { valueType }))) {
                missing.add(new JavaBeanSetter(setterName, valueType));
            }
        }
        return missing;
    }

    private static String toSetterName(String getterName) {
        if (getterName.startsWith("get") && getterName.length() > 3) {
            return "set" + getterName.substring(3);
        }
        if (getterName.startsWith("is") && getterName.length() > 2) {
            return "set" + getterName.substring(2);
        }
        return null;
    }

    private static String signatureKey(String name, Class<?>[] parameterTypes) {
        StringBuilder key = new StringBuilder(name);
        for (Class<?> parameterType : parameterTypes) {
            key.append('|').append(parameterType.getName());
        }
        return key.toString();
    }

    /**
     * A setter method that has to be generated for a java bean proxy.
     */
    static final class JavaBeanSetter {

        private final String name;
        private final Class<?> valueType;

        JavaBeanSetter(String name, Class<?> valueType) {
            this.name = name;
            this.valueType = valueType;
        }

        String getName() {
            return name;
        }

        Class<?> getValueType() {
            return valueType;
        }
    }
}
