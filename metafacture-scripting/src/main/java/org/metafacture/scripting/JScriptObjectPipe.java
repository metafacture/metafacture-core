/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.scripting;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.IOException;
import java.io.Reader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Executes the function process(obj) in a given jscript.
 *
 * @author Markus Geipel
 *
 */
@Description("executes the function process(obj) in a given jscript")
@In(Object.class)
@Out(Object.class)
@FluxCommand("jscript")
public final class JScriptObjectPipe extends DefaultObjectPipe<Object, ObjectReceiver<Object>> {

    private static final String PROCESS = "process";
    private String invoke = PROCESS;
    private Invocable invocable;

    /**
     * Creates an instance of {@link JScriptObjectPipe} by the given script.
     *
     * @param script the script
     */
    public JScriptObjectPipe(final String script) {
        setScript(script);
    }

    /**
     * Sets the name of the procedure or function to call by
     * {@code javax.script.Invocable#invokeFunction(String, Object...)}
     *
     * @param invoke the name of the procedure or function to call
     */
    public void setInvoke(final String invoke) {
        this.invoke = invoke;
    }

    private void setScript(final String file) {

        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("JavaScript");
        try (Reader reader = ResourceUtil.getReader(file)) {
            // LOG.info("loading code from '" + file + "'");
            engine.eval(reader);
        }
        catch (final ScriptException e) {
            throw new MetafactureException("Error in script", e);
        }
        catch (final IOException e) {
            throw new MetafactureException("Error loading script '" + file + "'", e);
        }
        invocable = (Invocable) engine;
    }

    @Override
    public void process(final Object obj) {
        assert !isClosed();
        try {

            // LOG.info("processing: " + value);
            final Object retObj = invocable.invokeFunction(invoke, obj);
            // LOG.info("returning: " + obj);

            getReceiver().process(retObj);

        }
        catch (final ScriptException e) {
            throw new MetafactureException("Error in script while evaluating 'process' method", e);
        }
        catch (final NoSuchMethodException e) {
            throw new MetafactureException("'process' method is missing in script", e);
        }
    }

}
