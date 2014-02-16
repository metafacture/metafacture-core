default data = FLUX_DIR + "data.txt";
default temp_file = "data.tmp";

//first flow
data|
open-file|
as-lines|
write(temp_file);

//second flow
temp_file|
open-file|
as-lines|
write("stdout");

