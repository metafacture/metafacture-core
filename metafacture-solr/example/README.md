# Example

## build-solr-doc

Produces Solr documents from `records.formeta`.

```
flux.sh build-solr-doc.flux
```

## handle-solr-xml

Produces Solr documents from `records.xml`.

```
flux.sh handle-solr-xml.flux
```

## to-solr;

This set of examples requires a running solr server.

### Configuration

* Download the [latest](http://lucene.apache.org/solr/mirrors-solr-latest-redir.html) version
* Unzip the archive (e.g. *solr-9.10.1*)
* Start the server
  * `solr-9.10.1//bin/solr start -m 1g -p 1111 -V`
* Create a default core
  * `solr-9.10.1/bin/solr create_core -c "test" -s http://localhost:1111 --verbose`
* Stop the server
  * `solr-9.10.1/bin/solr stop -all -p 1111 -V`

Visit the [Admin UI](http://localhost:1111/solr/#/test/query) in your browser
and run `Execute Query`.

You may also visit the following address:

```
http://localhost:1111/solr/test/select?q=*:*
```

Example response:

```
{
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "q":"*:*"}},
  "response":{"numFound":1,"start":0,"docs":[
      {
        "id":"1",
        "authors_ss":["Alice", "Bob"],
        "subject_ss":["Sports"],
        "dd_s":"796.35",
        "numpages_i":999,
        "price_f":12.4,
        "title_t":"Summer of the all-rounder: Test and championship cricket in England 1982",
        "isbn_s":"0002166313",
        "yearpub_i":1982,
        "publisher_s":"Random House",
        "_version_":1614486275410100224
      }
  ]}
}
```

### from-formeta

Commits Solr documents generated from `records.formeta` to `http://localhost:1111/solr/test`.

```
flux.sh to-solr-from-formeta.flux
```

### from-xml

Commits Solr documents generated from `records.xml` to `http://localhost:1111/solr/test`.

```
flux.sh to-solr-from-xml.flux
```
