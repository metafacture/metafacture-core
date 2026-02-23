package org.metafacture.solr;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
        SolrInputDocument document = buffer.getObject();

        assertThat(document.getFieldNames(), hasItems("name", "alias"));
        assertThat(document.getField("name").getValue(), equalTo("alice"));
        assertThat(document.getField("alias").getValue(), equalTo("bob"));
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
        SolrInputDocument document = buffer.getObject();

        assertThat(document.getFieldNames(), hasItems("name"));
        assertThat(document.getField("name").getValueCount(), equalTo(2));
        assertThat(document.getField("name").getValues(), hasItems("alice", "bob"));
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
        SolrInputDocument document = buffer.getObject();

        assertThat(document.getFieldNames(), hasItems("id", "name"));
        assertThat(document.getFieldValue("id"), equalTo("1"));

        Map<String,Object> map = (Map<String,Object>) document.getFieldValue("name");
        assertThat(map, hasEntry("add", "alice"));
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
        SolrInputDocument document = buffer.getObject();

        assertThat(document.getFieldNames(), hasItems("id", "name"));
        assertThat(document.getFieldValue("id"), equalTo("1"));

        HashMap<String, List<String>> atomicUpdatesNames = new HashMap<>();
        atomicUpdatesNames.put("add", Stream.of("alice", "bob").collect(Collectors.toList()));
        assertThat(document.getFieldValue("name"), equalTo(atomicUpdatesNames));

        HashMap<String, String> atomicUpdatesAge = new HashMap<>();
        atomicUpdatesAge.put("set", "20");
        assertThat(document.getFieldValue("age"), equalTo(atomicUpdatesAge));
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
        SolrInputDocument document = buffer.getObject();

        String documentString = document.toString();
        String expectedDocumentString = "SolrInputDocument(fields: [id=1, name={add=[alice, bob], remove=claire}])";
        assertThat(documentString, is(equalTo(expectedDocumentString)));
    }

    private void startRoot(SolrXmlHandler handler) {
        handler.startElement("", "add", "add", new AttributesImpl());
    }

    private void startDocument(SolrXmlHandler handler) {
        handler.startElement("", "doc", "doc", new AttributesImpl());
    }

    private void addField(SolrXmlHandler handler, String name, String value) {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", name);
        handler.startElement("", "field", "field", atts);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", "field", "field");
    }

    private void addField(SolrXmlHandler handler, String name, String value, String updateMethod) {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", "CDATA", name);
        atts.addAttribute("", "update", "update", "CDATA", updateMethod);
        handler.startElement("", "field", "field", atts);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", "field", "field");
    }

    private void endDocument(SolrXmlHandler handler) {
        handler.endElement("", "doc", "doc");
    }

    private void endRoot(SolrXmlHandler handler) {
        handler.endElement("", "add", "add");
    }
}
