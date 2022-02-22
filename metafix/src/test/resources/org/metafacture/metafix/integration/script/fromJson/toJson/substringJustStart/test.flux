FLUX_DIR + "input.json"
|open-file
|as-records
|decode-json
|fix(FLUX_DIR + "test.fix")
|encode-json
|write(FLUX_DIR + "output-metafix.json")
;
