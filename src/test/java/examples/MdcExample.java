package examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.Proxy;

public class MdcExample {

    public static void main(String[] args) {
        SomeImpl objectToLog = new SomeImpl();
        objectToLog = Proxy.mdcLogging(objectToLog, "id", "id001");
        objectToLog = Proxy.mdcLogging(objectToLog, "instance", "inst01");
        objectToLog.log("Hello World");

        SomeImpl objectToLog2 = new SomeImpl();
        objectToLog2 = Proxy.mdcLogging(objectToLog2, "id", "id001");
        objectToLog2 = Proxy.mdcLogging(objectToLog2, "instance", "inst02");
        objectToLog2.log("Hello World2");

        //Expected output:
        //2013-06-25 10:40:17,374 Logger:com.ericsson.commonlibrary.proxy.examples.MdcExample$SomeImpl [id001.inst01] Hello World
        //2013-06-25 10:40:17,376 Logger:com.ericsson.commonlibrary.proxy.examples.MdcExample$SomeImpl [id001.inst02] Hello World2
        //log4j configured: log4j.appender.A1.layout.ConversionPattern=%d{ISO8601} Logger:%c [%X{id}.%X{instance}] %m\n
    }

    public static class SomeImpl {

        private static final Logger LOG = LoggerFactory.getLogger(MdcExample.SomeImpl.class);

        public void log(String log) {
            LOG.info(log);
        }
    }
}
