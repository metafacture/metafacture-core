FLUX_DIR + "records.formeta"
| open-file
| as-lines
| decode-formeta
| build-solr-doc
| to-solr("http://localhost:1111/solr/", core="test, commitWithinMs="1000");
