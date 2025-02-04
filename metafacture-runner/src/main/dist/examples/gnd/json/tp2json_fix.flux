default gnd = FLUX_DIR + "Tp-200.pica.gz";

gnd
| open-file
| as-lines
| decode-pica(normalizeutf8="true", normalizedserialization="true")
| fix(FLUX_DIR + "tp2json.fix")
| normalize-unicode-stream
| encode-json
| write(FLUX_DIR + "test.txt");
