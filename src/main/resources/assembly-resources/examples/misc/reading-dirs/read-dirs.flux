default dir = FLUX_DIR + "dir";

dir|
read-dir(recursive="true")|
open-file|
as-lines|
write("stdout");
