
default base = "";
default counts= base + "counts.dat";
default dump = FLUX_DIR + "10.pica";

//count references
"counting references in " + dump | write("stdout");

dump|
open-file|
as-lines|
catch-object-exception|
decode-pica|
batch-log(batchsize="100000")|
fix(FLUX_DIR + "references.fix")|
stream-to-triples|
decouple|
count-triples(countBy="object")|
collect-triples|
encode-formeta(style="concise")|
write(counts);


