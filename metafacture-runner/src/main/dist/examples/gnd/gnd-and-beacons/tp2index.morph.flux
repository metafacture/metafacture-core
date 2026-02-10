// REQUIRES THE METAFACTURE-SEARCH PLUGIN

default gnd = FLUX_DIR + "Tp-200.pica.gz";
default beaconDir = FLUX_DIR + "beacons";

"reading beacons from " + beaconDir | print;

beaconDir|
read-dir|
log-object|
catch-object-exception|
open-file|
read-beacon(metadatafilter="name|description|institution")|
stream-to-triples|
@X;

"reading GND dump from " + gnd | print;

gnd|
open-file|
as-lines|
object-batch-log(batchSize="100000")|
decode-pica|
stream-to-triples|
@X;

@X|
sort-triples(by="subject")|
collect-triples|
filter(FLUX_DIR + "filter.xml")|
encode-formeta|
print;
//stream-to-index(FLUX_DIR + "id.xml", indexpath="Tp_ix");

