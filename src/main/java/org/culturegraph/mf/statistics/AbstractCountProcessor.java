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
package org.culturegraph.mf.statistics;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.objects.Triple;

/**
 * Base class for operating on count data. The expected inputs are triples
 * containing as subject the variable name and as object the count. Marginal
 * counts must appear first, joint counts second. Marinal counts must be written
 * as 1:VARNAME, Joint counts as 2:FIRSTVARNAME&amp;SECONDVARNAME.
 *
 * @author Markus Michael Geipel
 */
public abstract class AbstractCountProcessor extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {


	private static final Pattern KEY_SPLIT_PATTERN = Pattern.compile("&", Pattern.LITERAL);

	private static final String MARGINAL_PREFIX = "1:";
	private static final String JOINT_PREFIX = "2:";

	private final Map<String, Integer> marginals = new Hashtable<String, Integer>();
	private boolean inHeader = true;
	private int minCount;

	protected final int getTotal() {
		return getMarginal("");
	}

	protected final void setMinCount(final int min) {
		minCount = min;
	}

	@Override
	public final void process(final Triple triple) {
		if (triple.getSubject().indexOf('&') == -1) {
			if (!inHeader) {
				throw new IllegalArgumentException(
						"Marginal counts and joint count must not be mixed. Marginal counts must appear first, joint counts second");
			}
			if (!triple.getSubject().startsWith(MARGINAL_PREFIX)) {
				throw new IllegalArgumentException("Marginal counts must start with '1:'");
			}
			final int marginal = Integer.parseInt(triple.getObject());
			if (marginal >= minCount) {

				marginals.put(triple.getSubject().substring(2), Integer.valueOf(marginal));
			}

		} else {
			inHeader = false;
			if (!triple.getSubject().startsWith(JOINT_PREFIX)) {
				throw new IllegalArgumentException("Joint counts must start with '2:'");
			}

			final int nab = Integer.parseInt(triple.getObject());
			final String[] keyParts = KEY_SPLIT_PATTERN.split(triple.getSubject().substring(2));
			if (nab >= minCount) {

				final int na = getMarginal(keyParts[0]);
				final int nb = getMarginal(keyParts[1]);
				processCount(keyParts[0], keyParts[1], na, nb, nab);
			}
		}
	}

	protected abstract void processCount(final String varA, final String varB, final int countA, final int countB,
			final int countAandB);

	private int getMarginal(final String string) {
		final Integer value = marginals.get(string);
		if(null==value){
			return 0;
		}
		return value.intValue();
	}

	@Override
	protected final void onResetStream() {
		marginals.clear();
		inHeader = true;
		reset();
	}

	protected void reset() {
		// nothing to do

	}

	@Override
	protected final void onCloseStream() {
		onResetStream();
		close();
	}

	protected void close() {
		// nothing to do

	}

}
