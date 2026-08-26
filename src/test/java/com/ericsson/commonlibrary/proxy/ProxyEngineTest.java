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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.ProxyConfiguration.Engine;
import com.ericsson.commonlibrary.proxy.helpobjects.AbstractClassWithImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.MyInterface;
import com.ericsson.commonlibrary.proxy.helpobjects.MySubImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.PersonBean;
import com.ericsson.commonlibrary.proxy.helpobjects.ReturnString1;

/**
 * Verifies that both engines exist in parallel, that the user can switch between them, and that they behave the same.
 */
public class ProxyEngineTest {

    private static final String JAVASSIST_CLASS_MARKER = "_$$_jvst";
    private static final String BYTEBUDDY_CLASS_MARKER = "$ProxyByByteBuddy$";

    @AfterMethod
    public void restoreConfiguredEngine() {
        ProxyConfiguration.reset();
    }

    @DataProvider(name = "engines")
    public Object[][] engines() {
        return new Object[][] { { Engine.JAVASSIST }, { Engine.BYTEBUDDY } };
    }

    @Test
    public void defaultEngineIsTheOneFromTheSystemProperty() {
        assertEquals(ProxyConfiguration.getEngine(),
                Engine.fromName(System.getProperty(ProxyConfiguration.ENGINE_PROPERTY)));
    }

