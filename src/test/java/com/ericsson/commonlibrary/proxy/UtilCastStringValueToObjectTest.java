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
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

public class UtilCastStringValueToObjectTest {

    @Test
    public void castStringValueToObjectIntegerTest() throws Exception {
        assertEquals(Util.castStringValueToObject("22", Integer.class), (Integer) 22);
        assertEquals(Util.castStringValueToObject("33", int.class), (Integer) 33);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectIntegerFailTest() throws Exception {
        Util.castStringValueToObject("22X", Integer.class);
    }

    @Test
    public void castStringValueToObjectBooleanTest() throws Exception {
        assertEquals(Util.castStringValueToObject("true", Boolean.class), (Boolean) true);
        assertEquals(Util.castStringValueToObject("false", boolean.class), (Boolean) false);
    }

    @Test
    public void castStringValueToObjectBooleanFailTest() throws Exception {
        assertEquals(Util.castStringValueToObject("truee", boolean.class), (Boolean) false);

    }

    @Test
    public void castStringValueToObjectStringTest() throws Exception {
        assertEquals(Util.castStringValueToObject("true1", String.class), "true1");
    }

    @Test
    public void castStringValueToObjectFloatTest() throws Exception {
        assertEquals(Util.castStringValueToObject("1.1", Float.class), 1.1f);
        assertEquals(Util.castStringValueToObject("1.2", float.class), 1.2f);
        assertEquals(Util.castStringValueToObject("1.2f", float.class), 1.2f);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectFloatFailTest() throws Exception {
        Util.castStringValueToObject("1.1X", Float.class);
    }

    @Test
    public void castStringValueToObjectDoubleTest() throws Exception {
        assertEquals(Util.castStringValueToObject("1.1", Double.class), 1.1);
        assertEquals(Util.castStringValueToObject("1.2", double.class), 1.2);
        assertEquals(Util.castStringValueToObject("1.2d", double.class), 1.2);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectDoubleFailTest() throws Exception {
        Util.castStringValueToObject("1.1X", Double.class);
    }

    @Test
    public void castStringValueToObjectLongTest() throws Exception {
        assertEquals(Util.castStringValueToObject("1", Long.class), (Long) 1L);
        assertEquals(Util.castStringValueToObject("2", long.class), (Long) 2L);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectLongFailTest() throws Exception {
        Util.castStringValueToObject("1V", Long.class);
    }

    @Test
    public void castStringValueToObjectByteTest() throws Exception {
        assertEquals(Util.castStringValueToObject("1", Byte.class), (Byte) (byte) 1);
        assertEquals(Util.castStringValueToObject("2", byte.class), (Byte) (byte) 2);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectByteFailTest() throws Exception {
        Util.castStringValueToObject("1s", Byte.class);
    }

    @Test
    public void castStringValueToObjectShortTest() throws Exception {
        assertEquals(Util.castStringValueToObject("1", Short.class), (Short) (short) 1);
        assertEquals(Util.castStringValueToObject("2", short.class), (Short) (short) 2);
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void castStringValueToObjectShortFailTest() throws Exception {
        Util.castStringValueToObject("1a", Short.class);
    }

    @Test
    public void castStringValueToObjectCharTest() throws Exception {
        assertEquals(Util.castStringValueToObject("A", Character.class), (Character) 'A');
        assertEquals(Util.castStringValueToObject("B", char.class), (Character) 'B');
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void castStringValueToObjectCharFailTest() throws Exception {
        Util.castStringValueToObject("A12", Character.class);
    }

    @Test
    public void castStringValueToObjectClassTest() throws Exception {
        assertEquals(Util.castStringValueToObject("java.lang.String", Class.class), java.lang.String.class);
        assertEquals(Util.castStringValueToObject("java.util.List", Class.class), java.util.List.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Misspelled.*")
    public void castStringValueToObjectClassFailTest() throws Exception {
        Util.castStringValueToObject("java.lang.Misspelled", Class.class);
    }

    @Test
    public void castStringValueToObjectEnumTest() throws Exception {
        assertEquals(Util.castStringValueToObject("Val1", MyEnum.class), MyEnum.Val1);
        assertEquals(Util.castStringValueToObject("Val2", MyEnum.class), MyEnum.Val2);
        assertEquals(Util.castStringValueToObject("Val3", MyEnum.class), MyEnum.Val3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void castStringValueToObjectEnumFailTest() throws Exception {
        Util.castStringValueToObject("ValX", MyEnum.class);
    }

    @Test
    public void castStringValueToObjectFileTest() throws Exception {
        assertEquals(Util.castStringValueToObject("com/ericsson", File.class), new File("com/ericsson"));
    }

    @Test
    public void castStringValueToObjectListTest() throws Exception {
        List<String> list = Util.castStringValueToObject("one,two", List.class);
        assertEquals(list.size(), 2);
        assertTrue(list.contains("one"));
        assertTrue(list.contains("two"));
    }

    @Test
    public void castStringValueToObjectMapTest() throws Exception {
        Map<String, String> map = Util.castStringValueToObject("one=1,two=2", Map.class);
        assertEquals(map.size(), 2);
        assertTrue(map.get("one").equals("1"));
        assertTrue(map.get("two").equals("2"));
    }

    @Test
    public void castStringValueToObjectSetTest() throws Exception {
        Set<String> set = Util.castStringValueToObject("one,two,one", Set.class);
        assertEquals(set.size(), 2);
        assertTrue(set.contains("one"));
        assertTrue(set.contains("two"));
    }

    @Test
    public void castStringValueToStringArrayTest() throws Exception {
        String[] arr = Util.castStringValueToObject("one,two", String[].class);
        assertEquals(arr.length, 2);
        assertTrue(arr[0].equals("one"));
        assertTrue(arr[1].equals("two"));
    }

    @Test
    public void castStringValueToIntegerArrayTest() throws Exception {
        Integer[] arr = Util.castStringValueToObject("1,2", Integer[].class);
        assertEquals(arr.length, 2);
        assertTrue(arr[0] == 1);
        assertTrue(arr[1] == 2);
    }

    @Test
    public void castStringValueToMethodReturnTypeListTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("listInt", null);
        List list = Util.castStringValueToMethodReturnType("1,2,3", List.class, method);
        assertEquals(list.get(0), 1);
        assertEquals(list.get(1), 2);
        assertEquals(list.get(2), 3);
    }

    @Test
    public void castStringValueToMethodReturnTypeEmptyListTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("listInt", null);
        List list = Util.castStringValueToMethodReturnType("", List.class, method);
        assertTrue(list.isEmpty());
    }

    List<Integer> listInt() {
        return null;
    }

    @Test
    public void castStringValueToMethodReturnTypeSetTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("setInt", null);
        Set set = Util.castStringValueToMethodReturnType("1,2,3", Set.class, method);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    public void castStringValueToMethodReturnTypeEmptySetTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("setInt", null);
        Set set = Util.castStringValueToMethodReturnType("", Set.class, method);
        assertTrue(set.isEmpty());
    }

    Set<Integer> setInt() {
        return null;
    }

    @Test
    public void castStringValueToMethodReturnTypeMapTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("mapStringInt", null);
        Map<String, Integer> map = Util.castStringValueToMethodReturnType("one=1,two=2,three=3",
                Map.class, method);
        assertEquals(map.get("one"), (Integer) 1);
        assertEquals(map.get("two"), (Integer) 2);
        assertEquals(map.get("three"), (Integer) 3);
    }

    @Test
    public void castStringValueToMethodReturnTypeEmptyMapTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("mapStringInt", null);
        Map<String, Integer> map = Util.castStringValueToMethodReturnType("",
                Map.class, method);
        assertTrue(map.isEmpty());
    }

    Map<String, Integer> mapStringInt() {
        return null;
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void castStringValueToObjectUnsupportedTypeTest() throws Exception {
        Object object = Util.castStringValueToObject("null", Void.class);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void castStringValueToObjectUnsupportedListTypeTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("listVoid", null);
        List list = Util.castStringValueToMethodReturnType("1", List.class, method);
    }

    List<Void> listVoid() {
        return null;
    }

    @Test
    public void castStringValueToObjectIntegerNoCollectionTest() throws Exception {
        assertEquals(Util.castStringValueToObject("22", Integer.class), (Integer) 22);
    }

    @Test
    public void castStringValueToObjectIntegerInvocationTest() throws Exception {
        Method method = this.getClass().getDeclaredMethod("listVoid", null);
        Invocation invocation = new Invocation(this, method, method, null, null);
        assertEquals(Util.castStringValueToObject("22", Integer.class, invocation), (Integer) 22);
    }

    enum MyEnum {
        Val1,
        Val2,
        Val3;
    }
}
