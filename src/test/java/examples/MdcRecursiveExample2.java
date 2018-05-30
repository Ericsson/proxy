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

public class MdcRecursiveExample2 {

    public static void main(String[] args) {
        SomeImpl objectToLog = new SomeImpl();
        objectToLog.log("1");
        objectToLog = Proxy.mdcLogging(objectToLog, "id", "id001");
        objectToLog.log("1");
    }

    public static class SomeImpl {

        private static final Logger LOG = LoggerFactory.getLogger(SomeImpl.class);

        //Not dangerous
        private static final String stringStaticFinal = "hello";
        private final String stringPrivate = "hello";

        //dangerous. Will prevent Proxy from from adding mdc
        //    private static String stringStatic;
        //    public static String stringStaticPublic;
        //    public String stringPublic;
        //    public final String stringPublicFinal = "final";

        public void log(String log) {
            LOG.info(log);

        }
    }

}
