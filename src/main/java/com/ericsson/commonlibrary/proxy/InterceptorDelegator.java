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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A delegator interceptor. It will just delegate every call to a other objects.
 * if no method is found it will call the next interceptor or the original
 * method.
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
final class InterceptorDelegator implements Interceptor {

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorDelegator.class);
    private final Object[] implementations;

    InterceptorDelegator(Object... delegates) {
        implementations = delegates;
    }

    /**
     * Delegate if possible all method calls to some concrete delegate
     * implementation.
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getParameters();

        if (Util.isToStringOrHashcodeOrEqualsMethod(method)) {
            for (Object object : implementations) {
                Method methodToRun = Util.findMethodWithSignature(method,
                        object);
                if (methodToRun != null
                        && !isMethodDeclaredInObjectClass(methodToRun)) {
                    methodToRun.setAccessible(true);
                    return methodToRun.invoke(object, args);
                }
            }
            return invocation.invoke(); //invokes the next interceptor or original
        }

        try {
            return Util.invokeMethodWithSignature(method, args, implementations);
        } catch (MethodWithSignatureNotFoundException e) {
            LOG.trace("Was not able to find a implementation for the method:" + method.getName()
                    + "(...) in this particular delegator. invoking next interceptor...");
            return invocation.invoke();
        }
    }

    static boolean isMethodDeclaredInObjectClass(Method method) {
        return method.getDeclaringClass() == Object.class;
    }
}
