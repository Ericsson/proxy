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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.RandomAccess;
import java.util.Set;

import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;

public class UtilTest {

    private static Method methodPublic;
    private static Method methodFinal;
    private static Method methodStatic;
    private static Method methodReturn1;
    private static Method methodException;
    private static Method methodThrowable;
    private static Method methodError;
    private static Method methodListSize;
    private static Method methodAbstractListGet;
    static {
        try {
            methodPublic = UtilTest.class.getDeclaredMethod("method1", null);
            methodFinal = UtilTest.class.getDeclaredMethod("method2", null);
            methodStatic = UtilTest.class.getDeclaredMethod("method3", null);
            methodReturn1 = UtilTest.class.getDeclaredMethod("methodReturn1", null);
            methodException = UtilTest.class.getDeclaredMethod("methodException", null);
            methodThrowable = UtilTest.class.getDeclaredMethod("methodThrowable", null);
            methodError = UtilTest.class.getDeclaredMethod("methodError", null);
            methodListSize = List.class.getDeclaredMethod("size", null);
            methodAbstractListGet = AbstractList.class.getDeclaredMethod("get", int.class);
        } catch (NoSuchMethodException | SecurityException e) {
        }
    }

    public List<Void> method1() {
        return null;
    }

    final List<Void> method2() {
        return null;
    }

    static List<Void> method3() {
        return null;
    }

    int methodReturn1() {
        return 1;
    }

    int methodException() throws Exception {
        throw new Exception("");
    }

    int methodThrowable() throws Throwable {
        throw new Throwable();
    }

    int methodError() {
        throw new Error();
    }

    @Test
    public void methodSignatureEqualsTest() throws Exception {
        assertTrue(Util.methodSignatureEquals(methodPublic, methodPublic));
    }

    @Test
    public void methodSignatureEqualsNotEqualsTest() throws Exception {
        assertFalse(Util.methodSignatureEquals(methodPublic, methodFinal));
    }

    @Test
    public void findMethodWithSignatureTest() throws Exception {
        Method found = Util.findMethodWithSignature(methodPublic, this);
        assertTrue(found != null);
        assertTrue(Util.methodSignatureEquals(methodPublic, found));
    }

    @Test
    public void findMethodWithSignatureInClassTest() throws Exception {
        Method found = Util.findMethodWithSignatureInClass(methodPublic, UtilTest.class);
        assertTrue(found != null);
        assertTrue(Util.methodSignatureEquals(methodPublic, found));
    }

    @Test
    public void invokeMethodWithSignatureTest() throws Throwable {
        int i = (int) Util.invokeMethodWithSignature(methodReturn1, null, this);
        assertTrue(i == 1);
    }

    @Test(expectedExceptions = Exception.class)
    public void invokeMethodWithSignatureExceptionTest() throws Throwable {
        Util.invokeMethodWithSignature(methodException, null, this);
    }

    @Test(expectedExceptions = Throwable.class)
    public void invokeMethodWithSignatureThrowableTest() throws Throwable {
        Util.invokeMethodWithSignature(methodThrowable, null, this);
    }

    @Test(expectedExceptions = Error.class)
    public void invokeMethodWithSignatureErrorTest() throws Throwable {
        Util.invokeMethodWithSignature(methodError, null, this);
    }

    @Test
    public void isMethodDeclaredInInterfaceOrAnAbstractMethodTest() throws Throwable {
        assertFalse(Util.isMethodDeclaredInInterfaceOrAnAbstractMethod(methodPublic));
        assertTrue(Util.isMethodDeclaredInInterfaceOrAnAbstractMethod(methodListSize));
        assertTrue(Util.isMethodDeclaredInInterfaceOrAnAbstractMethod(methodAbstractListGet));
    }

    @Test
    public void isClassAInterfaceOrAbstractTest() throws Throwable {
        assertFalse(Util.isClassAInterfaceOrAbstract(UtilTest.class));
        assertTrue(Util.isClassAInterfaceOrAbstract(List.class));
        assertTrue(Util.isClassAInterfaceOrAbstract(AbstractList.class));
    }

    @Test
    public void isMethodWithImplementationTest() throws Throwable {
        assertTrue(Util.isMethodWithImplementation(methodPublic));
        assertFalse(Util.isMethodWithImplementation(methodListSize));
        assertFalse(Util.isMethodWithImplementation(methodAbstractListGet));
    }

    @Test
    public void isObjectImplementingAllInterfacesTest() throws Throwable {
        assertTrue(Util.isObjectImplementingAllInterfaces(new ArrayList(), List.class));
        assertTrue(Util.isObjectImplementingAllInterfaces(new ArrayList(), List.class, RandomAccess.class,
                Cloneable.class, java.io.Serializable.class, Collection.class, Iterable.class));
        assertFalse(Util.isObjectImplementingAllInterfaces(new ArrayList(), Map.class));
        assertFalse(Util.isObjectImplementingAllInterfaces(new ArrayList(), List.class, Map.class));
    }

    @Test
    public void isPublicAndNotStaticFinalTest() throws Throwable {
        assertTrue(Util.isPublicAndNotStaticFinal(methodPublic.getModifiers()));
        assertFalse(Util.isPublicAndNotStaticFinal(methodFinal.getModifiers()));
        assertFalse(Util.isPublicAndNotStaticFinal(methodStatic.getModifiers()));
    }

    @Test
    public void isStaticNonFinalTest() throws Throwable {
        assertFalse(Util.isStaticNonFinal(methodPublic.getModifiers()));
        assertTrue(Util.isStaticNonFinal(methodStatic.getModifiers()));
        assertFalse(Util.isStaticNonFinal(methodFinal.getModifiers()));
    }

    @Test
    public void isMethodExistingInClassTest() throws Throwable {
        assertTrue(Util.isMethodExistingInClass(methodPublic, UtilTest.class));
        assertTrue(Util.isMethodExistingInClass(methodReturn1, UtilTest.class));
        assertFalse(Util.isMethodExistingInClass(methodListSize, UtilTest.class));
    }

    @Test
    public void defaultToEmptyCollectionsOnNullValueTest() throws Throwable {
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(List.class, null).isEmpty());
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(List.class, Lists.newArrayList("1")).contains("1"));

        Set<Object> set = Sets.newHashSet();
        set.add(1);
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Set.class, set).contains(1));
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Set.class, null).isEmpty());

        Map<Object, Object> map = Maps.newHashMap();
        map.put(1, 2);
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Map.class, map).containsKey(1));
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Map.class, map).containsValue(2));
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Map.class, null).isEmpty());

        Object[] arr = set.toArray(new Object[set.size()]);
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Object[].class, null).length == 0);
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Object[].class, arr).length == 1);

        Properties prop = new Properties();
        prop.put("1", "2");
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Properties.class, prop).containsKey("1"));
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Properties.class, prop).containsValue("2"));
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Properties.class, null).isEmpty());

        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Collection.class, null).isEmpty());
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(Collection.class, set).contains(1));

        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(String.class, null) == null);
        assertTrue(Util.defaultToEmptyCollectionsOnNullValue(String.class, "123").contentEquals("123"));

    }

}
