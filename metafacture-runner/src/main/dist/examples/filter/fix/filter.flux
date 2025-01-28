// opens file 'fileName', interprets the content as pica and filters the results

default fileName = FLUX_DIR + "gnd-sample.pica";

fileName|
open-file|
as-lines|
decode-pica|
fix(FLUX_DIR + "filter.fix")| // Fix does not use the filter function but has its own filter mechanism within fix.
encode-formeta(style="verbose")|
write("stdout");
