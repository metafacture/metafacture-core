default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
fix(FLUX_DIR + "gnd-type.fix")|
stream-to-triples|
count-triples(countBy="object")|
template("${s}\t${o}")|
write("stdout");
