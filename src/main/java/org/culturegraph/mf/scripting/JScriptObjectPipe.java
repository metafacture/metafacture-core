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
package org.culturegraph.mf.scripting;

import java.io.FileNotFoundException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

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

	public JScriptObjectPipe(final String script) {
		setScript(script);
	}

	public void setInvoke(final String invoke) {
		this.invoke = invoke;
	}

	private void setScript(final String file) {

		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine engine = manager.getEngineByName("JavaScript");
		try {
			// LOG.info("loading code from '" + file + "'");
			engine.eval(ResourceUtil.getReader(file));
		} catch (ScriptException e) {
			throw new MetafactureException("Error in script", e);
		} catch (FileNotFoundException e) {
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

		} catch (ScriptException e) {
			throw new MetafactureException("Error in script while evaluating 'process' method", e);
		} catch (NoSuchMethodException e) {
			throw new MetafactureException("'process' method is missing in script", e);
		}
	}

}
