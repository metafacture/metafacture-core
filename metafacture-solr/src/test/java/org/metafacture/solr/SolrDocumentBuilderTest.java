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
import org.apache.solr.common.SolrInputField;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SolrDocumentBuilderTest {

    private SolrDocumentBuilder builder;
    private ObjectBuffer<SolrInputDocument> buffer;

    @Before
    public void setUp() {
        buffer = new ObjectBuffer<SolrInputDocument>();
        builder = new SolrDocumentBuilder();
        builder.setReceiver(buffer);
    }

    @Test
    public void shouldIgnoreRecordId() {
        builder.startRecord("ignored");
        builder.literal("name", "alice");
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();
        Assert.assertThat(document.getFieldNames().size(), CoreMatchers.is(CoreMatchers.equalTo(1)));
        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("name"));
    }

    @Test
    public void shouldContainSingleValueField() {
        builder.startRecord("id1");
        builder.literal("name", "alice");
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();
        final SolrInputField field = document.getField("name");
        Assert.assertThat(field.getValueCount(), CoreMatchers.is(CoreMatchers.equalTo(1)));
        Assert.assertThat(field.getFirstValue(), CoreMatchers.is(CoreMatchers.equalTo("alice")));
    }

    @Test
    public void shouldContainMultiValueField() {
        builder.startRecord("id1");
        builder.literal("name", "alice");
        builder.literal("name", "bob");
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();
        final SolrInputField field = document.getField("name");
        Assert.assertThat(field.getValueCount(), CoreMatchers.is(CoreMatchers.equalTo(2)));
        Assert.assertThat(field.getValues(), CoreMatchers.hasItems("alice", "bob"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldContainAtomicUpdates() {
        builder.startRecord("id1");
        builder.startEntity("add");
        builder.literal("name", "alice");
        builder.literal("name", "bob");
        builder.endEntity();
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();

        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("name"));

        final SolrInputField field = document.getField("name");
        Assert.assertThat(field.getValueCount(), CoreMatchers.is(CoreMatchers.equalTo(1)));

        final Object value = field.getFirstValue();
        Assert.assertThat(value, CoreMatchers.is(CoreMatchers.instanceOf(Map.class)));

        final Map<String, List<String>> valueMap = (Map<String, List<String>>) value;
        Assert.assertThat(valueMap.keySet(), CoreMatchers.hasItems("add"));
        Assert.assertThat(valueMap.get("add"), CoreMatchers.hasItems("alice", "bob"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldContainTwoAtomicUpdates() {
        builder.startRecord("id1");
        builder.startEntity("set");
        builder.literal("title", "New Title");
        builder.endEntity();
        builder.startEntity("add");
        builder.literal("name", "alice");
        builder.literal("name", "bob");
        builder.endEntity();
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();

        final String expectedDocument = "SolrInputDocument(fields: [title={set=New Title}, name={add=[alice, bob]}])";
        Assert.assertThat(document.toString(), CoreMatchers.is(CoreMatchers.equalTo(expectedDocument)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldContainTwoAtomicUpdatesForOneField() {
        builder.startRecord("id1");
        builder.startEntity("add");
        builder.literal("name", "alice");
        builder.literal("name", "bob");
        builder.endEntity();
        builder.startEntity("remove");
        builder.literal("name", "claire");
        builder.endEntity();
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();
        final String expectedDocument = "SolrInputDocument(fields: [name={add=[alice, bob], remove=claire}])";

        Assert.assertThat(document.toString(), CoreMatchers.is(CoreMatchers.equalTo(expectedDocument)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotPutSingleUpdateValueInList() {
        builder.startRecord("id1");
        builder.startEntity("add");
        builder.literal("name", "alice");
        builder.endEntity();
        builder.endRecord();
        builder.closeStream();

        final SolrInputDocument document = buffer.getObject();
        final String expectedDocument = "SolrInputDocument(fields: [name={add=alice}])";

        Assert.assertThat(document.toString(), CoreMatchers.is(CoreMatchers.equalTo(expectedDocument)));
    }
}
