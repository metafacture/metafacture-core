default in = FLUX_DIR + "filedigest.flux";

in
|digest-file("md5")
|write("stdout");