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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jcsp.lang.Barrier;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.PoisonException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolrCommitProcess implements CSProcess {
    private ChannelInput<SolrInputDocument> channelInput;
    private Barrier barrier;
    private SolrClient client;
    private String collection;
    private int batchSize;
    private int commitWithinMs;
    private int maxRetries;
    private int waitMs;

    public SolrCommitProcess(final ChannelInput<SolrInputDocument> channelInput, final Barrier barrier, final SolrClient client, final String collection) {
        this.channelInput = channelInput;
        this.barrier = barrier;
        this.client = client;
        this.collection = collection;
        this.batchSize = 1;
        this.commitWithinMs = -1;
        this.maxRetries = 0;
        this.waitMs = 10_000;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setWaitMs(final int waitMs) {
        this.waitMs = waitMs;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public void setCommitWithinMs(final int commitWithinMs) {
        this.commitWithinMs = commitWithinMs;
    }

    @Override
    public void run() {
        List<SolrInputDocument> batch = new ArrayList<>(batchSize);

        while (true) {
            final SolrInputDocument document;
            try {
                document = receive();
                batch.add(document);
            }
            catch (final PoisonException e) {
                if (!batch.isEmpty()) {
                    final boolean isSuccessful = commit(batch);
                    if (!isSuccessful) {
                        retryCommit(batch, maxRetries, waitMs);
                    }
                }
                barrier.sync();
                break;
            }

            if (batch.size() == batchSize) {
                final boolean isSuccessful = commit(batch);

                if (!isSuccessful) {
                    retryCommit(batch, maxRetries, waitMs);
                }

                batch = new ArrayList<>(batchSize);
            }
        }
    }

    private void retryCommit(final List<SolrInputDocument> documents, final int retries, final int waitMs) {
        int retryLimit = retries;
        while (true) {
            if (retryLimit == 0) {
                break;
            }

            try {
                Thread.sleep(waitMs);
            }
            catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            final boolean retrySuccessful = commit(documents);

            if (retrySuccessful) {
                break;
            }
            else {
                retryLimit -= 1;
            }
        }
    }

    private boolean commit(final List<SolrInputDocument> documents) {
        try {
            final UpdateResponse response;
            if (commitWithinMs >= 0) {
                response = client.add(collection, documents, commitWithinMs);
            }
            else {
                response = client.add(collection, documents);
            }
            if (response.getStatus() != 0) {
                return false;
            }
        }
        catch (final IOException e) {
            System.err.println("Could not commit batch, due to communication error: " + e.getMessage());
            return false;
        }
        catch (final SolrServerException e) {
            System.err.println("Could not commit batch, due to server error: " + e.getMessage());
            return false;
        }
        catch (final Exception e) {
            System.err.println("Could not commit batch, due to unknown error: " + e.getMessage());
            return false;
        }
        return true;
    }

    private SolrInputDocument receive() {
        return channelInput.read();
    }
}
