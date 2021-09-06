default idn = "1021191485";

idn|
template("https://portal.dnb.de/opac.htm?method=requestMarcXml&idn=${o}")|
open-http|
decode-xml|
handle-marcxml|
encode-formeta(style="multiline")|
write("stdout");
