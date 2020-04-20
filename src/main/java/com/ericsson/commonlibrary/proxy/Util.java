/*
 * Copyright (c) 2018 Ericsson
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ericsson.commonlibrary.proxy;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of utilities that could be useful in the interception domain.
 * 
 * @author Elis Edlund (elis.edlund@ericsson.com)
 */
public final class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private Util() {
        //hidden
    }

    /**
     * Check if the given object implements the given set of interfaces.
     * 
     * @param <T> target object type
     * @param object to check if it implement interfaces
     * @param interfaces that the object should implement
     * @return if the object implements all interfaces
     */
    public static <T> boolean isObjectImplementingAllInterfaces(final T object,
            Class<?>... interfaces) {
        for (Class<?> class1 : interfaces) {
            if (!class1.isAssignableFrom(object.getClass())) {
                return false; //object did not implement all interfaces.
            }
        }

        return true;
    }

    /**
     * Find and invoke the first found method that is matching the given methodSignature.
     * notice the methodSignature does not have the same Method object so declaredClass and
     * returntype is not part of signature matching.
     * 
     * @param methodSignature a method signature that you want to find and invoke,
     * @param args argument to send to the method
     * @param objectsToLookIn objects to look for the method in.
     * @return the returned value of the invoked method
     * @throws MethodWithSignatureNotFoundException if the no method was found
     * @throws Throwable any type of exception/error including actual ones from the method called.
     */
    static Object invokeMethodWithSignature(Method methodSignature, Object[] args,
            Object... objectsToLookIn) throws Throwable {
        for (Object object : objectsToLookIn) {
            Method methodInObject = findMethodWithSignature(methodSignature, object);
            if (methodInObject != null) {
                return invokeMethodInObject(args, object, methodInObject);
            }
        }
        throw new MethodWithSignatureNotFoundException(
                "Was not able to find a implementation for the method:" + methodSignature.getName()
                        + "(...)");
    }

    @SuppressWarnings({ "squid:S1166", "squid:S00112" })
    private static Object invokeMethodInObject(Object[] args, Object object, final Method methodInObject)
            throws Throwable {
        try {
            methodInObject.setAccessible(true);
            return methodInObject.invoke(object, args);
        } catch (IllegalAccessException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Get all interfaces implemented by the passed in objects.
     * 
     * @param objects that you want to take all the interfaces from.
     * @return interfaces.
     */
    public static Class<?>[] getInterfacesImplementedByObjects(Object... objects) {
        Class<?>[] interfaces = new Class[0];
        for (Object delegate : objects) {
            interfaces = Util.concatArrays(interfaces, delegate.getClass()
                    .getInterfaces());
        }
        return interfaces;
    }

    /**
     * Check if two methods have the same signature. Return type(not part of the
     * signature) and declaring class is ignored
     * 
     * @param method first method to compare
     * @param method2 second method to compare
     * @return if the methods was equals or not.
     */
    public static boolean methodSignatureEquals(Method method, final Method method2) {
        boolean nameEq = method2.getName().equals(method.getName());
        return nameEq && methodParamsEquals(method, method2);
    }

    private static boolean methodParamsEquals(Method method,
            final Method methodsInObject) {
        return Arrays.deepEquals(methodsInObject.getParameterTypes(),
                method.getParameterTypes());
    }

    /**
     * Checks if a method is declared in an Abstract class or interface
     * 
     * @param method to check
     * @return true if its declared in an Abstract class or interface
     */
    public static boolean isMethodDeclaredInInterfaceOrAnAbstractMethod(Method method) {
        return method.getDeclaringClass().isInterface() || Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Checks if a method is an abstract or declared in interface(and not a default implementation)
     *
     * @param method to check
     * @return true if it has a implementation(which might be empty!)
     */
    public static boolean isMethodWithImplementation(Method method) {
        if (method.isDefault()) {
            return true;
        }
        return !method.getDeclaringClass().isInterface() && !Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Check if the provided class is and interface or and abstract class.
     * 
     * @param clazz to check
     * @return true if interface or abstract class
     */
    public static boolean isClassAInterfaceOrAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Check if the method is a toString() hashcode() or equals(o) method,
     * These are the non final methods of Object.class
     * 
     * @param method to check
     * @return true if one of these methods
     */
    public static boolean isToStringOrHashcodeOrEqualsMethod(Method method) {
        try {
            return Util.methodSignatureEquals(method, Object.class.getMethod("toString"))
                    || Util.methodSignatureEquals(method, Object.class.getMethod("hashCode"))
                    || Util.methodSignatureEquals(method, Object.class.getMethod("equals", Object.class));
        } catch (SecurityException | NoSuchMethodException e) {
            throw ProxyException.returnThisShouldNeverHappen(e);
        }
    }

    /**
     * Utility method to help convert a String to an actual object of other common types.
     * Like all primitives type(and object variants) Enums,Files and some simple Collections support
     * to convert comma separated string into List of one of the supported simple types.
     * 
     * @param <T> target object type
     * @param stringToCast the string that you want to convert to another type.
     * @param classToCastTo resulting Clas of the object you want to convert the String to
     * @param invocation only needed for Collection types.
     * @return actual object with the provided type.
     */
    public static <T> T castStringValueToObject(String stringToCast, Class<T> classToCastTo, Invocation invocation) {
        if (invocation == null) {
            return castStringValueToMethodReturnType(stringToCast, classToCastTo, null);
        } else {
            return castStringValueToMethodReturnType(stringToCast, classToCastTo, invocation.getMethod());
        }
    }

    /**
     * Utility method to help convert a String to an actual object of other common types.
     * Like all primitives type(and object variants) Enums, Files.
     * 
     * @param <T> target object type
     * @param stringToCast the string that you want to convert to another type.
     * @param classToCastTo resulting Clas of the object you want to convert the String to
     * @return actual object with the provided type.
     */
    public static <T> T castStringValueToObject(String stringToCast, Class<T> classToCastTo) {
        return castStringValueToMethodReturnType(stringToCast, classToCastTo, null);
    }

    /**
     * Utility method to help convert a String to an actual object of other common types.
     * Like all primitives type(and object variants) Enums, Files and some simple Collections
     * support
     * to convert comma separated string into List of one of the supported simple types.
     * 
     * @param <T> target object type
     * @param stringToCast the string that you want to convert to another type.
     * @param classToCastTo resulting Clas of the object you want to convert the String to
     * @param method only needed for Collection types.
     * @return actual object with the provided type.
     */
    @SuppressWarnings("squid:S3776")
    public static <T> T castStringValueToMethodReturnType(String stringToCast, Class<T> classToCastTo, Method method) {
        if (classToCastTo.equals(String.class)) {
            return (T) stringToCast;
        } else if (classToCastTo.equals(int.class) || classToCastTo.equals(Integer.class)) {
            return (T) new Integer(stringToCast);
        } else if (classToCastTo.equals(boolean.class) || classToCastTo.equals(Boolean.class)) {
            return (T) new Boolean(stringToCast);
        } else if (classToCastTo.equals(float.class) || classToCastTo.equals(Float.class)) {
            return (T) new Float(stringToCast);
        } else if (classToCastTo.equals(double.class) || classToCastTo.equals(Double.class)) {
            return (T) new Double(stringToCast);
        } else if (classToCastTo.equals(byte.class) || classToCastTo.equals(Byte.class)) {
            return (T) new Byte(stringToCast);
        } else if (classToCastTo.equals(short.class) || classToCastTo.equals(Short.class)) {
            return (T) new Short(stringToCast);
        } else if (classToCastTo.equals(long.class) || classToCastTo.equals(Long.class)) {
            return (T) new Long(stringToCast);
        } else if (classToCastTo.equals(char.class) || classToCastTo.equals(Character.class)) {
            if (stringToCast.length() == 1) {
                return (T) Character.valueOf(stringToCast.charAt(0));
            } else {
                throw new IllegalArgumentException(
                        "A character should be a string of size 1. and not: '" + stringToCast + "'");
            }
        } else if (classToCastTo.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>) classToCastTo.asSubclass(Enum.class), stringToCast.trim());
        } else if (classToCastTo.equals(Class.class)) {
            try {
                return (T) Thread.currentThread().getContextClassLoader().loadClass(stringToCast);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Classname: '" + stringToCast + "' does not seem to exist", e);
            }
        } else if (classToCastTo.equals(File.class)) {
            return (T) new File(stringToCast);
        } else if (classToCastTo.equals(List.class)
                || classToCastTo.equals(Collection.class)
                || classToCastTo.equals(Iterable.class)) {
            return (T) castStringListToObjectList(stringToCast, method);
        } else if (classToCastTo.equals(Map.class)) {
            return (T) castStringMapToObjectMap(stringToCast, method);
        } else if (classToCastTo.equals(Set.class)) {
            return (T) castStringListToObjectSet(stringToCast, method);
        } else if (classToCastTo.isArray()) {
            return (T) castStringToArray(stringToCast, method, classToCastTo.getComponentType());
        } else {
            //TODO handle constructors that takes String by default with reflection. and common fromString method factories?
            throw new UnsupportedOperationException(
                    "Received type that is not supported! Type: " + classToCastTo.getSimpleName());
        }
    }

    /**
     * Will default null values for collections types (and arrays) to an empty variant of that type.
     *
     * @param <T> target object type
     * @param collectionClass the collection class
     * @param value the collection which will be unchanged and this method will only return a empty
     *        collection if the this given value is null.
     * @return original value or empty collections instead of null
     */
    public static <T> T defaultToEmptyCollectionsOnNullValue(Class<T> collectionClass, T value) {
        if (value != null) {
            return value;
        } else if (collectionClass.equals(List.class)) {
            return (T) Collections.emptyList();
        } else if (collectionClass.equals(Map.class)) {
            return (T) Collections.emptyMap();
        } else if (collectionClass.equals(Set.class)) {
            return (T) Collections.emptySet();
        } else if (collectionClass.equals(Properties.class)) {
            return (T) new Properties();
        } else if (collectionClass.isArray()) {
            return (T) Array.newInstance(collectionClass.getComponentType(), 0);
        } else if (Collection.class.isAssignableFrom(collectionClass)) {
            return (T) Collections.emptyList();
        }
        return value;
    }

    private static List<Object> castStringListToObjectList(String commaSeparatedList, Method method) {
        List<Object> returnList = new ArrayList<>();
        if (commaSeparatedList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> valueList = Arrays.asList(commaSeparatedList.split(","));
        for (String singleValueString : valueList) {
            Class<?> classToCastTo = String.class;
            if (method != null) {
                classToCastTo = getGenericTypeOfReturnType(method);
            }
            returnList.add(castStringValueToMethodReturnType(singleValueString, classToCastTo, method));
        }
        return returnList;
    }

    private static Set<Object> castStringListToObjectSet(String commaSeparatedList, Method method) {
        Set<Object> returnSet = new LinkedHashSet<>();
        if (commaSeparatedList.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> valueList = Arrays.asList(commaSeparatedList.split(","));
        for (String singleValueString : valueList) {
            Class<?> classToCastTo = String.class;
            if (method != null) {
                classToCastTo = getGenericTypeOfReturnType(method);
            }
            returnSet.add(castStringValueToMethodReturnType(singleValueString, classToCastTo, method));
        }
        return returnSet;
    }

    private static Map<Object, Object> castStringMapToObjectMap(String commaSeparatedMap, Method method) {
        Map<Object, Object> returnMap = new TreeMap<>();
        if (commaSeparatedMap.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> entryList = Arrays.asList(commaSeparatedMap.split(","));
        for (String keyAndValue : entryList) {
            String[] split = keyAndValue.split("=");
            String key = split[0];
            String value = split[1];

            Class<?> genericTypeOfKey = String.class;
            Class<?> genericTypeOfValue = String.class;
            if (method != null) {
                genericTypeOfKey = getGenericTypeOfReturnType(method);
                genericTypeOfValue = getSecondGenericTypeOfReturnType(method);
            }

            Object keyAsObject = castStringValueToMethodReturnType(key,
                    genericTypeOfKey,
                    method);
            Object valueAsObject = castStringValueToMethodReturnType(value,
                    genericTypeOfValue,
                    method);
            returnMap.put(keyAsObject, valueAsObject);
        }
        return returnMap;
    }

    private static <T> T[] castStringToArray(String commaSeparatedArray, Method method, Class<T> arrayType) {
        List<Object> returnList = new ArrayList<Object>();
        if (commaSeparatedArray.isEmpty()) {
            return (T[]) Array.newInstance(arrayType, 0);
        }
        List<String> valueList = Arrays.asList(commaSeparatedArray.split(","));
        for (String singleValueString : valueList) {
            returnList.add(castStringValueToMethodReturnType(singleValueString,
                    arrayType,
                    method));
        }
        return returnList.toArray((T[]) Array.newInstance(arrayType, returnList.size()));
    }

    /**
     * Get the generic return type of the provided method,
     * Meaning the String.class from a method like  {@code public List<String> method() }
     * 
     * and String.class from a method like  {@code public Map<String,Integer> method() }
     * 
     * @param method to fetch return type from
     * @return generic type of the return type
     */
    public static Class<?> getGenericTypeOfReturnType(Method method) {
        Type[] actualTypeArguments = ((ParameterizedType) method.getGenericReturnType())
                .getActualTypeArguments();

        Type type = actualTypeArguments[0];
        return getGenericTypeOfReturnType(method, type);
    }

    /**
     * Get the second generic return type of the provided method,
     * Meaning the Integer.class from a method like: {@code public Map<String,Integer> method()}
     * 
     * @param method to fetch return type from
     * @return second generic type of the return type
     */
    public static Class<?> getSecondGenericTypeOfReturnType(Method method) {
        Type[] actualTypeArguments = ((ParameterizedType) method.getGenericReturnType())
                .getActualTypeArguments();
        Type type = actualTypeArguments[1];
        return getGenericTypeOfReturnType(method, type);
    }

    private static Class<?> getGenericTypeOfReturnType(Method method, Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof WildcardType) {
            WildcardType wildType = (WildcardType) type;
            return (Class<?>) wildType.getUpperBounds()[0];
        } else if (type instanceof ParameterizedType) {
            return ((Class) ((ParameterizedType) type).getRawType());
        }
        throw new IllegalArgumentException(
                "Was not able to figure out the generic type of method: " + method.getName());
    }

    /**
     * Find the first found method that is matching the given methodSignature.
     * notice the methodSignature does not have be same Method object, So declaredClass and
     * returntype is not part of signature matching.
     * 
     * @param methodSignature a method signature that you want to find and invoke,
     * @param classesToLookIn classes to look for the method in.
     * @return the first found method, or null if none was found
     */
    public static Method findMethodWithSignatureInClass(Method methodSignature,
            Class<?>... classesToLookIn) {
        for (Class<?> classToLookIn : classesToLookIn) {
            for (final Method methodInObject : classToLookIn
                    .getMethods()) {
                if (methodSignatureEquals(methodSignature, methodInObject)) {
                    return methodInObject;
                }
            }
            for (final Method methodInObject : classToLookIn
                    .getDeclaredMethods()) {
                if (methodSignatureEquals(methodSignature, methodInObject)) {
                    return methodInObject;
                }
            }
        }
        return null;
    }

    static Method findMethodWithSignature(Method method,
            Object objectToLookIn) {
        return findMethodWithSignatureInClass(method, objectToLookIn.getClass());
    }

    static boolean isClassSafeFromPublicVariableProblems(Class<?> clazz) {
        List<Field> fieldList = getAllFields(clazz);
        for (Field f : fieldList) {
            if ("_filter_signature".equals(f.getName())) {
                continue;
            }
            int modifiers = f.getModifiers();
            if (isPublicAndNotStaticFinal(modifiers)) {
                return false;
            }
        }
        return true;
    }

    static boolean isPublicAndNotStaticFinal(int modifiers) {
        return Modifier.isPublic(modifiers) && !(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }

    static boolean isStaticNonFinal(int modifiers) {
        return Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers);
    }

    static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            fieldList.addAll(getAllFields(superClass));
        }
        return fieldList;
    }

    static <T> boolean isNewProxyNeeded(final T objectToIntercept, Class<?>... interfaces) {
        return !(objectToIntercept instanceof InterceptableProxy)
                || !isObjectImplementingAllInterfaces(objectToIntercept, interfaces);
    }

    /**
     * Concatenate two arrays into one.
     * The passed in arrays will be unaltered. They will only be used in construction of a new array
     * 
     * @param firstArray the first array
     * @param varargsArrays the other arrays to concat to one large array
     * @return a new merged array
     */
    static <T> T[] concatArrays(final T[] firstArray, final T[]... varargsArrays) {
        int totalLength = firstArray.length;
        for (final T[] array : varargsArrays) {
            totalLength += array.length;
        }
        final T[] result = Arrays.copyOf(firstArray, totalLength);
        int offset = firstArray.length;
        for (final T[] array : varargsArrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    static void cleanMethodNameField(Method met) throws NoSuchFieldException,
            IllegalAccessException {
        if (met.getName().startsWith("_")) {
            final Class<?> secretClass = met.getClass();
            final Field namefield = secretClass.getDeclaredField("name");
            namefield.setAccessible(true);
            namefield.set(met, met.getName().replaceFirst("_.\\d*", ""));
        }
    }

    static boolean isMethodExistingInClass(Method method, Class<?> class1) {
        return findMethodWithSignatureInClass(method, class1) != null;
    }

    static void filterExceptionAndRethrowCorrect(
            InvocationTargetException e) throws Throwable {
        Throwable throwable = e;
        while (throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        }
        throw throwable;
    }

    @SuppressWarnings({ "squid:S1181", "squid:S1166" })
    static Object tryToAddInterceptorToObject(Object object, Interceptor interceptor,
            Invocation invocation,
            boolean shouldInterceptPrivateMethods) {

        if (object == null) {
            return null;
        }
        if (!Util.isClassSafeFromPublicVariableProblems(object.getClass())) {
            LOG.debug(
                    "Class: {} is not safe to add an interceptor {} recursivly to. Will therefore not add the recursive interceptor",
                    object.getClass().getSimpleName(),
                    interceptor.getClass().getSimpleName());
            return object;
        }
        if (!shouldInterceptPrivateMethods && Modifier.isPrivate(invocation.getMethod().getModifiers())) {
            return object;
        }
        if (Util.doesObjectHaveInterceptor(object, interceptor)) {
            return object;
        }
        try {
            return Proxy.intercept(object, interceptor);

        } catch (Throwable t) {
            LOG.trace("Failed to add interceptor: {} to the return object of type: {} returned by method: {}",
                    interceptor.getClass().getSimpleName(),
                    invocation.getMethod().getReturnType().getSimpleName(),
                    invocation.getMethodName());
            return object;
        }
    }

    static boolean doesObjectHaveInterceptor(Object object, Interceptor inter) {
        if (!(object instanceof InterceptableProxy)) {
            return false;
        }
        return Proxy.getProxyInterface(object).getInterceptorList().contains(inter);
    }

}
