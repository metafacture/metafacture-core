//creates a beacon file based on a pica+ dump of the DNB CBS data.

default type = "ALL";
default out = dump + "-" + type + ".beacon";
default header = FLUX_DIR + "header.txt";


//read header
"reading header " + header | write("stdout");
header|open-file|as-lines|@Y;

//count references
"counting references in " + dump | write("stdout");

dump|
open-file|
as-lines|
catch-object-exception|
decode-pica|
batch-log(batchsize="100000")|
morph(FLUX_DIR + "extract.xml", *)|
stream-to-triples(redirect="true")|
sort-triples(by="subject")|
collect-triples|
morph(FLUX_DIR + "output.xml")|
batch-log("merged ${totalRecords}", batchsize="100000")|
stream-to-triples|
template("${s}")|
@Y;

@Y|
wait-for-inputs("2")|
write(out);



