default file = FLUX_DIR + "10.marc21";

file|
open-file|
as-lines|
decode-marc21|
fix(FLUX_DIR + "morph-marc21.fix")|
stream-to-triples|
template("${o}")|
write("stdout");
