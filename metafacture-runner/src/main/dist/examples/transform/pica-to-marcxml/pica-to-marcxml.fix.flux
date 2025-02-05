// opens file 'fileName', interprets the content as non normalized serialized
// pica+, encode as marc, transforms to marcxml and writes to standard out

default fileName = FLUX_DIR + "nonNormalized.pica";

fileName|
open-file|
as-lines|
lines-to-records|
decode-pica(normalizedSerialization="false", ignoreMissingIdn="true")|
fix(FLUX_DIR + "pica-to-marcxml.fix")|
encode-marc21|
decode-marc21(emitLeaderAsWhole="true", ignoreMissingId="true")|
encode-marcxml|
write("stdout");
