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

import java.util.Deque;

/**
 * Interface with methods an intercepteable proxy should implement to be able to
 * add/remove {@link Interceptor}s dynamically.
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
public interface InterceptableProxy {

    /**
     * Adds a {@link Interceptor} to the proxy.
     * 
     * @param interceptor to add
     */
    void addInterceptor(Interceptor interceptor);

    /**
     * Removes a specific {@link Interceptor} from the proxy.
     * 
     * @param interceptor to remove
     */
    void removeInterceptor(Interceptor interceptor);

    /**
     * @return a Deque of all {@link Interceptor}s in the proxy.
     */
    Deque<Interceptor> getInterceptorList();
}
