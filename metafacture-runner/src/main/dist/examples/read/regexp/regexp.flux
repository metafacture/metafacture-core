default fileName = FLUX_DIR + "dates.csv";

fileName|
open-file|
as-lines|
regex-decode("(?<id>\\w*)\\s.*geboren=(?<birth>\\d*)")|
encode-formeta(style="multiline")|
write("stdout");
