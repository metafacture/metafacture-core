default in = ".";

in
|walk-filetree
|digest-file("sha1")
|sort-triples(by="OBJECT")
|reorder-triple(subjectFrom="object", objectFrom="subject")
|collect-triples
|filter(FLUX_DIR + "filter-duplicates.xml")
|encode-formeta(style="multiline")
|write("stdout");