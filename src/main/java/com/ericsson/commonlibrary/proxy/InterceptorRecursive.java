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

/**
 * An utility decoration {@link Interceptor} that makes itself and the real provided {@link Interceptor} to be added to
 * every returned object on the intercepted Object, t this will make the this interceptor spread itself in the object
 * tree.
 *
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptorRecursive implements Interceptor {

    private Interceptor actualInterceptor;
    private boolean shouldInterceptPrivateMethods = false;

    InterceptorRecursive(Interceptor actualInterceptor) {
        this.actualInterceptor = actualInterceptor;
    }

    InterceptorRecursive(Interceptor actualInterceptor, boolean shouldInterceptPrivateMethods) {
        this.actualInterceptor = actualInterceptor;
        this.shouldInterceptPrivateMethods = shouldInterceptPrivateMethods;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object returnObject = actualInterceptor.intercept(invocation);
        return Util.tryToAddInterceptorToObject(returnObject, this, invocation, shouldInterceptPrivateMethods);
    }

}
