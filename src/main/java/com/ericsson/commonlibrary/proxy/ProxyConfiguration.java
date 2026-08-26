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

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selects which bytecode manipulation engine the proxy library uses. Two engines are supported and both implement the
 * exact same behavior, so switching engine never requires any change of the code that uses {@link Proxy}.
 * <p>
 * The engine can be selected in three ways, in falling order of precedence:
 * <ol>
 * <li>Per thread, with {@link #setThreadEngine(Engine)} or {@link #callWithEngine(Engine, Callable)}.</li>
 * <li>Globally at runtime, with {@link #setEngine(Engine)}.</li>
 * <li>At JVM startup, with the system property {@code -Dproxy.engine=javassist|bytebuddy}.</li>
 * </ol>
 * When nothing is configured, {@link Engine#JAVASSIST} is used.
 * <p>
 * Already created proxies are unaffected by a later engine change, only proxies created afterwards use the new engine.
 * Proxies from both engines can safely be mixed in the same JVM, and a proxy from one engine can be proxied again by
 * the other one.
 */
public final class ProxyConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyConfiguration.class);

    /**
     * Name of the system property that selects the engine at JVM startup.
     */
    public static final String ENGINE_PROPERTY = "proxy.engine";

    /**
     * The bytecode manipulation engines that the proxy library supports.
     */
    public enum Engine {

        /**
         * <a href="http://jboss-javassist.github.io/javassist/">Javassist</a> based engine. This is the default and the
         * engine the library has been using since its first release.
         */
        JAVASSIST("javassist"),

        /**
         * <a href="https://bytebuddy.net/">Byte Buddy</a> based engine.
         */
        BYTEBUDDY("bytebuddy");

        private final String name;

        Engine(String name) {
            this.name = name;
        }

        /**
         * @return the lower case name of the engine, as used by the {@value ProxyConfiguration#ENGINE_PROPERTY} system
         *         property
         */
        public String getName() {
            return name;
        }

        /**
         * Resolves an {@link Engine} from its name, ignoring case and surrounding whitespace.
         *
         * @param name
         *            the engine name, {@code null} resolves to the default engine
         *
         * @return the matching engine, or the default engine when the name is unknown
         */
        public static Engine fromName(String name) {
            if (name == null || name.trim().isEmpty()) {
                return DEFAULT_ENGINE;
            }
            for (Engine engine : values()) {
                if (engine.name.equalsIgnoreCase(name.trim())) {
                    return engine;
                }
            }
            LOG.warn("Unknown proxy engine '{}', falling back to '{}'", name, DEFAULT_ENGINE.getName());
            return DEFAULT_ENGINE;
        }
    }

    private static final Engine DEFAULT_ENGINE = Engine.JAVASSIST;

    private static volatile Engine globalEngine = Engine.fromName(System.getProperty(ENGINE_PROPERTY));

    private static final ThreadLocal<Engine> THREAD_ENGINE = new ThreadLocal<>();

    private ProxyConfiguration() {
        // hidden
    }

    /**
     * @return the engine that will be used for the next proxy created by this thread.
     */
    public static Engine getEngine() {
        Engine threadEngine = THREAD_ENGINE.get();
        return threadEngine != null ? threadEngine : globalEngine;
    }

    /**
     * Sets the engine used by all threads that do not have a thread local engine of their own.
     *
     * @param engine
     *            the engine to use, never {@code null}
     */
    public static void setEngine(Engine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine can not be null");
        }
        globalEngine = engine;
        LOG.debug("Global proxy engine changed to: {}", engine.getName());
    }

    /**
     * Sets the engine used by all threads that do not have a thread local engine of their own.
     *
     * @param engineName
     *            name of the engine to use, see {@link Engine#fromName(String)}
     */
    public static void setEngine(String engineName) {
        setEngine(Engine.fromName(engineName));
    }

    /**
     * Sets the engine for the current thread only. Overrides {@link #setEngine(Engine)} for this thread until
     * {@link #clearThreadEngine()} is called.
     *
     * @param engine
     *            the engine to use, never {@code null}
     */
    public static void setThreadEngine(Engine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine can not be null");
        }
        THREAD_ENGINE.set(engine);
    }

    /**
     * Removes the thread local engine of the current thread, so that the global engine is used again.
     */
    public static void clearThreadEngine() {
        THREAD_ENGINE.remove();
    }

    /**
     * Runs the given code with a specific engine on the current thread and restores the previous setting afterwards.
     *
     * @param <T>
     *            the type returned by the callable
     * @param engine
     *            the engine used while the callable runs
     * @param callable
     *            the code to run
     *
     * @return whatever the callable returns
     *
     * @throws Exception
     *             anything the callable throws
     */
    public static <T> T callWithEngine(Engine engine, Callable<T> callable) throws Exception {
        Engine previous = THREAD_ENGINE.get();
        setThreadEngine(engine);
        try {
            return callable.call();
        } finally {
            if (previous == null) {
                THREAD_ENGINE.remove();
            } else {
                THREAD_ENGINE.set(previous);
            }
        }
    }

    /**
     * Resets the configuration back to the {@value #ENGINE_PROPERTY} system property, or to the default engine when
     * that property is not set. Also removes any thread local engine of the current thread.
     */
    public static void reset() {
        THREAD_ENGINE.remove();
        setEngine(Engine.fromName(System.getProperty(ENGINE_PROPERTY)));
    }
}
