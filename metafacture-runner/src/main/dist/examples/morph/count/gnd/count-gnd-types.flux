default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
morph(FLUX_DIR + "gnd-type.xml")|
stream-to-triples|
count-triples(countBy="object")|
template("${s}\t${o}")|
write("stdout");
