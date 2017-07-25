



"A"|
template("${o}")|
template("x${o}")|
@X;

"B"|
template("x${o}")|
@X;

@X|
template("${o}")|
write("stdout");


"C"|
template("x${o}")|
@X;
