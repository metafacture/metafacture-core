default out = "stdout";
default file = FLUX_DIR + "Test_DNB_Mono.xml";

//file|
//open-file|
"1025374754"|
template("https://portal.dnb.de/opac.htm?method=requestMarcXml&idn=${o}")| 
open-http|
decode-xml|
handle-marcxml|
morph(FLUX_DIR + "MARC21-EDM.xml", *)|
add-oreaggregation|
rdf-macros|
stream-to-xml(roottag="rdf:RDF", recordtag="", namespacefile= FLUX_DIR+"edm-namespaces.properties")|
write(out);