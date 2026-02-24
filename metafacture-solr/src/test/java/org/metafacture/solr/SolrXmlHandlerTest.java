package org.metafacture.solr;

import org.apache.solr.common.SolrInputDocument;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolrXmlHandlerTest {

    private SolrXmlHandler handler;
    private ObjectBuffer<SolrInputDocument> buffer;

    @Before
    public void setUp() throws Exception {
        buffer = new ObjectBuffer<>();
        handler = new SolrXmlHandler();
        handler.setReceiver(buffer);
    }

    @Test
    public void singleDocument() {
        startRoot(handler);
        startDocument(handler);
        addField(handler, "name", "alice");
        addField(handler, "alias", "bob");
        endDocument(handler);
        endRoot(handler);

        buffer.closeStream();
        final SolrInputDocument document = buffer.getObject();

        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("name", "alias"));
        Assert.assertThat(document.getField("name").getValue(), CoreMatchers.equalTo("alice"));
        Assert.assertThat(document.getField("alias").getValue(), CoreMatchers.equalTo("bob"));
    }

    @Test
    public void multiValueDocument() {
        startRoot(handler);
        startDocument(handler);
        addField(handler, "name", "alice");
        addField(handler, "name", "bob");
        endDocument(handler);
        endRoot(handler);

        buffer.closeStream();
        final SolrInputDocument document = buffer.getObject();

        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("name"));
        Assert.assertThat(document.getField("name").getValueCount(), CoreMatchers.equalTo(2));
        Assert.assertThat(document.getField("name").getValues(), CoreMatchers.hasItems("alice", "bob"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void singleUpdateDocument() {
        startRoot(handler);
        startDocument(handler);
        addField(handler, "id", "1");
        addField(handler, "name", "alice", "add");
        endDocument(handler);
        endRoot(handler);

        buffer.closeStream();
        final SolrInputDocument document = buffer.getObject();

        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("id", "name"));
        Assert.assertThat(document.getFieldValue("id"), CoreMatchers.equalTo("1"));

        final Map<String, Object> map = (Map<String, Object>) document.getFieldValue("name");
        Assert.assertThat(map, Matchers.hasEntry("add", "alice"));
    }

    @Test
    public void multiUpdateDocument() {
        startRoot(handler);
        startDocument(handler);
        addField(handler, "id", "1");
        addField(handler, "name", "alice", "add");
        addField(handler, "name", "bob", "add");
        addField(handler, "age", "20", "set");
        endDocument(handler);
        endRoot(handler);

        buffer.closeStream();
        final SolrInputDocument document = buffer.getObject();

        Assert.assertThat(document.getFieldNames(), CoreMatchers.hasItems("id", "name"));
        Assert.assertThat(document.getFieldValue("id"), CoreMatchers.equalTo("1"));

        final HashMap<String, List<String>> atomicUpdatesNames = new HashMap<>();
        atomicUpdatesNames.put("add", Stream.of("alice", "bob").collect(Collectors.toList()));
        Assert.assertThat(document.getFieldValue("name"), CoreMatchers.equalTo(atomicUpdatesNames));

        final HashMap<String, String> atomicUpdatesAge = new HashMap<>();
        atomicUpdatesAge.put("set", "20");
        Assert.assertThat(document.getFieldValue("age"), CoreMatchers.equalTo(atomicUpdatesAge));
    }

    @Test
    public void applyTwoModifiersOnOneField() {
        startRoot(handler);
        startDocument(handler);
        addField(handler, "id", "1");
        addField(handler, "name", "alice", "add");
        addField(handler, "name", "bob", "add");
        addField(handler, "name", "claire", "remove");
        endDocument(handler);
        endRoot(handler);

        buffer.closeStream();
        final SolrInputDocument document = buffer.getObject();

        final String documentString = document.toString();
        final String expectedDocumentString = "SolrInputDocument(fields: [id=1, name={add=[alice, bob], remove=claire}])";
        Assert.assertThat(documentString, CoreMatchers.is(CoreMatchers.equalTo(expectedDocumentString)));
    }

    private void startRoot(final SolrXmlHandler handler) {
        handler.startElement("", "add", "add", new AttributesImpl());
    }

    private void startDocument(final SolrXmlHandler handler) {
        handler.startElement("", "doc", "doc", new AttributesImpl());
    }

    private void addField(final SolrXmlHandler handler, final String name, final String value) {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", name);
        handler.startElement("", "field", "field", atts);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", "field", "field");
    }

    private void addField(final SolrXmlHandler handler, final String name, final String value, final String updateMethod) {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", name);
        atts.addAttribute("", "update", "update", "CDATA", updateMethod);
        handler.startElement("", "field", "field", atts);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", "field", "field");
    }

    private void endDocument(final SolrXmlHandler handler) {
        handler.endElement("", "doc", "doc");
    }

    private void endRoot(final SolrXmlHandler handler) {
        handler.endElement("", "add", "add");
    }
}
