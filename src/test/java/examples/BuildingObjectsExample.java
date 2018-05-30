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

public class BuildingObjectsExample {

    public static void main(String[] args) {
        //Building all possible combinations of Algorithm. this would not be possible with inheritance without duplication
        Algorithm obj1 = Proxy.delegate(Algorithm.class, new Sort1(), new Reverse1());
        Algorithm obj2 = Proxy.delegate(Algorithm.class, new Sort1(), new Reverse2());
        Algorithm obj3 = Proxy.delegate(Algorithm.class, new Sort2(), new Reverse1());
        Algorithm obj4 = Proxy.delegate(Algorithm.class, new Sort2(), new Reverse2());

        System.out.println(obj2);
        System.out.println(obj2.algorithmReverse("obj2"));
        System.out.println(obj2.algorithmSort("obj2"));
        System.out.println("-----------");

        System.out.println(obj4);
        System.out.println(obj4.algorithmReverse("obj4"));
        System.out.println(obj4.algorithmSort("obj4"));
        System.out.println("-----------");

        //Object methods are handled a bit special. Here it will use the toString() impl in ToString class even if all the other delegate objects contain a toString() method inherited from class Object.
        Algorithm objWithSpecialToString = Proxy.delegate(Algorithm.class, new Sort2(), new ToString(), new Reverse1());
        System.out.println(objWithSpecialToString);
        System.out.println(objWithSpecialToString.algorithmReverse("objWithSpecialToString"));
        System.out.println(objWithSpecialToString.algorithmSort("objWithSpecialToString"));
        System.out.println("-----------");

        //You can replace specific parts of existing implementations
        Algorithm changeExistingImplClass = Proxy.delegate(AlgorithmImpl.class, new Sort2());
        System.out.println(changeExistingImplClass);
        System.out.println(changeExistingImplClass.algorithmReverse("changeExistingImplClass"));
        System.out.println(changeExistingImplClass.algorithmSort("changeExistingImplClass"));
        System.out.println("-----------");

        //You can even replace specific parts of existing objects
        Algorithm changeExistingImplObject = Proxy.delegate(new AlgorithmImpl(), new Reverse1());
        System.out.println(changeExistingImplObject);
        System.out.println(changeExistingImplObject.algorithmReverse("changeExistingImplObject"));
        System.out.println(changeExistingImplObject.algorithmSort("changeExistingImplObject"));
    }

    public interface Algorithm {

        String algorithmSort(String arg);

        String algorithmReverse(String arg);
    }

    public static class AlgorithmImpl implements Algorithm {

        @Override
        public String algorithmSort(String arg) {
            return "sort3: " + arg;
        }

        @Override
        public String algorithmReverse(String arg) {
            return "reverse3: " + arg;
        }
    }

    public static class Sort1 {

        public String algorithmSort(String arg) {
            return "sort1: " + arg;
        }
    }

    public static class Sort2 {

        public String algorithmSort(String arg) {
            return "sort2: " + arg;
        }
    }

    public static class Reverse1 {

        public String algorithmReverse(String arg) {
            return "reverse1: " + arg;
        }
    }

    public static class Reverse2 {

        public String algorithmReverse(String arg) {
            return "reverse2: " + arg;
        }
    }

    public static class ToString {

        @Override
        public String toString() {
            return "special toString";
        }
    }
}
