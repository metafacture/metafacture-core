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
morph(FLUX_DIR + "extract.xml")|
stream-to-triples(redirect="true")|
sort-triples(by="subject")|
collect-triples|
morph(FLUX_DIR + "output.xml")|
batch-log(batchsize="100000")|
stream-to-triples|
template("${o}")|
write(out);
