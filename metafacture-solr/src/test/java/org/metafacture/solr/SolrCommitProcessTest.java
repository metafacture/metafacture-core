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

import org.apache.solr.common.SolrInputDocument;
import org.hamcrest.CoreMatchers;
import org.jcsp.lang.Barrier;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.ChannelOutput;
import org.jcsp.lang.One2AnyChannel;
import org.jcsp.lang.Parallel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolrCommitProcessTest {

    private FakeSolrClient client;
    private One2AnyChannel<SolrInputDocument> documentChannel;
    private int noPoisonImmunity;

    public SolrCommitProcessTest() {
    }

    @Before
    public void setUp() throws Exception {
        client = new FakeSolrClient();
        documentChannel = Channel.one2any(noPoisonImmunity);
    }

    @Test
    public void shouldTerminate() {
        final CSProcess send = new SendProcess(documentChannel.out(), new ArrayList<>());
        final CSProcess commit = new SolrCommitProcess(documentChannel.in(), new Barrier(1), client, "test");
        final Parallel parallel = new Parallel();
        parallel.addProcess(send);
        parallel.addProcess(commit);
        parallel.run();
    }

    @Test
    public void shouldReceiveDocuments() {
        final SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("id", "1");

        final SolrInputDocument doc2 = new SolrInputDocument();
        doc2.addField("id", "2");

        final SolrInputDocument doc3 = new SolrInputDocument();
        doc3.addField("id", "3");

        final List<SolrInputDocument> docs = Stream.of(doc1, doc2, doc3).collect(Collectors.toList());

        final CSProcess send = new SendProcess(documentChannel.out(), docs);
        final SolrCommitProcess commit = new SolrCommitProcess(documentChannel.in(), new Barrier(1), client, "test");
        commit.setBatchSize(2);

        final Parallel parallel = new Parallel();
        parallel.addProcess(send);
        parallel.addProcess(commit);
        parallel.run();

        final List<SolrInputDocument> collection = client.getCollection("test");
        Assert.assertThat(collection.size(), CoreMatchers.is(CoreMatchers.equalTo(3)));
        Assert.assertThat(collection, CoreMatchers.hasItems(doc1, doc2, doc3));
    }

    /** A simple producer process that puts elements into a channel. */
    class SendProcess implements CSProcess {

        private ChannelOutput<SolrInputDocument> channel;
        private List<SolrInputDocument> documents;

        SendProcess(final ChannelOutput<SolrInputDocument> channel, final List<SolrInputDocument> documents) {
            this.channel = channel;
            this.documents = documents;
        }

        @Override
        public void run() {
            documents.forEach(channel::write);
            channel.poison(1);
        }
    }
}
