FLUX_DIR + "records.formeta"
| open-file
| as-lines
| decode-formeta
| build-solr-doc
| print;
