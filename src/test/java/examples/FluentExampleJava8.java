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

import static com.ericsson.commonlibrary.proxy.Proxy.with;

public class FluentExampleJava8 {

    public static class SomeImpl { //sample class

        public void log(String log) {
            System.out.println(log);
        }
    }

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {

        //lambda on class
        SomeImpl obj = with(SomeImpl.class)
                .interceptAll(i -> {
                    System.out.println("before method: " + i.getMethodName() + " param: " + i.getParameter0());
                    return i.invoke();
                }).get();
        obj.log("123");
        //Console output:
        //        before method: log param: 123
        //        123

        //lambda on object
        SomeImpl obj2 = with(new SomeImpl())
                .interceptAll(i -> {
                    Object result = i.invoke();
                    System.out.println("after method: " + i.getMethodName() + " param: " + i.getParameter0());
                    return result;
                }).get();
        obj2.log("321");
        //Console output:
        //        321
        //        after method: log param: 321

        //lambda without return.
        SomeImpl obj3 = with(SomeImpl.class)
                .interceptAll((i) -> System.out.println("Replace method invocation: " + i.getMethodName()))
                .get();
        obj3.log("12345");
        //Console output:
        //        Replace method invocation: log
    }
}
