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

    /**
     * Default settings for SolWriter
     *
     * @param url configures URL to Solr Server.
     */
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

    /**
     * Sets the the name of solr core
     *
     * @param core configures Solr Core (Default: default).
     */
    public void setCore(final String core) {
        this.core = core;
    }

    /**
     * Sets the the number of documents per commit.
     *
     * @param batchSize configures number of documents per commit (Default: 1)
     */
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Sets the the max time (in ms) before a commit.
     *
     * @param commitWithinMs configures max time (in ms) before a commit will happen (Default: -1 (Disabled), See also: link:https://lucene.apache.org/solr/guide/7_4/updatehandlers-in-solrconfig.html#UpdateHandlersinSolrConfig-commitWithin[Solr Ref Guide - commitWithin]).
     */
    public void setCommitWithinMs(final int commitWithinMs) {
        this.commitWithinMs = commitWithinMs;
    }

    /**
     * Sets the the number of threads.
     *
     * @param threads configures number of threads for concurrent batch processing (Default: 1).
     */
    public void setThreads(final int threads) {
        this.threads = threads;
    }

    /**
     * Sets the the maximal number of retries for each writing process.
     *
     * @param maxRetries configures the number of max retries for non-successful commits (Default: 0).
     */
    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Sets the wait time between each writing.
     *
     * @param waitMs configures the delay (in ms) before a retry will be triggered (Default: 10000).
     */
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
