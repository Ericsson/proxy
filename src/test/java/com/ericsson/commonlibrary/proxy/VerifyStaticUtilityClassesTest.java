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
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

public class VerifyStaticUtilityClassesTest {

    @Test
    public void utilPrivateConstructor() throws Exception {
        assertUtilityClassShouldOnlyHaveOnePrivateConstructor(Util.class);
    }

    @Test
    public void utilStaticMethodsOnly() throws Exception {
        assertUtilityClassShouldOnlyHaveStaticMethods(Util.class);
    }

    @Test
    public void proxyPrivateConstructor() throws Exception {
        assertUtilityClassShouldOnlyHaveOnePrivateConstructor(Proxy.class);
    }

    @Test
    public void proxyStaticMethodsOnly() throws Exception {
        assertUtilityClassShouldOnlyHaveStaticMethods(Proxy.class);
    }

    /**
     * Verify that there only one private constructor. Also invoke this private constructor to get full code coverage
     * but not because its really needed but because of the warm feeling 100% coverage gives ;)
     *
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void assertUtilityClassShouldOnlyHaveOnePrivateConstructor(final Class<?> clazz)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        assertEquals(constructors.length, 1, "Utility class should only have one constructor");

        final Constructor<?> constructor = constructors[0];
        assertFalse(constructor.isAccessible(), "Utility class constructor should be inaccessible");

        try {
            constructor.newInstance();
            fail(); // it should not be possible to create a instance.
        } catch (final Exception e) {
            // pass.
        }

        // Hack to make private constructor usable. == coverage.
        constructor.setAccessible(true);
        assertEquals(constructor.newInstance().getClass(), clazz,
                "You'd expect the construct to return the expected type");
        constructor.setAccessible(false);
    }

    /**
     * Verify that the class only have static methods
     *
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void assertUtilityClassShouldOnlyHaveStaticMethods(final Class<?> clazz)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(clazz)) {
                fail("there exists a non-static method:" + method);
            }
        }
    }
}
