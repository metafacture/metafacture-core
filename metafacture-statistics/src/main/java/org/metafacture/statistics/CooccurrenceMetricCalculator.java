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
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.objects.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates values for various co-occurrence metrics. The expected inputs are
 * triples containing as subject the var name and as object the count. Marginal
 * counts must appear first, joint counts second. Marginal counts must be
 * written as 1:A, Joint counts as 2:A&amp;B
 *
 * @author Markus Michael Geipel
 */
@Description("Calculates values for various cooccurrence metrics. The expected inputs are triples containing as subject the var name and as object the count. " +
    "Marginal counts must appear first, joint counts second. Marinal counts must be written as 1:A, Joint counts as 2:A&B")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("calculate-metrics")
public final class CooccurrenceMetricCalculator extends AbstractCountProcessor {

    /**
     * implementation of the different metrics
     */
    enum Metric {
        X2 {
            @Override
            double calculate(final int countA, final int countB, final int countAandB, final int total) {
                final double o12 = countA - countAandB;
                final double o21 = countB - countAandB;
                final double o22 = total - countAandB;
                final double d = (countAandB * o22) - (o12 * o21);

                final double x2 = total * Math.pow(d, 2) / ((countAandB + o12) * (countAandB + o21) * (o12 + o22) * (o21 + o22));
                return x2 * Math.signum(d);
            }
        },
        F {
            @Override
            double calculate(final int countA, final int countB, final int countAandB, final int total) {
                final double pa = (double) countA / total;
                final double pb = (double) countB / total;
                final double pab = (double) countAandB / total;
                final double precission = pab / pa;
                final double recall = pab / pb;

                return 2 * precission * recall / (precission + recall);
            }
        },
        PRECISSION {
            @Override
            double calculate(final int countA, final int countB, final int countAandB, final int total) {
                final double pa = (double) countA / total;
                final double pab = (double) countAandB / total;
                return pab / pa;
            }
        },
        RECALL {
            @Override
            double calculate(final int countA, final int countB, final int countAandB, final int total) {
                final double pb = (double) countB / total;
                final double pab = (double) countAandB / total;
                return pab / pb;
            }
        },
        JACCARD {
            @Override
            double calculate(final int countA, final int countB, final int countAandB, final int total) {
                return countAandB / (double) (countA + countB - countAandB);
            }
        };

        abstract double calculate(int countA, int countB, int countAandB, int total);
    }

    private static final int MIN_COUNT = 5;

    private final List<Metric> metrics = new ArrayList<Metric>();

    /**
     * Constructs a CooccurrenceMetricCalculator with given metrics.
     *
     * @param allMetrics the Metrics as a comma separated String
     */
    public CooccurrenceMetricCalculator(final String allMetrics) {
        setMinCount(MIN_COUNT);
        for (final String metric : allMetrics.split("\\s*,\\s*")) {
            metrics.add(Metric.valueOf(metric));
        }
    }

    /**
     * Constructs a CooccurrenceMetricCalculator with given metrics.
     *
     * @param metrics the Metrics as an array
     */
    public CooccurrenceMetricCalculator(final Metric... metrics) {
        setMinCount(MIN_COUNT);
        for (final Metric metric : metrics) {
            this.metrics.add(metric);
        }
    }

    @Override
    protected void processCount(final String varA, final String varB, final int countA, final int countB, final int countAandB) {
        for (final Metric metric : metrics) {
            final double value = metric.calculate(countA, countB, countAandB, getTotal());
            getReceiver().process(new Triple(varA + "&" + varB, metric.toString(), String.valueOf(value)));
        }
    }
}
