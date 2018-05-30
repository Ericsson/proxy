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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.Proxy;

public class MdcRecursiveExample {

    public static void main(String[] args) {
        SomeImpl objectToLog = new SomeImpl();
        objectToLog = Proxy.mdcLogging(objectToLog, "id", "id001");
        objectToLog.log("1");

        SomeImpl2 impl = objectToLog.getImpl();
        impl.log("2");
        SomeImpl3 impl2 = impl.getImpl();
        impl2.log("3");

    }

    public static class SomeImpl {

        private final SomeImpl2 impl = new SomeImpl2();
        private static final Logger LOG = LoggerFactory.getLogger(MdcRecursiveExample.SomeImpl.class);

        public void log(String log) {
            LOG.info(log);
        }

        public SomeImpl2 getImpl() {
            return impl;
        }
    }

    public static class SomeImpl2 {

        private final SomeImpl3 impl = new SomeImpl3();

        private static final Logger LOG = LoggerFactory.getLogger(MdcRecursiveExample.SomeImpl2.class);

        public void log(String log) {
            LOG.info(log);
        }

        public SomeImpl3 getImpl() {
            return impl;
        }
    }

    public static class SomeImpl3 {

        private static final Logger LOG = LoggerFactory.getLogger(MdcRecursiveExample.SomeImpl3.class);

        public void log(String log) {
            LOG.info(log);
        }
    }
}
