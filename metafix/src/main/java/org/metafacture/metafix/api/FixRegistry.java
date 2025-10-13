/*
 * Copyright 2025 hbz NRW
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metafacture.metafix.api;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.commons.reflection.ReflectionUtil;
import org.metafacture.framework.MetafactureException;
import org.metafacture.metafix.FixCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class FixRegistry {

    private static final String FILE_SEPARATOR = "/";
    private static final String PACKAGE_SEPARATOR = ".";

    private static final String CLASS_FILE_EXTENSION = ".class";

    private static final String PROPERTIES_LOCATION = "fix-commands.properties";

    private static final Map<String, Map<String, Class<?>>> CACHE = new ConcurrentHashMap<>();

    private final Registry<FixContext> binds = new Registry<>(FixContext.class, "bind");
    private final Registry<FixFunction> methods = new Registry<>(FixFunction.class, "method");
    private final Registry<FixPredicate> conditionals = new Registry<>(FixPredicate.class, "conditional");

    private final Set<Registry<?>> registries = Set.of(methods, conditionals, binds);

    private final ClassLoader loader = ReflectionUtil.getContextClassLoader();

    /**
     * Creates an instance of {@link FixRegistry}. {@link #registerProperties(String) Registers}
     * all Fix commands found in any {@value PROPERTIES_LOCATION} files.
     */
    public FixRegistry() {
        registerProperties(PROPERTIES_LOCATION);
    }

    private void registerCommands(final String key, final Function<String, Map<String, Class<?>>> function) {
        loadCommands(key, function).forEach(this::registerCommand);
    }

    private Map<String, Class<?>> loadCommands(final String key, final Function<String, Map<String, Class<?>>> function) {
        return CACHE.computeIfAbsent(key, function);
    }

    private void getResources(final String name, final Consumer<URL> consumer) {
        try {
            loader.getResources(name).asIterator().forEachRemaining(consumer);
        }
        catch (final IOException e) {
            throw new MetafactureException("Unable to load resources: " + name, e);
        }
    }

    /**
     * Registers Fix commands from the given properties file. If the property
     * value is empty, {@link #registerPackage(String) registers} the package
     * given in the property key, otherwise registers the class given in the
     * property value under the Fix command name given in the property key.
     *
     * @param propertiesName the properties file
     */
    public void registerProperties(final String propertiesName) {
        registerCommands(propertiesName, this::loadProperties);
    }

    private Map<String, Class<?>> loadProperties(final String propertiesName) {
        final Map<String, Class<?>> map = new HashMap<>();

        getResources(propertiesName, u -> {
            try {
                ResourceUtil.loadProperties(u).forEach((k, v) -> {
                    final String key = k.toString();
                    final String value = v.toString();

                    if (value.isEmpty()) {
                        map.putAll(loadCommands(key, this::loadPackage));
                    }
                    else {
                        try {
                            map.put(key, loader.loadClass(value));
                        }
                        catch (final ClassNotFoundException e) {
                            throw new MetafactureException("Class not found: " + value, e);
                        }
                    }
                });
            }
            catch (final IOException e) {
                throw new MetafactureException("Unable to load properties: " + u, e);
            }
        });

        return map;
    }

    /**
     * Registers Fix commands from the given package name. Only classes
     * annotated with the {@link FixCommand} annotation are considered.
     *
     * @param packageName the package name
     */
    public void registerPackage(final String packageName) {
        registerCommands(packageName, this::loadPackage);
    }

    private Map<String, Class<?>> loadPackage(final String packageName) {
        final Map<String, Class<?>> map = new HashMap<>();

        final String directoryName = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);

        getResources(directoryName, u -> {
            try {
                final URLConnection connection = u.openConnection();

                if (connection instanceof JarURLConnection) {
                    final JarURLConnection jarConnection = (JarURLConnection) connection;

                    try (JarFile jarFile = jarConnection.getJarFile()) {
                        loadPackage(map, jarFile.stream().map(JarEntry::getName)
                                .filter(f -> {
                                    final int index = f.lastIndexOf(FILE_SEPARATOR);
                                    return index > 0 && directoryName.equals(f.substring(0, index));
                                })
                                .map(f -> f.replace(FILE_SEPARATOR, PACKAGE_SEPARATOR)));
                    }
                }
                else {
                    try (
                        InputStream stream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream))
                    ) {
                        loadPackage(map, reader.lines().map(l -> packageName + PACKAGE_SEPARATOR + l));
                    }
                }
            }
            catch (final IOException e) {
                throw new MetafactureException("Unable to load package: " + u, e);
            }
        });

        return map;
    }

    private void loadPackage(final Map<String, Class<?>> map, final Stream<String> stream) {
        stream.filter(f -> f.endsWith(CLASS_FILE_EXTENSION)).forEach(f -> {
            final String className = f.substring(0, f.lastIndexOf(CLASS_FILE_EXTENSION));

            try {
                final Class<?> clazz = loader.loadClass(className);
                final String name = getCommandName(clazz);

                if (name != null) {
                    map.put(name, clazz);
                }
            }
            catch (final ClassNotFoundException e) {
                throw new MetafactureException("Class not found: " + className, e);
            }
        });
    }

    private String getCommandName(final Class<?> clazz) {
        final FixCommand annotation = clazz.getAnnotation(FixCommand.class);
        return annotation != null ? Objects.requireNonNull(annotation.value()) : null;
    }

    /**
     * Registers the given class as Fix command under the Fix command name given
     * by the {@link FixCommand} annotation.
     *
     * @param clazz the class
     *
     * @return the Fix command name
     *
     * @throws IllegalArgumentException if the class is not annotated with the
     *                                  {@link FixCommand} annotation
     */
    public String registerCommand(final Class<?> clazz) {
        final String name = getCommandName(clazz);

        if (name != null) {
            return registerCommand(name, clazz) ? name : null;
        }
        else {
            throw new IllegalArgumentException("Fix command annotation missing: " + clazz);
        }
    }

    /**
     * Registers the given class as Fix command under the given Fix command name.
     *
     * @param <T> the Fix command type
     * @param name the Fix command name
     * @param clazz the class
     *
     * @return false if the class has already been registered under the name
     *
     * @throws IllegalArgumentException if a different class has already been
     *                                  registered under the name
     *
     * @throws IllegalArgumentException if the Fix command type is not supported
     */
    public <T> boolean registerCommand(final String name, final Class<? extends T> clazz) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(clazz);

        for (final Registry<?> registry : registries) {
            final Class<?> currentClass = registry.get(name);
            if (currentClass != null) {
                if (currentClass.equals(clazz)) {
                    return false;
                }

                throw new IllegalArgumentException("Fix " + registry.getTypeName() + " already registered: " + name);
            }
        }

        for (final Registry<?> registry : registries) {
            if (registry.getTypeClass().isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked") // protected by isAssignableFrom check
                final Registry<T> castedRegistry = (Registry<T>) registry;

                castedRegistry.put(name, clazz);
                return true;
            }
        }

        throw new IllegalArgumentException("Unsupported Fix command type: " + clazz);
    }

    /**
     * Checks whether a Fix command is registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return true if a Fix command is registered under the given name
     */
    public boolean isRegisteredCommand(final String name) {
        return registries.stream().anyMatch(r -> r.containsKey(name));
    }

    private <T> T getCommand(final Registry<T> registry, final String name) {
        final Class<? extends T> clazz = registry.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("Unsupported Fix " + registry.getTypeName() + ": " + name);
        }

        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (final ReflectiveOperationException e) {
            throw new MetafactureException(e);
        }
    }

    /**
     * Returns the Fix bind registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the Fix bind
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered bind
     */
    public FixContext getBind(final String name) {
        return getCommand(binds, name);
    }

    /**
     * Returns the Fix conditional registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the Fix conditional
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered conditional
     */
    public FixPredicate getConditional(final String name) {
        return getCommand(conditionals, name);
    }

    /**
     * Returns the Fix method registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the Fix method
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered method
     */
    public FixFunction getMethod(final String name) {
        return getCommand(methods, name);
    }

    private <T> Class<? extends T> unregisterCommand(final Registry<T> registry, final String name) {
        final Class<? extends T> clazz = registry.remove(name);
        if (clazz == null) {
            throw new IllegalArgumentException("Fix " + registry.getTypeName() + " not registered: " + name);
        }

        return clazz;
    }

    /**
     * Unregisters the Fix bind registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the previously registered Fix bind
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered bind
     */
    public Class<? extends FixContext> unregisterBind(final String name) {
        return unregisterCommand(binds, name);
    }

    /**
     * Unregisters the Fix conditional registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the previously registered Fix conditional
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered conditional
     */
    public Class<? extends FixPredicate> unregisterConditional(final String name) {
        return unregisterCommand(conditionals, name);
    }

    /**
     * Unregisters the Fix method registered under the given name.
     *
     * @param name the Fix command name
     *
     * @return the previously registered Fix method
     *
     * @throws IllegalArgumentException if the Fix command name is not a
     *                                  registered method
     */
    public Class<? extends FixFunction> unregisterMethod(final String name) {
        return unregisterCommand(methods, name);
    }

    private static class Registry<T> {

        private final Map<String, Class<? extends T>> registry = new HashMap<>();
        private final Class<T> typeClass;
        private final String typeName;

        private Registry(final Class<T> typeClass, final String typeName) {
            this.typeClass = typeClass;
            this.typeName = typeName;
        }

        private Class<T> getTypeClass() {
            return typeClass;
        }

        private String getTypeName() {
            return typeName;
        }

        private void put(final String name, final Class<? extends T> clazz) {
            registry.put(name, clazz);
        }

        private boolean containsKey(final String name) {
            return registry.containsKey(name);
        }

        private Class<? extends T> get(final String name) {
            return registry.get(name);
        }

        private Class<? extends T> remove(final String name) {
            return registry.remove(name);
        }

    }

}
