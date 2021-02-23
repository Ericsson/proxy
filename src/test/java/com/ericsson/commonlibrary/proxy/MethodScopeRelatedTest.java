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

import org.testng.annotations.Test;

public class MethodScopeRelatedTest {

    @Test
    public void ableOverridePackagePrivateMethodWithAPublic() throws Exception {
        PackagePrivateMethod method = new PackagePrivateMethod("private");
        assertEquals(method.returnString(), "private");

        assertEquals(method.publicReturnString(), "public");

        method = Proxy.delegate(method, new PublicMethod("override"));

        assertEquals(method.returnString(), "override");
        assertEquals(method.publicReturnString(), "public");
    }

    @Test
    public void ableOverridePublicMethodWithPackagePrivate() throws Exception {
        PublicMethod method = new PublicMethod("public");
        assertEquals(method.returnString(), "public");

        method = Proxy.delegate(method, new PackagePrivateMethod("override"));

        assertEquals(method.returnString(), "override");
    }

    public static class PackagePrivateMethod {

        private final String string;

        public PackagePrivateMethod(String string) {
            this.string = string;
        }

        String returnString() {
            return string;
        }

        public String publicReturnString() {
            return "public";
        }

    }

    public static class PublicMethod {

        private final String string;

        public PublicMethod(String string) {
            this.string = string;
        }

        public String returnString() {
            return string;
        }

    }
}
