FLUX_DIR + "records.xml"
| open-file
| decode-xml
| handle-solr-xml
| to-solr("http://localhost:1111/solr/", core="test", commitWithinMs="1000")
;
