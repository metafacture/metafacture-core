// REQUIRES THE METAFACTURE-SEARCH PLUGIN

default gnd = FLUX_DIR + "Tp-200.pica.gz";
default beaconDir = FLUX_DIR + "beacons";

"reading beacons from " + beaconDir | write("stdout");

beaconDir|
read-dir|
log-object|
catch-object-exception|
open-file|
read-beacon(metadatafilter="name|description|institution")|
stream-to-triples|
@X;

"reading GND dump from " + gnd | write("stdout");

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
fix(
    "unless any_match('002@.0', '.p.*') # Accept only Tp records
        reject()
    end"
)|
encode-formeta|
write("stdout");
//stream-to-index(FLUX_DIR + "id.xml", indexpath="Tp_ix");

