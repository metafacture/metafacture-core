// opens file 'fileName', interprets the content as non normalized serialized
// pica+, encode as marc, morphs to marcxml and writes to standard out

default fileName = FLUX_DIR + "nonNormalized.pica";

fileName|
open-file|
as-lines|
lines-to-records|
decode-pica(normalizedSerialization="false", ignoreMissingIdn="true")|
morph(FLUX_DIR + "pica-to-marcxml.xml")|
encode-marc21|
decode-marc21(emitLeaderAsWhole="true", ignoreMissingId="true")|
encode-marcxml|
write("stdout");
