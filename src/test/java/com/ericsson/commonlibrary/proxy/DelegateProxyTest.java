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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.ericsson.commonlibrary.proxy.helpobjects.AddBlocker;
import com.ericsson.commonlibrary.proxy.helpobjects.ContainsThrowsException;
import com.ericsson.commonlibrary.proxy.helpobjects.FinalMethod;
import com.ericsson.commonlibrary.proxy.helpobjects.IAdd;
import com.ericsson.commonlibrary.proxy.helpobjects.MyInterface;
import com.ericsson.commonlibrary.proxy.helpobjects.MySubImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.MySubImpl2;
import com.ericsson.commonlibrary.proxy.helpobjects.ObjectReturnTrueFalse;
import com.ericsson.commonlibrary.proxy.helpobjects.OverrideFinalMethod;
import com.ericsson.commonlibrary.proxy.helpobjects.PolymorfismOnClassDelegatesImpl;
import com.ericsson.commonlibrary.proxy.helpobjects.ReturnString1;
import com.ericsson.commonlibrary.proxy.helpobjects.Size10;
import com.ericsson.commonlibrary.proxy.helpobjects.ToString;

public class DelegateProxyTest {

    @Test
    public void oneDelegateBlockAdd() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new AddBlocker());
        list.add("hello");
        assertEquals(list.size(), 0);
    }

    @Test
    public void oneDelegateSize10() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new Size10());
        assertEquals(list.size(), 10);
    }

    @Test
    public void notAbleToOverrideFinalMethod() throws Exception {
        FinalMethod method = new FinalMethod();
        assertEquals(method.finalmethod(), "final");
        assertEquals(method.notfinalmethod(), "notfinal");
        method = Proxy.delegate(method,
                new OverrideFinalMethod());

        assertEquals(method.finalmethod(), "final");
        assertEquals(method.notfinalmethod(), "override");
    }

    @Test
    public void operatesOnOriginalIfNoDelegateMethod() throws Exception {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("hello");
        arrayList.add("world");
        assertEquals(arrayList.size(), 2);
        List<String> listProxy = Proxy.delegate(arrayList, new Size10());
        assertEquals(listProxy.size(), 10);
        assertEquals(arrayList.size(), 2);

        assertTrue(listProxy.contains("hello"));
        assertTrue(listProxy.contains("world"));
        assertFalse(listProxy.contains("world2"));
    }

    @Test
    public void delagateWithNoInterfacesInvolved() throws Exception {
        ObjectReturnTrueFalse objFalse = new ObjectReturnTrueFalse(false);
        assertFalse(objFalse.returnBoolean());

        ObjectReturnTrueFalse objProxy = Proxy.delegate(objFalse,
                new ObjectReturnTrueFalse(
                        true));

        assertTrue(objProxy.returnBoolean());
        assertFalse(objFalse.returnBoolean());
    }

    @Test
    public void interfaceDelegate() throws Exception {
        MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class,
                new MySubImpl2(), new MySubImpl());
        assertEquals(interfaceDelegator.doSomething(), "MySubImpl");
        assertEquals(interfaceDelegator.doSomethingElse(), "MySubImpl2");
        assertEquals(interfaceDelegator.doSomethingWithInteger(1), 1);

        // Order matters. now it will find doSomethingElse in MyImpl first.
        MyInterface interfaceDelegator2 = Proxy.delegate(MyInterface.class,
                new MySubImpl(), new MySubImpl2());
        assertEquals(interfaceDelegator2.doSomething(), "MySubImpl");
        assertEquals(interfaceDelegator2.doSomethingElse(), "MySubImpl");
        assertEquals(interfaceDelegator2.doSomethingWithInteger(1), 1);
    }

    @Test
    public void interfaceDelegateToStringDefaultIfNotOverriden() throws Exception {
        MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class,
                new MySubImpl2(), new MySubImpl());
        assertTrue(interfaceDelegator.toString().contains("MyInterface"));

    }

    @Test
    public void interfaceDelegateToString() throws Exception {
        MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class,
                new MySubImpl2(), new ToString(), new MySubImpl());
        assertEquals(interfaceDelegator.toString(), "ToString");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void throwsExceptionIfMethodDoesNotExistInADelegate()
            throws Exception {
        MyInterface interfaceDelegator = Proxy.delegate(MyInterface.class, new MySubImpl());
        interfaceDelegator.doSomething();
        interfaceDelegator.doSomethingWithInteger(1);
    }

    @Test
    public void delegateCombinedWithInterfaceNarrower() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new AddBlocker());
        assertFalse(list.add("hello"));
        IAdd addObject = Proxy.changeInterface(IAdd.class, list);
        assertFalse(addObject.add("world"));
    }

    @Test
    public void delegateCombinedWithInterfaceNarrower2() throws Exception {
        List<String> list = new ArrayList<String>();
        IAdd addObject = Proxy.changeInterface(IAdd.class, list);
        assertTrue(addObject.add("world"));

        addObject = Proxy.delegate(addObject, new AddBlocker());
        assertFalse(addObject.add("hello"));
    }

    @Test
    public void interfaceNarrower() throws Exception {
        List<String> list = new ArrayList<String>();
        IAdd addObject = Proxy.changeInterface(IAdd.class, list);
        assertTrue(addObject.add("world"));
        assertEquals(list.size(), 1);
    }

    @Test
    public void delegateThrowsException() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new ContainsThrowsException());
        try {
            list.contains("hello");
            fail();
        } catch (NullPointerException e) {
            // success.
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void twoDelegatesBlockAddAndSize10() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new Size10(), new AddBlocker());
        list.add("hello");
        assertEquals(list.size(), 10);
        assertFalse(list.contains("hello"));
    }

    @Test
    public void addDelegatesRuntime() throws Exception {
        List<String> list = Proxy.delegate(new ArrayList<String>(),
                new Size10());
        list.add("hello");
        assertEquals(list.size(), 10);
        assertTrue(list.contains("hello"));

        Proxy.delegate(list, new AddBlocker());

        list.add("world");
        assertEquals(list.size(), 10);
        assertTrue(list.contains("hello"));
        assertFalse(list.contains("world"));
    }

    @Test
    public void createNewProxyClass() throws Exception {
        List<String> list = Proxy.delegate(ArrayList.class,
                new Size10());
        list.add("hello");
        assertEquals(list.size(), 10);
        assertTrue(list.contains("hello"));
    }

    @Test
    public void polymorfisitcBehaviorOnClassDelegatesTest() throws Exception {

        PolymorfismOnClassDelegatesImpl proxy = Proxy.delegate(
                PolymorfismOnClassDelegatesImpl.class, new ReturnString1("3"));
        assertEquals(proxy.returnString1(), "3");
        assertEquals(proxy.returnString2(), "2");
        assertEquals(proxy.returnString1Times2(), "33");
        assertEquals(proxy.returnString2Times2(), "22");
    }

}
