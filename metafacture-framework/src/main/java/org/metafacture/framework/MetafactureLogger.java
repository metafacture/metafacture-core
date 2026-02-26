/*
 * Copyright 2026 hbz NRW
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

package org.metafacture.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a logging facility for both internal/developer-oriented and
 * external/user-oriented messages.
 *
 * @author Jens Wille
 */
public class MetafactureLogger {

    private static final String EXTERNAL_PREFIX = "external.";

    private final Logger externalLogger;
    private final Logger internalLogger;

    /**
     * Creates an instance of {@link MetafactureLogger} with the given class.
     *
     * @param clazz the class
     */
    public MetafactureLogger(final Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Creates an instance of {@link MetafactureLogger} with the given name.
     *
     * @param name the name
     */
    public MetafactureLogger(final String name) {
        internalLogger = LoggerFactory.getLogger(name);
        externalLogger = LoggerFactory.getLogger(EXTERNAL_PREFIX + name);
    }

    /**
     * Returns the <i>internal</i> logger instance.
     *
     * @return the logger
     */
    public Logger getInternalLogger() {
        return internalLogger;
    }

    /**
     * Returns the <i>external</i> logger instance.
     *
     * @return the logger
     */
    public Logger getExternalLogger() {
        return externalLogger;
    }

    /**
     * Logs an <i>internal</i> message at the ERROR level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void error(final String format, final Object... arguments) {
        internalLogger.error(format, arguments);
    }

    /**
     * Logs an <i>external</i> message at the ERROR level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void externalError(final String format, final Object... arguments) {
        externalLogger.error(format, arguments);
    }

    /**
     * Logs an <i>internal and external</i> message at the ERROR level according
     * to the specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void combinedError(final String format, final Object... arguments) {
        error(format, arguments);
        externalError(format, arguments);
    }

    /**
     * Logs an <i>internal</i> message at the WARN level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void warn(final String format, final Object... arguments) {
        internalLogger.warn(format, arguments);
    }

    /**
     * Logs an <i>external</i> message at the WARN level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void externalWarn(final String format, final Object... arguments) {
        externalLogger.warn(format, arguments);
    }

    /**
     * Logs an <i>internal and external</i> message at the WARN level according
     * to the specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void combinedWarn(final String format, final Object... arguments) {
        warn(format, arguments);
        externalWarn(format, arguments);
    }

    /**
     * Logs an <i>internal</i> message at the INFO level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void info(final String format, final Object... arguments) {
        internalLogger.info(format, arguments);
    }

    /**
     * Logs an <i>external</i> message at the INFO level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void externalInfo(final String format, final Object... arguments) {
        externalLogger.info(format, arguments);
    }

    /**
     * Logs an <i>internal and external</i> message at the INFO level according
     * to the specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void combinedInfo(final String format, final Object... arguments) {
        info(format, arguments);
        externalInfo(format, arguments);
    }

    /**
     * Logs an <i>internal</i> message at the DEBUG level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void debug(final String format, final Object... arguments) {
        internalLogger.debug(format, arguments);
    }

    /**
     * Logs an <i>external</i> message at the DEBUG level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void externalDebug(final String format, final Object... arguments) {
        externalLogger.debug(format, arguments);
    }

    /**
     * Logs an <i>internal and external</i> message at the DEBUG level according
     * to the specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void combinedDebug(final String format, final Object... arguments) {
        debug(format, arguments);
        externalDebug(format, arguments);
    }

    /**
     * Logs an <i>internal</i> message at the TRACE level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void trace(final String format, final Object... arguments) {
        internalLogger.trace(format, arguments);
    }

    /**
     * Logs an <i>external</i> message at the TRACE level according to the
     * specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void externalTrace(final String format, final Object... arguments) {
        externalLogger.trace(format, arguments);
    }

    /**
     * Logs an <i>internal and external</i> message at the TRACE level according
     * to the specified format and arguments.
     *
     * @param format the format string
     * @param arguments a list of arguments
     */
    public void combinedTrace(final String format, final Object... arguments) {
        trace(format, arguments);
        externalTrace(format, arguments);
    }

}
