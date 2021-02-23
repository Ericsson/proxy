package examples;

import com.ericsson.commonlibrary.proxy.Proxy;

public class JavaBeanExample {

    public static void main(String[] args) {
        // Normally without the proxy library.
        JavaBean bean = new JavaBeanImpl(); // requires you to create JavaBeanImpl with implementation.
        bean.setName("bean");
        System.out.println("bean name:" + bean.getName());

        // "Proxy.javaBean" will dynamically create a class that acts exactly like JavaBeanImpl.
        // This means that JavaBeanImpl is no longer needed can be removed.
        JavaBean proxyBean = Proxy.javaBean(JavaBean.class);
        proxyBean.setName("proxy");
        System.out.println("bean proxy name:" + proxyBean.getName());
    }

    public interface JavaBean {

        String getName();

        void setName(String name);
    }

    // NOT needed when using the proxy solution
    public static class JavaBeanImpl implements JavaBean {

        private String name;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }
    }
}
