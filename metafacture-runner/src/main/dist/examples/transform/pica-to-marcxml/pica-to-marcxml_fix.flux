// opens file 'fileName', interprets the content as non normalized serialized
// pica+, encode as marc, morphs to marcxml and writes to standard out

default fileName = FLUX_DIR + "nonNormalized.pica";

fileName
| open-file
| as-lines
| lines-to-records
| decode-pica(normalizedSerialization="false", ignoreMissingIdn="true")
| fix(FLUX_DIR + "morph-pica-to-marcxml.fix")
| encode-marcxml(ensurecorrectmarc21xml="true")
| write("stdout");
