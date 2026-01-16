FLUX_DIR + "inputFile.json"
| open-file
| as-records
| decode-json
| fix(FLUX_DIR + "base.fix",*)
| encode-json
| print
;
