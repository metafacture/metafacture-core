/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.morph.functions;

import java.io.FileNotFoundException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.exceptions.MorphException;
import org.culturegraph.mf.util.ResourceUtil;



/**
 * @author Markus Michael Geipel
 */
public final class Script extends AbstractSimpleStatelessFunction {

	//private static final Logger LOG = LoggerFactory.getLogger(Script.class);
	
	private Invocable invocable;
	private String invoke;
	
	public void setInvoke(final String invoke){
		this.invoke = invoke;
	}

	public void setFile(final String file) {

		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine engine = manager.getEngineByName("JavaScript");
		try {
			//LOG.info("loading code from '" + file + "'");
			engine.eval(ResourceUtil.getReader(file));
		} catch (ScriptException e) {
			throw new MorphDefException("Error in script", e);
		} catch (FileNotFoundException e) {
			throw new MorphDefException("Error loading script '" + file + "'", e);
		}
		invocable = (Invocable) engine;
	}
	
	@Override
	public String process(final String value) {
		final Object obj;
		try {
			//LOG.info("processing: " + value);
			obj = invocable.invokeFunction(invoke, value);
			//LOG.info("returning: " + obj);
			return obj.toString();
		} catch (ScriptException e) {
			throw new MorphException("Error in script while evaluating 'process' method", e);
		} catch (NoSuchMethodException e) {
			throw new MorphException("'process' method is missing in script", e);
		}
	}
}
