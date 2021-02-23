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
package examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.commonlibrary.proxy.Interceptor;
import com.ericsson.commonlibrary.proxy.Invocation;
import com.ericsson.commonlibrary.proxy.Proxy;

public class CountDownCollectionRecursionExample {
    public static void main(String[] args) {
        // Anonymous class, should usually be put in a separate class.
        Interceptor countDown = new Interceptor() {

            @Override
            public Object intercept(Invocation invocation) throws Throwable {
                if (invocation.getMethodName().contains("add")) {
                    invocation.invoke(); // invoke original
                    Integer val = (Integer) invocation.getParameter0();
                    if (val > 0) {
                        Collection list = (Collection) invocation.getThis();
                        return list.add(val - 1); // invoke proxy again.
                    }
                    return true;
                }
                return invocation.invoke(); // invokes method as normal
            }
        };

        List<Integer> list = Proxy.intercept(new ArrayList<Integer>(), countDown);
        list.add(4);
        list.add(6);
        list.add(3);
        System.out.println(list); // [4, 3, 2, 1, 0, 6, 5, 4, 3, 2, 1, 0, 3, 2, 1, 0]

        Set<Integer> set = Proxy.intercept(new HashSet<Integer>(), countDown);
        set.add(4);
        System.out.println(set); // [0, 1, 2, 3, 4]
        set.add(6);
        System.out.println(set); // [0, 1, 2, 3, 4, 5, 6]
        set.add(3);
        System.out.println(set); // [0, 1, 2, 3, 4, 5, 6]

    }
}
