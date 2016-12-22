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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;


/**
 * Draws a uniform sample of records from the input stream.
 *
 * @param <T> object type
 *
 * @author Christoph Böhme
 * @author Markus Geipel
 *
 */
@Description("Draws a uniform sample of records from the input stream.")
@In(Object.class)
@Out(Object.class)
@FluxCommand("draw-uniform-sample")
public final class UniformSampler<T> extends
		DefaultObjectPipe<T, ObjectReceiver<T>> {

	private final int sampleSize;
	private final List<T> sample;
	private final Random random = new Random();

	private long count;

	public UniformSampler(final int sampleSize) {
		super();
		this.sampleSize = sampleSize;
		sample = new ArrayList<T>(sampleSize);
	}


	public UniformSampler(final String sampleSize) {
		this(Integer.parseInt(sampleSize));
	}


	public int getSampleSize() {
		return sampleSize;
	}


	public void setSeed(final long seed) {
		random.setSeed(seed);
	}

	@Override
	public void process(final T obj) {
		assert !isClosed();
		assert null!=obj;
		count += 1;
		if (sample.size() < sampleSize) {
			sample.add(obj);
		} else {
			final double p = sampleSize / (double)count;
			if (random.nextDouble() < p) {
				sample.set(random.nextInt(sampleSize), obj);
			}
		}
	}

	@Override
	protected void onCloseStream() {
		for(T obj : sample) {
			getReceiver().process(obj);
		}
		sample.clear();
		count = 0;
	}

	@Override
	protected void onResetStream() {
		sample.clear();
		count = 0;
	}

}
