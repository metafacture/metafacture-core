default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
morph(FLUX_DIR + "gnd-pref-label.xml")|
stream-to-triples|
sort-triples(by="object")|
template("${s}\t${o}")|
write("stdout");
