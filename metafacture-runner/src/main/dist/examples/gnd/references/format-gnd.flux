
default base = "";
default gndsimple = base + "gnd-simplified.dat";

gnd|
open-file|
as-lines|
decode-pica|
fix(FLUX_DIR + "format-gnd.fix")|
encode-formeta(style="concise")|
write(gndsimple);




