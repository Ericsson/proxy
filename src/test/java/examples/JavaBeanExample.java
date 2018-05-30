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

import com.ericsson.commonlibrary.proxy.Proxy;

public class JavaBeanExample {

    public static void main(String[] args) {
        //Normally without the proxy library.
        JavaBean bean = new JavaBeanImpl(); // requires you to create JavaBeanImpl with implementation.
        bean.setName("bean");
        System.out.println("bean name:" + bean.getName());

        //"Proxy.javaBean" will dynamically create a class that acts exactly like JavaBeanImpl.
        //This means that JavaBeanImpl is no longer needed can be removed.
        JavaBean proxyBean = Proxy.javaBean(JavaBean.class);
        proxyBean.setName("proxy");
        System.out.println("bean proxy name:" + proxyBean.getName());
    }

    public interface JavaBean {

        String getName();

        void setName(String name);
    }

    //NOT needed BY the proxy solution
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
