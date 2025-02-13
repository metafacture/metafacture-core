
default base = "";
default gndsimple = base + "gnd-simplified.dat";

gnd|
open-file|
as-lines|
decode-pica|
morph(FLUX_DIR + "format-gnd.xml")|
encode-formeta(style="concise")|
write(gndsimple);




