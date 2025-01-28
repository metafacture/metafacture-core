default basedir = FLUX_DIR;

basedir|
read-dir|
filter-strings(".*\\.beacon", passmatches="true")|
open-file|
read-beacon(metadatafilter="name|isil")|
stream-to-triples|
sort-triples(by="subject")|
collect-triples|
encode-formeta(style="multiline")|
write("stdout");
