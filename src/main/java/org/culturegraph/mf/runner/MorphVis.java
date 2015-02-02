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
package org.culturegraph.mf.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.culturegraph.mf.morph.MorphVisualizer;
import org.xml.sax.InputSource;


/**
 * Generates a graphviz dot file based on a Metamorph definition.
 *
 * @author Markus Michael Geipel
 */
public final class MorphVis {

	private static final String ENCODING = "UTF8";

	private MorphVis() {/* no instances */
	}

	/**
	 * @param args
	 *
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final Writer out;
		if(args.length==1){
			out = new OutputStreamWriter(System.out, ENCODING);
		}else if(args.length==2){
			System.out.println("Writing to " + args[1]);
			out = new  OutputStreamWriter(new FileOutputStream(new File(args[1])), ENCODING);
		}else{
			System.err.println("Usage: Visualize MORPHDEF [DOTFILE]");
			return;
		}
		final MorphVisualizer visualizer = new MorphVisualizer(out);
		visualizer.walk(new InputSource(new FileReader(args[0])));
	}


}
