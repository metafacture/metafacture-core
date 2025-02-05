// opens file 'fileName', interprets the content as pica and filters the results

default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
filter(FLUX_DIR + "filter.xml")|
encode-formeta(style="verbose")|
write("stdout");
