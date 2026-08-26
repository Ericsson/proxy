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

import java.util.EnumMap;
import java.util.Map;

import com.ericsson.commonlibrary.proxy.ProxyConfiguration.Engine;

/**
 * Lazily creates and caches one {@link ProxyEngineFactory} per {@link Engine}. Engines are only instantiated when they
 * are actually used, so an application that never selects the byte-buddy engine never touches byte-buddy classes.
 */
final class ProxyEngineProvider {

    private static final Map<Engine, ProxyEngineFactory> FACTORIES = new EnumMap<>(Engine.class);

    private ProxyEngineProvider() {
        // hidden
    }

    /**
     * @return the factory of the engine that {@link ProxyConfiguration} currently selects.
     */
    static ProxyEngineFactory getCurrentFactory() {
        return getFactory(ProxyConfiguration.getEngine());
    }

    static ProxyEngineFactory getFactory(Engine engine) {
        synchronized (FACTORIES) {
            ProxyEngineFactory factory = FACTORIES.get(engine);
            if (factory == null) {
                factory = createFactory(engine);
                FACTORIES.put(engine, factory);
            }
            return factory;
        }
    }

    private static ProxyEngineFactory createFactory(Engine engine) {
        try {
            switch (engine) {
            case JAVASSIST:
                return new JavassistProxyEngineFactory();
            case BYTEBUDDY:
                return new ByteBuddyProxyEngineFactory();
            default:
                throw new ProxyException("Unsupported proxy engine: " + engine);
            }
        } catch (LinkageError e) {
            throw new ProxyException("The " + engine.getName()
                    + " proxy engine is not available on the classpath. Add the missing dependency or select another engine with -D"
                    + ProxyConfiguration.ENGINE_PROPERTY, e);
        }
    }
}
