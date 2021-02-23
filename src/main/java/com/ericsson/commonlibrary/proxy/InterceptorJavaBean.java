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

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Interceptor} that lets you create javabean without providing a implementation.
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptorJavaBean implements Interceptor {

    private static final int IS_LENGTH = 2;
    private static final int XET_LENGTH = 3;
    private final Map<String, Object> map = new HashMap<String, Object>();
    private final Class<?> beanInterface;
    private boolean primitiveDefaultIsException;

    InterceptorJavaBean(Class<?> beanInterface, boolean primitiveDefaultIsException) {
        this.beanInterface = beanInterface;
        this.primitiveDefaultIsException = primitiveDefaultIsException;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!Util.isMethodExistingInClass(invocation.getMethod(), beanInterface)) {
            return invocation.invoke();
        }

        String methodName = invocation.getMethodName();

        if (methodName.startsWith("get")) {
            String name = methodName.substring(XET_LENGTH);
            return getValue(invocation, name);
        } else if (methodName.startsWith("is")) {
            String name = methodName.substring(IS_LENGTH);
            return getValue(invocation, name);
        } else if (methodName.startsWith("set")) {
            String name = methodName.substring(XET_LENGTH);
            map.put(name, invocation.getParameter0());
            return null;
        }
        return invocation.invoke();
    }

    private Object getValue(Invocation invocation, String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }

        Class<?> returnType = invocation.getMethod().getReturnType();
        if (returnType.isPrimitive()) {
            if (primitiveDefaultIsException) {
                throw new IllegalStateException("The primitive value: " + name + " has not been configured!");
            }
            return PrimitiveDefaultValue.getValue(returnType);
        }
        return null; // not a primitive.
    }

    private static class PrimitiveDefaultValue {

        private static boolean defaultBoolean;
        private static byte defaultByte;
        private static short defaultShort;
        private static int defaultInt;
        private static long defaultLong;
        private static float defaultFloat;
        private static double defaultDouble;
        private static char defaultChar;

        private PrimitiveDefaultValue() {
            // hidden
        }

        public static Object getValue(Class<?> clazz) {
            if (clazz.equals(boolean.class)) {
                return defaultBoolean;
            } else if (clazz.equals(byte.class)) {
                return defaultByte;
            } else if (clazz.equals(short.class)) {
                return defaultShort;
            } else if (clazz.equals(int.class)) {
                return defaultInt;
            } else if (clazz.equals(long.class)) {
                return defaultLong;
            } else if (clazz.equals(float.class)) {
                return defaultFloat;
            } else if (clazz.equals(double.class)) {
                return defaultDouble;
            } else if (clazz.equals(char.class)) {
                return defaultChar;
            } else {
                return null; // not a primitive, then always defaults to null
            }
        }
    }
}