    @Test
    public void unknownEngineNameFallsBackToJavassist() {
        assertEquals(Engine.fromName("does-not-exist"), Engine.JAVASSIST);
        assertEquals(Engine.fromName(null), Engine.JAVASSIST);
        assertEquals(Engine.fromName("  "), Engine.JAVASSIST);
        assertEquals(Engine.fromName(" ByteBuddy "), Engine.BYTEBUDDY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullEngineIsRejected() {
        ProxyConfiguration.setEngine((Engine) null);
    }

    @Test
    public void globalEngineCanBeSwitchedAtRuntime() {
        ProxyConfiguration.setEngine(Engine.BYTEBUDDY);
        assertEquals(ProxyConfiguration.getEngine(), Engine.BYTEBUDDY);
        assertTrue(proxyClassNameOfAClassProxy().contains(BYTEBUDDY_CLASS_MARKER));

        ProxyConfiguration.setEngine("javassist");
        assertEquals(ProxyConfiguration.getEngine(), Engine.JAVASSIST);
        assertTrue(proxyClassNameOfAClassProxy().contains(JAVASSIST_CLASS_MARKER));
    }

    @Test
    public void threadEngineOverridesTheGlobalEngine() throws Exception {
        ProxyConfiguration.setEngine(Engine.JAVASSIST);
        ProxyConfiguration.setThreadEngine(Engine.BYTEBUDDY);
        assertEquals(ProxyConfiguration.getEngine(), Engine.BYTEBUDDY);

        ProxyConfiguration.clearThreadEngine();
        assertEquals(ProxyConfiguration.getEngine(), Engine.JAVASSIST);

        String name = ProxyConfiguration.callWithEngine(Engine.BYTEBUDDY, this::proxyClassNameOfAClassProxy);
        assertTrue(name.contains(BYTEBUDDY_CLASS_MARKER));
        assertEquals(ProxyConfiguration.getEngine(), Engine.JAVASSIST);
    }

    @Test(dataProvider = "engines")
    public void classProxyIsCreatedByTheSelectedEngine(Engine engine) throws Exception {
        String name = ProxyConfiguration.callWithEngine(engine, this::proxyClassNameOfAClassProxy);
        assertTrue(name.contains(markerOf(engine)), name + " was not created by " + engine);
    }

    @Test(dataProvider = "engines")
    public void interfaceProxyIsCreatedByTheSelectedEngine(Engine engine) throws Exception {
        MyInterface proxy = withEngine(engine, () -> Proxy.delegate(MyInterface.class, new MySubImpl()));
        assertTrue(proxy.getClass().getName().contains(markerOf(engine)));
        assertEquals(proxy.doSomething(), "MySubImpl");
    }

    @Test(dataProvider = "engines")
    public void interceptionOfAClassProceedsToTheRealImplementation(Engine engine) throws Exception {
        ReturnString1 proxy = withEngine(engine, () -> Proxy.with(ReturnString1.class, "real")
                .interceptAll(i -> "before " + i.invoke() + " after").get());
        assertEquals(proxy.returnString1(), "before real after");
    }

    @Test(dataProvider = "engines")
    public void interceptionOfAnObjectProceedsToTheRealImplementation(Engine engine) throws Exception {
        List<String> list = withEngine(engine, () -> Proxy.intercept(new ArrayList<String>(),
                i -> "size".equals(i.getMethodName()) ? 42 : i.invoke()));
        list.add("one");
        assertEquals(list.size(), 42);
        assertEquals(list.get(0), "one");
    }

    @Test(dataProvider = "engines")
    public void interceptionOfAnAbstractClassProceedsToTheImplementedMethodsOnly(Engine engine) throws Exception {
        AbstractClassWithImpl proxy = withEngine(engine,
                () -> Proxy.with(AbstractClassWithImpl.class).interceptAll(Invocation::invoke).get());
        assertEquals(proxy.getImpl(), "impl");
        try {
            proxy.getNotImpl();
            org.testng.Assert.fail("An abstract method has nothing to proceed to");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("getNotImpl"));
        }
    }

    @Test(dataProvider = "engines")
    public void javaBeanWorksOnBothEngines(Engine engine) throws Exception {
        PersonBean bean = withEngine(engine, () -> Proxy.javaBean(PersonBean.class));
        bean.setName("elis");
        bean.setAge(20);
        assertEquals(bean.getName(), "elis");
        assertEquals(bean.getAge(), 20);
        assertTrue(bean.toString().contains("PersonBean"));
    }

    @Test(dataProvider = "engines")
    public void generatedJavaBeanSettersAreReachableWithReflectionOnBothEngines(Engine engine) throws Exception {
        com.ericsson.commonlibrary.proxy.helpobjects.JavaBean bean = withEngine(engine,
                () -> Proxy.javaBean(com.ericsson.commonlibrary.proxy.helpobjects.JavaBean.class));
        bean.getClass().getMethod("setName", String.class).invoke(bean, "generated");
        assertEquals(bean.getName(), "generated");
    }

    @Test(dataProvider = "engines")
    public void interceptorsCanBeAddedAndRemovedOnBothEngines(Engine engine) throws Exception {
        ReturnString1 proxy = withEngine(engine, () -> Proxy.with(ReturnString1.class, "real").get());
        Interceptor interceptor = i -> "intercepted";

        Proxy.getProxyInterface(proxy).addInterceptor(interceptor);
        assertEquals(proxy.returnString1(), "intercepted");
        assertTrue(Proxy.getProxyInterface(proxy).getInterceptorList().contains(interceptor));

        Proxy.getProxyInterface(proxy).removeInterceptor(interceptor);
        assertEquals(proxy.returnString1(), "real");
    }

    @Test
    public void aByteBuddyProxyCanBeWrappedInAJavassistProxy() throws Exception {
        ReturnString1 inner = withEngine(Engine.BYTEBUDDY,
                () -> Proxy.with(ReturnString1.class, "real").interceptAll(appending("+bytebuddy")).get());
        ReturnString1 outer = withEngine(Engine.JAVASSIST, () -> Proxy.delegate(ReturnString1.class, inner));

        assertNotSame(outer, inner);
        assertTrue(outer.getClass().getName().contains(JAVASSIST_CLASS_MARKER));
        assertEquals(outer.returnString1(), "real+bytebuddy");
    }

    @Test
    public void aJavassistProxyCanBeWrappedInAByteBuddyProxy() throws Exception {
        ReturnString1 inner = withEngine(Engine.JAVASSIST,
                () -> Proxy.with(ReturnString1.class, "real").interceptAll(appending("+javassist")).get());
        ReturnString1 outer = withEngine(Engine.BYTEBUDDY, () -> Proxy.delegate(ReturnString1.class, inner));

        assertNotSame(outer, inner);
        assertTrue(outer.getClass().getName().contains(BYTEBUDDY_CLASS_MARKER));
        assertEquals(outer.returnString1(), "real+javassist");
    }

    @Test
    public void interceptorsFromBothEnginesStackOnTheSameProxy() throws Exception {
        ReturnString1 proxy = withEngine(Engine.BYTEBUDDY,
                () -> Proxy.with(ReturnString1.class, "real").interceptAll(appending("+first")).get());
        // Intercepting an already existing proxy reuses it, no matter which engine is configured at the time.
        ReturnString1 same = withEngine(Engine.JAVASSIST, () -> Proxy.intercept(proxy, appending("+second")));

        assertSame(same, proxy);
        assertEquals(proxy.returnString1(), "real+first+second");
    }

    /**
     * Only alters returnString1(), so that hashCode() and toString() of the proxy keep working.
     */
    private static Interceptor appending(String suffix) {
        return i -> "returnString1".equals(i.getMethodName()) ? i.invoke() + suffix : i.invoke();
    }

    @Test(dataProvider = "engines", expectedExceptions = ProxyException.class)
    public void finalClassesAreRejectedByBothEnginesWithTheSameException(Engine engine) throws Exception {
        withEngine(engine, () -> Proxy.intercept("a final class", i -> i.invoke()));
    }

    /**
     * Both features refuse to touch classes that expose public instance variables, so a generated proxy class must not
     * expose any itself.
     */
    @Test(dataProvider = "engines")
    public void mdcAndRecursiveInterceptionAreNotRefusedOnAProxyFromEitherEngine(Engine engine) throws Exception {
        ReturnString1 proxy = withEngine(engine, () -> Proxy.with(ReturnString1.class, "real").get());
        Deque<Interceptor> interceptors = Proxy.getProxyInterface(proxy).getInterceptorList();

        int size = interceptors.size();
        withEngine(engine, () -> Proxy.mdcLogging(proxy, "key", "value"));
        assertEquals(interceptors.size(), size + 1, "the mdc interceptor was silently refused");

        withEngine(engine, () -> Proxy.recursiveIntercept(proxy, Invocation::invoke));
        assertEquals(interceptors.size(), size + 2, "the recursive interceptor was silently refused");

        assertEquals(proxy.returnString1(), "real");
    }

    /**
     * A new generated class per proxy instance would make metaspace grow without bound in a long running application.
     */
    @Test(dataProvider = "engines")
    public void generatedProxyClassesAreReusedForTheSameShape(Engine engine) throws Exception {
        Class<?> first = withEngine(engine, () -> Proxy.with(ReturnString1.class, "one").get().getClass());
        Class<?> second = withEngine(engine, () -> Proxy.with(ReturnString1.class, "two").get().getClass());
        assertSame(second, first, "a new proxy class was generated for an identical proxy shape");
    }

    @Test(dataProvider = "engines")
    public void generatedProxyClassesExposeNoPublicInstanceFields(Engine engine) throws Exception {
        Class<?> proxyClass = withEngine(engine, () -> Proxy.with(ReturnString1.class, "real").get().getClass());
        for (Field field : proxyClass.getDeclaredFields()) {
            assertFalse(
                    Util.isPublicAndNotStaticFinal(field.getModifiers())
                            && !"_filter_signature".equals(field.getName()),
                    proxyClass.getName() + " exposes the public field " + field.getName());
        }
    }

    private String proxyClassNameOfAClassProxy() {
        return Proxy.with(ReturnString1.class, "real").get().getClass().getName();
    }

    private static String markerOf(Engine engine) {
        return engine == Engine.JAVASSIST ? JAVASSIST_CLASS_MARKER : BYTEBUDDY_CLASS_MARKER;
    }

    private static <T> T withEngine(Engine engine, Callable<T> callable) throws Exception {
        return ProxyConfiguration.callWithEngine(engine, callable);
    }
}
