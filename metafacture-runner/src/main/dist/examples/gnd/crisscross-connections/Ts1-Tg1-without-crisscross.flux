default base = "";
default dump = FLUX_DIR + "10.pica";
default out = base + "Ts1-Tg1-without-crisscross.txt";

"counting references in " + dump | write("stdout");

dump|
open-file|
as-lines|
catch-object-exception|
decode-pica|
batch-log(batchsize="100000")|
fix(FLUX_DIR + "extract.fix")|
stream-to-triples(redirect="true")|
sort-triples(by="subject")|
collect-triples|
fix(FLUX_DIR + "output.fix")|
batch-log(batchsize="100000")|
encode-csv(noquotes="true",separator=";")|
write(out);
