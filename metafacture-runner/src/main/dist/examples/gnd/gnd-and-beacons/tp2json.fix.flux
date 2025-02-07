default gnd = FLUX_DIR + "Tp-200.pica.gz";
default beaconDir = FLUX_DIR + "beacons";
default out = "stdout";


"reading beacons from " + beaconDir | write("stdout");

beaconDir|
read-dir|
log-object|
catch-object-exception|
open-file|
read-beacon(metadatafilter="name|dedcription")|
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
fix(FLUX_DIR + "tp2json.fix")| // Fix does not use the filter function but has its own filter mechanism within fix.
encode-json|
write(out);
