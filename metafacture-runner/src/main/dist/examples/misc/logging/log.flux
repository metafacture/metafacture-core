FLUX_DIR + "inputFile.json"
| open-file
| as-records
| log-object(prefix="Before Transformation Object Log: ")
| decode-json
| log-stream(prefix="Before Transformation Stream Log: ")
| fix("add_field('test','field')")
| log-stream(prefix="After Transformation Stream Log: ")
| encode-json
| log-object(prefix="Output Object Log: ")
| print
;
