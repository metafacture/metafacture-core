// opens file 'fileName', interprets the content as pica and writes to stdout

default fileName = FLUX_DIR + "10.pica";

fileName|
open-file|
as-lines|
decode-pica|
encode-formeta(style="multiline")|
write("stdout");
