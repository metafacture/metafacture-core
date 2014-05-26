default files = FLUX_DIR;

// beware: to use this URL your IP has to be allowed by registration
"http://services.d-nb.de/oai/repository" |
open-oaipmh(dateFrom="2013-08-11",dateUntil="2013-08-12",metadataPrefix="PicaPlus-xml",setSpec="bib") |
decode-xml |
handle-picaxml |
encode-formeta(style="multiline")|
write("stdout");