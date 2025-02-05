
default base = "";
default counts= base + "counts.dat";
default gndsimple = base + "gnd-simplified.dat";
default out = base + "gnd-references.csv";

//merge and output
"megring information" | write("stdout");

counts + "," + gndsimple|
decode-string(",")|
open-file|
as-lines|
decode-formeta|
batch-log("records read: ${totalRecords}",batchsize="100000")|
stream-to-triples|
decouple|
sort-triples(by="subject")|
collect-triples|
fix(FLUX_DIR + "output.fix")|
batch-log(batchsize="100000")|
encode-csv(separator=";",noQuotes="true")|
write(out);
