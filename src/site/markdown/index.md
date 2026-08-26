## Table of Contents

<!-- MACRO{toc|fromDepth=1|toDepth=3} --> 

## Introduction

A small yet powerful interception library that lets you manipulate existing objects and classes behavior runtime, 
It's achieving this by using [javassist](http://jboss-javassist.github.io/javassist/) or
[Byte Buddy](https://bytebuddy.net/) to do bytecode manipulation, see [Bytecode engines](#bytecode-engines),

### Interesting features

* Easy to use
* Gives you Java super powers
* Well tested

Using Proxy can allow you to architecturally implement your code completely differently with features like:

* Building complex object from small objects.
* Dynamically change an interface of an object (duck typing)
* Replacing an existing object with modified version.
* [Mixin](https://en.wikipedia.org/wiki/Mixin) / multiple inheritance functionality. (Not present in Java by default)
* [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming) like features.
* Create simple java beans objects without providing an implementation.

## Examples

This chapter will present some usage examples of this library. This list is in no way a full list of what you can do with this library.
  
### Example: Building aggregate delegation objects from small implementations

Variant with a fluent style 

<!-- MACRO{include|source=examples.BuildingObjectsFluentExample} --> 

Variant with a typical static style 

<!-- MACRO{include|source=examples.BuildingObjectsExample} --> 

### Example: Altered behavior with recursive nature

<!-- MACRO{include|source=examples.CountDownCollectionRecursionExample} --> 

### Example: Compact interception with lambdas 

<!-- MACRO{include|source=examples.LambdaInterceptionExample} -->


### Example: Remove all Java bean implementation and stick with interfaces
 
<!-- MACRO{include|source=examples.JavaBeanExample} --> 

### Example: Alter existing objects in Java, like the console.

<!-- MACRO{include|source=examples.RedirectConsoleExample} --> 

### Example: MDC logging, add contextual metadata to your logging.

See more info at: [MDC logging](http://www.baeldung.com/mdc-in-log4j-2-logback) 

<!-- MACRO{include|source=examples.MdcExample} --> 
<!-- MACRO{include|source=examples.MdcRecursiveExample} --> 
<!-- MACRO{include|source=examples.MdcDangersExample} --> 

### Example: Simplified explaination on how you can visualize how Proxy actually working.

<!-- MACRO{include|source=examples.InterceptionInnerWorkingsExplaination} --> 

## Bytecode engines

Proxy ships with two interchangeable bytecode manipulation engines,
[javassist](http://jboss-javassist.github.io/javassist/) and [Byte Buddy](https://bytebuddy.net/).
They implement the same behavior, so the engine is purely an implementation detail and no code that uses `Proxy`
has to change when you switch. The whole test suite is executed twice by the build, once per engine, to keep it that way.

Javassist is the default. Select another engine for the whole JVM with a system property:

```bash
java -Dproxy.engine=bytebuddy your.Application
```

Or select it from code, globally, per thread, or for a single call:

<!-- MACRO{include|source=examples.ProxyEngineExample} --> 

Proxies from both engines can be mixed freely in the same JVM, and a proxy created by one engine can be proxied again by
the other. Already created proxies keep the engine they were created with.

