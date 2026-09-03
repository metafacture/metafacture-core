FLUX_DIR + "input.tsv"
|open-file
|as-lines
|decode-csv(hasHeader="false",separator="\t")
|merge-batch-stream(batchSize="6")
|fix(FLUX_DIR + "test.fix")
|encode-json(prettyPrinting="true")
|write(FLUX_DIR + "output-metafix.json")
;
