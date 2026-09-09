A Flux workflow can process the standard input (stdin) with the `>` instruction
instead of the usual string/opener at the beginning of a workflow.

For this example, change to the folder of this distribution and run `echo '{ "test" : "case" }' | flux.sh -f examples/misc/open-stdin/test.flux`
