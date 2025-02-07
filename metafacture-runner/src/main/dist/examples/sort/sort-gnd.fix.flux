default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
fix(FLUX_DIR + "gnd-pref-label.fix")|
stream-to-triples|
sort-triples(by="object")|
template("${s}\t${o}")|
write("stdout");
