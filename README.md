<!---
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
--->

# Proxy
A small yet powerful interception library that lets you manipulate existing objects and classes behavior runtime, 
It's achieving this by using [javassist](http://jboss-javassist.github.io/javassist/) to do bytecode manipulation,
Proxy has a [fluent](http://en.wikipedia.org/wiki/Fluent_interface) interception API:

```java
package examples;

import static com.ericsson.commonlibrary.proxy.Proxy.with;

public class FluentExample {

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

```
## What is Proxy and why use it?

Proxy is a highly general-purpose library that solve a typical Java development problem (Runtime change of behavior for classes and objects) that no other open source solution does today.
Proxy is powerful interception library that lets you manipulate existing objects and classes(by internally using bytecode manipulation). 

Using Proxy can allow you to architecturally implement your code completely differently using it’s features like:

* Building complex object from small objects.
* Create simple java beans objects without providing an implementation.
* Dynamically change an interface of an object (duck typing)
* Replacing an existing object with modified version.
* [Mixin](https://en.wikipedia.org/wiki/Mixin) / multiple inheritance functionality. (Not present in Java by default)
* [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming) like features.

Proxy is a great building block for creating other innovative solutions.

## Alternative Solutions
* Proxy like functionality is already included in Java itself but **limited to interfaces only** which is often not enough.
  * Often referred to as JDK dynamic proxies
  * https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html 
* [AspectJ](http://www.eclipse.org/aspectj/doc/next/progguide/) very powerful but requires special plugins and learn a new _language_ 
  * Great at static interception done **during compilation**
  * Is missing most of the **runtime** features of Proxy.
* [Spring-AOP](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-api )
  * Proxy is much more lightweight in comparison.
  * It does bytecode manipulation with cglib similarly to how Proxy does the same with Javassist
  * It is missing features like: __recursive interception__ and __delegation__ and **object interception**

## User guide 
**Under Construction**:  https://ericsson.github.io/proxy

## How to Propose Changes
Anyone is welcome to propose changes to this repository by creating a new [Issue](https://github.com/Ericsson/proxy/issues) ticket in GitHub. These requests may concern anything contained in the repo: changes to documentation, changes to interfaces, changes to implementations, additional tests et cetera.

When posting a new issue, try to be as precise as possible and phrase your arguments for your request carefully. Keep in mind that collaborative software development is often an exercise in finding workable compromises between multiple and often conflicting needs. In particular, pay attention to the following:
1. What type of change is requested?
1. Why would you like to see this change?
1. Can you provide any concrete examples?
1. Which arguments in favor can you think of?
1. Which arguments against can you think of, and why are they outweighed by the arguments in favor?

Also, keep in mind that just as anyone is welcome to propose a change, anyone is welcome to disagree with and criticize that proposal.

## How to Contribute
While we welcome requests for changes (in the form of Issues), we absolutely love ready solutions (in the form of Pull Requests). The best requests are the ones with Pull Requests to go along with them.

Contributions can be made by anyone using the standard [GitHub Fork and Pull model](https://help.github.com/articles/about-pull-requests). When making a pull request, keep a few things in mind.
1. Always explicitly connect a pull request to an Issue. See How to Propose Changes above for further information.
1. Pull Requests will be publicly reviewed, criticized, and potentially rejected. Don't take it personally.

## [License](./LICENSE.md)

**Proxy** is licensed under the [MIT License](https://opensource.org/licenses/MIT).

## [Code of Conduct](./CODE_OF_CONDUCT.md)
