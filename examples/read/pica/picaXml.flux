default files = FLUX_DIR;

files+"pica.xml.bz2"|
open-file(compression="BZIP2") |
decode-xml |
handle-picaxml |
encode-formeta(style="multiline")|
write("stdout");
};