package examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.proxy.Proxy;

public class MdcDangersExample {

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
