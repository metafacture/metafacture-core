// opens file 'fileName', interprets the content as marc21 and writes to stdout

default fileName = FLUX_DIR + "10.marc21";

fileName|
open-file|
as-lines|
decode-marc21|
encode-formeta(style="multiline")|
write("stdout");
