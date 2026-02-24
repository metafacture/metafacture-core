package org.metafacture.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple class that mocks a SolrClient. Stores added document in memory.
 */
public class FakeSolrClient extends SolrClient {

    final Map<String, List<SolrInputDocument>> storage;

    public FakeSolrClient() {
        this.storage = new HashMap<>();
    }

    public List<SolrInputDocument> getCollection(final String collection) {
        return storage.getOrDefault(collection, new ArrayList<>());
    }

    @Override
    public UpdateResponse add(final String collection, final Collection<SolrInputDocument> docs, int commitWithinMs) throws SolrServerException, IOException {
        if (!storage.containsKey(collection)) storage.put(collection, new ArrayList<>());
        final List<SolrInputDocument> list = storage.get(collection);
        list.addAll(docs);

        final UpdateResponse updateResponse = new UpdateResponse();
        updateResponse.setElapsedTime(0);
        final NamedList<Object> response = new NamedList<>();
        response.add("status", 0);
        updateResponse.setResponse(response);
        return updateResponse;
    }

    @Override
    public NamedList<Object> request(final SolrRequest request, final String collection) throws SolrServerException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
}
