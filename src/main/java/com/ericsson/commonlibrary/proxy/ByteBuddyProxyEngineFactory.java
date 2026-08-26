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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.objenesis.ObjenesisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.InterceptableProxyFactory.JavaBeanSetter;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * {@link ProxyEngineFactory} implemented with <a href="https://bytebuddy.net/">Byte Buddy</a>.
 * <p>
 * Every intercepted method is delegated to {@link ByteBuddyProxyDispatcher#dispatch}, which receives the real
 * implementation as a {@code @SuperMethod} accessor. That accessor is what makes {@link Invocation#invoke()} able to
 * proceed into the original code, exactly like javassist's {@code proceed} method does.
 */
final class ByteBuddyProxyEngineFactory implements ProxyEngineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ByteBuddyProxyEngineFactory.class);

    private static final String PROXY_CLASS_INFIX = "$ProxyByByteBuddy$";
    private static final String FALLBACK_PACKAGE = "com.ericsson.commonlibrary.proxy.generated";

    private static final AtomicLong COUNTER = new AtomicLong();

    /**
     * All virtual methods that can be overridden, minus the {@link Object} methods that are not
     * {@code toString/hashCode/equals}.
     */
    private static final ElementMatcher.Junction<MethodDescription> INTERCEPTED_METHODS = ElementMatchers
            .not(ElementMatchers.isStatic()).and(ElementMatchers.not(ElementMatchers.isFinal()))
            .and(ElementMatchers.not(ElementMatchers.isPrivate()))
            .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)).or(ElementMatchers.isToString())
                    .or(ElementMatchers.isHashCode()).or(ElementMatchers.isEquals()));

    private static final Implementation DELEGATION = MethodDelegation.withDefaultConfiguration()
            .to(ByteBuddyProxyDispatcher.class);

    /**
     * Generated proxy classes are shared between proxy instances of the same shape. Soft references let the JVM drop
     * them again under metaspace pressure.
     */
    private static final TypeCache<CacheKey> TYPE_CACHE = new TypeCache.WithInlineExpunction<>(TypeCache.Sort.SOFT);

    @Override
    public String getEngineName() {
        return ProxyConfiguration.Engine.BYTEBUDDY.getName();
    }

    @Override
    public <T> T createInterfaceProxy(Class<?>... interfaces) {
        Class<?>[] accessible = InterceptableProxyFactory.filterOnlyAccessableInterfaces(interfaces[0], interfaces);
        Class<?> proxyClass = generate(null, accessible, null, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING);
        return attach(newInstanceWithDefaultConstructor(proxyClass));
    }

    @Override
    public <T> T createInterfaceJavaBeanProxy(Class<?> toJavaBeanify) {
        Class<?>[] accessible = InterceptableProxyFactory.filterOnlyAccessableInterfaces(toJavaBeanify, toJavaBeanify);
        Class<?> proxyClass = generate(null, accessible,
                InterceptableProxyFactory.findMissingJavaBeanSetters(toJavaBeanify),
                ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING);
        return attach(newInstanceWithDefaultConstructor(proxyClass));
    }

    @Override
    public <T> T createClassProxy(Class<T> classToIntercept, Class<?>... interfaces) {
        Class<?> superClass = resolveSuperClass(classToIntercept);
        Class<?>[] accessible = InterceptableProxyFactory.filterOnlyAccessableInterfaces(classToIntercept, interfaces);
        Class<?> proxyClass = generate(superClass, accessible, null,
                ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING);
        return attach(newInstanceWithDefaultConstructorOrObjenesis(proxyClass));
    }

    @Override
    public <T> T createClassProxyWithArguments(Class<T> classToIntercept, Object... constructorArgs) {
        Class<?> superClass = resolveSuperClass(classToIntercept);
        Class<?> proxyClass = generate(superClass, new Class<?>[0], null,
                ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING);
        Class<?>[] parameterTypes = InterceptableProxyFactory.findConstructorParameterTypes(superClass,
                constructorArgs);
        try {
            return attach(proxyClass.getDeclaredConstructor(parameterTypes).newInstance(constructorArgs));
        } catch (ReflectiveOperationException e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }

    @Override
    public <T> T createClassJavaBeanProxy(Class<?> toJavaBeanify, Class<?>... interfaces) {
        Class<?> superClass = resolveSuperClass(toJavaBeanify);
        Class<?>[] accessible = InterceptableProxyFactory.filterOnlyAccessableInterfaces(toJavaBeanify, interfaces);
        Class<?> proxyClass = generate(superClass, accessible,
                InterceptableProxyFactory.findMissingJavaBeanSetters(toJavaBeanify),
                ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING);
        return attach(newInstanceWithDefaultConstructorOrObjenesis(proxyClass));
    }

    @Override
    public <T> T createObjectProxyIfNeeded(T objectToIntercept, Class<?>... interfaces) {
        if (!Util.isNewProxyNeeded(objectToIntercept, InterceptableProxyFactory.makeAValidInterfaceArray(interfaces))) {
            return objectToIntercept;
        }
        Class<?> superClass = resolveSuperClass(objectToIntercept.getClass());
        Class<?>[] accessible = InterceptableProxyFactory.filterOnlyAccessableInterfaces(objectToIntercept.getClass(),
                interfaces);
        // No constructor of the proxied type may run, the object it delegates to already exists.
        Class<?> proxyClass = generate(superClass, accessible, null, ConstructorStrategy.Default.NO_CONSTRUCTORS);
        T proxy = attach(ObjenesisHelper.newInstance(proxyClass));
        Proxy.getProxyInterface(proxy).addInterceptor(new InterceptorDelegator(objectToIntercept));
        return proxy;
    }

    /**
     * Proxying a proxy would clash with the dispatcher field of the inner proxy, so the inner proxy is unwrapped first.
     * Javassist does the same thing for its own generated classes.
     */
    private static Class<?> resolveSuperClass(Class<?> candidate) {
        Class<?> superClass = candidate;
        while (superClass != null && superClass != Object.class && isGeneratedProxyClass(superClass)) {
            superClass = superClass.getSuperclass();
        }
        return superClass == null ? Object.class : superClass;
    }

    private static boolean isGeneratedProxyClass(Class<?> candidate) {
        if (!InterceptableProxy.class.isAssignableFrom(candidate)) {
            return false;
        }
        for (Field field : candidate.getDeclaredFields()) {
            if (ByteBuddyProxyDispatcher.DISPATCHER_FIELD.equals(field.getName())) {
                return true;
            }
        }
        // A javassist proxy class, which can not be extended either because of its duplicated handler field.
        return candidate.getName().contains("_$$_");
    }

    private Class<?> generate(Class<?> superClass, Class<?>[] interfaces, List<JavaBeanSetter> extraSetters,
            ConstructorStrategy constructorStrategy) {

        Class<?>[] validInterfaces = InterceptableProxyFactory.makeAValidInterfaceArray(interfaces);
        Class<?> effectiveSuperClass = superClass == null ? Object.class : superClass;
        Class<?> host = resolveHost(effectiveSuperClass, validInterfaces);
        Class<?> lookupHost = host != null ? host : ByteBuddyProxyDispatcher.class;

        // Proxy classes are shared, all instance state lives in the dispatcher field, so proxying the same type twice
        // must not generate a second class. Javassist caches its proxy classes the same way.
        CacheKey key = new CacheKey(effectiveSuperClass, validInterfaces, extraSetters, constructorStrategy);
        try {
            return TYPE_CACHE.findOrInsert(lookupHost.getClassLoader(), key, () -> generateNewClass(effectiveSuperClass,
                    validInterfaces, extraSetters, constructorStrategy, host), TYPE_CACHE);
        } catch (RuntimeException e) {
            // The type cache wraps whatever the generation threw, so the original ProxyException is dug out again.
            throw asProxyException(e);
        }
    }

    private static ProxyException asProxyException(RuntimeException e) {
        for (Throwable candidate = e; candidate != null; candidate = candidate.getCause()) {
            if (candidate instanceof ProxyException) {
                return (ProxyException) candidate;
            }
        }
        return new ProxyException("Not able to create proxy", e);
    }

    private static Class<?> generateNewClass(Class<?> superClass, Class<?>[] interfaces,
            List<JavaBeanSetter> extraSetters, ConstructorStrategy constructorStrategy, Class<?> host) {

        // The generated class is named after the type being proxied, because a proxy's default toString() is expected
        // to reveal what it is a proxy of.
        Class<?> nameSource = superClass != Object.class ? superClass : interfaces[0];

        try {
            DynamicType.Builder<?> builder = new ByteBuddy().subclass(superClass, constructorStrategy)
                    .name(nextProxyClassName(host, nameSource)).implement(interfaces)
                    // Private, because a public field would make Util consider the proxy unsafe to intercept further.
                    .defineField(ByteBuddyProxyDispatcher.DISPATCHER_FIELD, Object.class, Visibility.PRIVATE,
                            SyntheticState.SYNTHETIC);

            if (extraSetters != null) {
                for (JavaBeanSetter setter : extraSetters) {
                    builder = builder.defineMethod(setter.getName(), void.class, Visibility.PUBLIC)
                            .withParameter(setter.getValueType()).intercept(DELEGATION);
                }
            }

            DynamicType.Unloaded<?> unloaded = builder.method(INTERCEPTED_METHODS).intercept(DELEGATION).make();
            return load(unloaded, host);
        } catch (ProxyException e) {
            throw e;
        } catch (RuntimeException | LinkageError e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }

    /**
     * Identifies a generated proxy class. Only type names are kept, never {@link Class} references, so that a cached
     * class never keeps a class loader alive.
     */
    private static final class CacheKey {

        private final String superClassName;
        private final List<String> interfaceNames;
        private final String discriminator;

        CacheKey(Class<?> superClass, Class<?>[] interfaces, List<JavaBeanSetter> extraSetters,
                ConstructorStrategy constructorStrategy) {
            this.superClassName = superClass.getName();
            this.interfaceNames = new ArrayList<>(interfaces.length);
            for (Class<?> anInterface : interfaces) {
                interfaceNames.add(anInterface.getName());
            }
            StringBuilder builder = new StringBuilder(constructorStrategy.toString());
            if (extraSetters != null) {
                for (JavaBeanSetter setter : extraSetters) {
                    builder.append('|').append(setter.getName()).append('(').append(setter.getValueType().getName())
                            .append(')');
                }
            }
            this.discriminator = builder.toString();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheKey)) {
                return false;
            }
            CacheKey that = (CacheKey) other;
            return superClassName.equals(that.superClassName) && interfaceNames.equals(that.interfaceNames)
                    && discriminator.equals(that.discriminator);
        }

        @Override
        public int hashCode() {
            return Objects.hash(superClassName, interfaceNames, discriminator);
        }
    }

    /**
     * Decides which type's package the generated proxy class is placed in. It has to be the package of the proxied type
     * whenever anything about that type is package private, otherwise the generated class would not be allowed to
     * implement its interfaces or override its methods.
     */
    private static Class<?> resolveHost(Class<?> superClass, Class<?>[] interfaces) {
        if (isDefinableIn(superClass)) {
            return superClass;
        }
        for (Class<?> candidate : interfaces) {
            if (!Modifier.isPublic(candidate.getModifiers()) && isDefinableIn(candidate)) {
                return candidate;
            }
        }
        for (Class<?> candidate : interfaces) {
            if (isDefinableIn(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * A class can host a generated proxy when it was loaded by a normal class loader and its package is not owned by
     * the JDK.
     */
    private static boolean isDefinableIn(Class<?> candidate) {
        if (candidate == Object.class || candidate.getClassLoader() == null || candidate.isArray()) {
            return false;
        }
        String packageName = candidate.getPackageName();
        return !packageName.isEmpty() && !packageName.startsWith("java.") && !packageName.startsWith("javax.")
                && !packageName.startsWith("jdk.") && !packageName.startsWith("sun.")
                && !packageName.startsWith("com.sun.");
    }

    private static String nextProxyClassName(Class<?> host, Class<?> superClass) {
        String packageName = host != null ? host.getPackageName() : FALLBACK_PACKAGE;
        return packageName + "." + simpleNameOf(superClass) + PROXY_CLASS_INFIX + COUNTER.incrementAndGet();
    }

    /**
     * A java identifier derived from the super class, only used to make generated class names readable. Anonymous and
     * local classes have names that can not be reused as is.
     */
    private static String simpleNameOf(Class<?> superClass) {
        if (superClass == Object.class) {
            return "Proxy";
        }
        String name = superClass.getName();
        int lastDot = name.lastIndexOf('.');
        StringBuilder simpleName = new StringBuilder();
        for (int i = lastDot + 1; i < name.length(); i++) {
            char character = name.charAt(i);
            simpleName.append(Character.isJavaIdentifierPart(character) && character != '$' ? character : '_');
        }
        return simpleName.length() == 0 ? "Proxy" : simpleName.toString();
    }

    private static Class<?> load(DynamicType.Unloaded<?> unloaded, Class<?> host) {
        Class<?> lookupHost = host != null ? host : ByteBuddyProxyDispatcher.class;
        ClassLoader classLoader = lookupHost.getClassLoader();
        return unloaded.load(classLoader, strategyFor(lookupHost, unloaded)).getLoaded();
    }

    /**
     * Defining the proxy in the same runtime package as the type it proxies is what makes package private types,
     * interfaces and methods work. A method handle lookup is the only way to do that without any JVM flags, so it is
     * tried first and class injection is only used as a fallback.
     */
    private static ClassLoadingStrategy<ClassLoader> strategyFor(Class<?> host, DynamicType.Unloaded<?> unloaded) {
        try {
            return ClassLoadingStrategy.UsingLookup.of(MethodHandles.privateLookupIn(host, MethodHandles.lookup()));
        } catch (IllegalAccessException | RuntimeException e) {
            LOG.debug(
                    "Was not able to define {} next to {} with a method handle lookup. Falls back to class injection.",
                    unloaded.getTypeDescription().getName(), host.getName(), e);
        }
        if (ClassInjector.UsingReflection.isAvailable()) {
            return ClassLoadingStrategy.Default.INJECTION;
        }
        return ClassLoadingStrategy.Default.WRAPPER;
    }

    private static <T> T newInstanceWithDefaultConstructor(Class<?> proxyClass) {
        try {
            return (T) proxyClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }

    private static <T> T newInstanceWithDefaultConstructorOrObjenesis(Class<?> proxyClass) {
        try {
            return (T) proxyClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            LOG.debug("{} has no usable empty constructor. Will construct it without calling a constructor.",
                    proxyClass.getSuperclass().getName());
            return (T) ObjenesisHelper.newInstance(proxyClass);
        } catch (ReflectiveOperationException e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }

    @SuppressWarnings("squid:S3011")
    private static <T> T attach(Object proxy) {
        try {
            Field field = proxy.getClass().getDeclaredField(ByteBuddyProxyDispatcher.DISPATCHER_FIELD);
            field.setAccessible(true);
            field.set(proxy, new ByteBuddyInterceptorDispatcher());
            return (T) proxy;
        } catch (ReflectiveOperationException e) {
            throw new ProxyException("Not able to create proxy", e);
        }
    }
}
