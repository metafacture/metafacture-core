// opens file 'fileName', interprets the content as non normalized serialized
// pica+ and writes to stdout

default fileName = FLUX_DIR + "nonNormalized.pica";

fileName|
open-file|
as-lines|
lines-to-records|
decode-pica(normalizedSerialization="false", ignoreMissingIdn="true")|
encode-formeta(style="multiline")|
write("stdout");
