
default counts="myflux/counts.dat";
default catalogue = FLUX_DIR + "10.pica";

//count references
"counting references in " + catalogue | write("stdout");

catalogue|
open-file|
as-lines|
catch-object-exception|
decode-pica|
fix(FLUX_DIR + "references.fix")|
stream-to-triples|
count-triples(countBy="object")|

write("subjects.dat");


