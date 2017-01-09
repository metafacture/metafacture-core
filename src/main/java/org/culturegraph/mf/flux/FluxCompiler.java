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
package org.culturegraph.mf.flux;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.culturegraph.mf.flux.parser.FlowBuilder;
import org.culturegraph.mf.flux.parser.FluxLexer;
import org.culturegraph.mf.flux.parser.FluxParser;
import org.culturegraph.mf.flux.parser.FluxProgramm;

/**
 * Creates a flow based on a flux script.
 *
 * @author Markus Michael Geipel
 */
public final class FluxCompiler {
	private FluxCompiler() {
		// no instances
	}

	public static FluxProgramm compile(final InputStream flux, final Map<String,String> vars ) throws RecognitionException, IOException{
		return compileFlow(compileAst(flux), vars);
	}

	private static CommonTreeNodeStream compileAst(final InputStream flowDef) throws IOException, RecognitionException {
		final FluxParser parser = new FluxParser(new CommonTokenStream(new FluxLexer(new ANTLRInputStream(flowDef))));
		return new CommonTreeNodeStream(parser.flux().getTree());
	}

	private static FluxProgramm compileFlow(final CommonTreeNodeStream treeNodes, final Map<String, String> vars)
			throws RecognitionException {
		final FlowBuilder flowBuilder = new FlowBuilder(treeNodes);
		flowBuilder.addVaribleAssignements(vars);
		return flowBuilder.flux();
	}
}
