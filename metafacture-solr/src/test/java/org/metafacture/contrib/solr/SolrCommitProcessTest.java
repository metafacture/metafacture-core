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
import org.jcsp.lang.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class SolrCommitProcessTest {

    private FakeSolrClient client;
    private One2AnyChannel<SolrInputDocument> documentChannel;
    private int noPoisonImmunity = 0;

    @Before
    public void setUp() throws Exception {
        client = new FakeSolrClient();
        documentChannel = Channel.one2any(noPoisonImmunity);
    }

    @Test
    public void shouldTerminate() {
        CSProcess send = new SendProcess(documentChannel.out(), new ArrayList<>());
        CSProcess commit = new SolrCommitProcess(documentChannel.in(), new Barrier(1), client, "test");
        Parallel parallel = new Parallel();
        parallel.addProcess(send);
        parallel.addProcess(commit);
        parallel.run();
    }

    @Test
    public void shouldReceiveDocuments() {
        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("id", "1");

        SolrInputDocument doc2 = new SolrInputDocument();
        doc2.addField("id", "2");

        SolrInputDocument doc3 = new SolrInputDocument();
        doc3.addField("id", "3");

        List<SolrInputDocument> docs = Stream.of(doc1, doc2, doc3).collect(Collectors.toList());

        CSProcess send = new SendProcess(documentChannel.out(), docs);
        SolrCommitProcess commit = new SolrCommitProcess(documentChannel.in(), new Barrier(1), client, "test");
        commit.setBatchSize(2);

        Parallel parallel = new Parallel();
        parallel.addProcess(send);
        parallel.addProcess(commit);
        parallel.run();

        List<SolrInputDocument> collection = client.getCollection("test");
        assertThat(collection.size(), is(equalTo(3)));
        assertThat(collection, hasItems(doc1, doc2, doc3));
    }

    /** A simple producer process that puts elements into a channel. */
    class SendProcess implements CSProcess {

        private ChannelOutput<SolrInputDocument> channel;
        private List<SolrInputDocument> documents;

        public SendProcess(ChannelOutput<SolrInputDocument> channel, List<SolrInputDocument> documents) {
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
