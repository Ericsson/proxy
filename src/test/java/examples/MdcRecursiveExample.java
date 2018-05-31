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
