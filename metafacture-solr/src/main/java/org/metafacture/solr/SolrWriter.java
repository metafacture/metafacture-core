/*
 * Copyright 2018 Deutsche Nationalbibliothek
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

package org.metafacture.solr;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.solr.helpers.DefaultSolrDocumentReceiver;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jcsp.lang.Barrier;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2AnyChannel;
import org.jcsp.lang.Parallel;

@Description("Adds documents to a Solr core.")
@In(SolrDocumentReceiver.class)
@Out(Void.class)
@FluxCommand("to-solr")
public class SolrWriter extends DefaultSolrDocumentReceiver {

    /** Solr Server URL */
    private String url;
    private String core;

    private SolrClient client;
    /** Number of document per commit */
    private int batchSize;
    /** Time range in which a commit will happen. */
    private int commitWithinMs;

    private int maxRetries;
    private int waitMs;

    /** Number of threads to run in parallel */
    private int threads;
    private Barrier barrier;
    private One2AnyChannel<SolrInputDocument> documentChannel;

    private Thread runner;

    /** Flag for a hook that acts before the first processing occurs. */
    private boolean onStartup;

    public SolrWriter(final String url) {
        this.url = url;
        this.core = "default";
        this.threads = 1;
        this.batchSize = 1;
        this.commitWithinMs = -1;
        this.onStartup = true;
        this.maxRetries = 0;
        this.waitMs = 10_000;
    }

    public void setCore(final String core) {
        this.core = core;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public void setCommitWithinMs(final int commitWithinMs) {
        this.commitWithinMs = commitWithinMs;
    }

    public void setThreads(final int threads) {
        this.threads = threads;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setWaitMs(final int waitMs) {
        this.waitMs = waitMs;
    }

    @Override
    public void process(final SolrInputDocument document) {
        if (onStartup) {
            final HttpSolrClient httpClient = new HttpSolrClient.Builder()
                    .withBaseSolrUrl(url)
                    .allowCompression(true)
                    .build();
            httpClient.setRequestWriter(new BinaryRequestWriter());
            client = httpClient;

            final int noPoisonImmunity = 0;
            documentChannel = Channel.one2any(noPoisonImmunity);

            barrier = new Barrier(threads);

            final Parallel parallel = new Parallel();
            for (int i = 0; i < threads; i++) {
                final SolrCommitProcess process = new SolrCommitProcess(documentChannel.in(), barrier, client, core);
                process.setBatchSize(batchSize);
                process.setCommitWithinMs(commitWithinMs);
                process.setMaxRetries(maxRetries);
                process.setWaitMs(waitMs);
                parallel.addProcess(process);
            }

            onStartup = false;

            runner = new Thread(new Runnable() {
                @Override
                public void run() {
                    parallel.run();
                }
            });
            runner.start();
        }

        documentChannel.out().write(document);
    }

    @Override
    public void resetStream() {
        onStartup = true;
        documentChannel.out().poison(1);
    }

    @Override
    public void closeStream() {
        documentChannel.out().poison(1);
        try {
            runner.join();
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MetafactureException(e);
        }
    }
}
