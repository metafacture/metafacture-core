default files = FLUX_DIR;

files + "gndRdf.xml.bz2" |
open-file |
decode-xml|
split-xml(entityName="Description",toplevelelement="rdf:RDF")|
extract-literals|
write("stdout")
};
