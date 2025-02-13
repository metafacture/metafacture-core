default gnd = FLUX_DIR + "Tp-200.pica.gz";

gnd|
open-file|
as-lines|
decode-pica|
fix(FLUX_DIR + "tp2json.fix")|
encode-json|
write("stdout");
