default file = FLUX_DIR + "10.marc21";

file|
open-file|
as-lines|
decode-marc21|
morph(FLUX_DIR + "marc21.xml")|
stream-to-triples|
template("${o}")|
write("stdout");
