default files = FLUX_DIR;

files+"HT010726584.xml.bz2"|
open-file(compression="BZIP2") |
decode-xml |
handle-mabxml |
encode-formeta(style="multiline")|
write("stdout");
};
