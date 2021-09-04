default fileName = FLUX_DIR + "austriaforum.beacon";

fileName|
open-file(encoding="UTF-8")|
read-beacon(metadatafilter="name|isil")|
encode-formeta(style="multiline")|
write("stdout");
