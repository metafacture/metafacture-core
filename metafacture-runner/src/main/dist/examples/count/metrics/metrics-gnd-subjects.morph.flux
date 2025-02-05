
catalogue|
open-file|
as-lines|
catch-object-exception|
decode-pica|
batch-log(batchsize="100000")|
morph(FLUX_DIR + "subject-cooccurrence.xml")|
stream-to-triples|
count-triples(countBy="object")|
calculate-metrics("X2")|
template("${s} ${o}")|
//write("stdout");
write(FLUX_DIR+"x2.dat");
