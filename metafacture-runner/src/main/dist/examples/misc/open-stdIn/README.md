A Flux workflow can process the input of stdId (Standard Input) with the `>` instructor
instead of the usual string/opener at the beginning of a workflow.

For this example change to the folder of this distibution and run `echo '{ "test" : "case" }' | flux.sh 'examples/misc/open-stdIn/test.flux'`
