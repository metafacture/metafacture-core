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

package org.metafacture.statistics;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    /**
     * Creates an instance of {@link UniformSampler} with a given sample size.
     *
     * @param sampleSize the size of the sample
     */
    public UniformSampler(final int sampleSize) {
        this.sampleSize = sampleSize;
        sample = new ArrayList<T>(sampleSize);
    }

    /**
     * Creates an instance of {@link UniformSampler} with a given sample size.
     *
     * @param sampleSize the sample size
     */
    public UniformSampler(final String sampleSize) {
        this(Integer.parseInt(sampleSize));
    }

    /**
     * Gets the sample size.
     *
     * @return the sample size.
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Sets the seed.
     *
     * @param seed the seed.
     */
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    @Override
    public void process(final T obj) {
        assert !isClosed();
        assert null != obj;
        count += 1;
        if (sample.size() < sampleSize) {
            sample.add(obj);
        }
        else {
            final double p = sampleSize / (double) count;
            if (random.nextDouble() < p) {
                sample.set(random.nextInt(sampleSize), obj);
            }
        }
    }

    @Override
    protected void onCloseStream() {
        for (final T obj : sample) {
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
