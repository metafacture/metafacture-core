About
-----

This is early work in progress towards an implementation of the FIX language for Metafacture.

See [https://github.com/elag/FIG](https://github.com/elag/FIG)

State
-----

This repo contains an Xtext web project with a basic FIX grammar, which generates a parser and a web editor.

[![Build Status](https://travis-ci.org/metafacture/metafacture-fix.svg?branch=master)](https://travis-ci.org/metafacture/metafacture-fix)

Setup
-----

Go to the Xtext parent project:

`cd metafacture-fix/org.metafacture.fix.parent`

Run the tests (see `FixParsingTest.xtend`):

`./gradlew test`

Editor
------

Start the server:

`./gradlew jettyRun`

Visit [http://localhost:8080/](http://localhost:8080/), and paste this into the editor:

```
# FIX is a macro-language for data transformations
			
# Simple fixes

add_field(hello,world)
remove_field(my.deep.nested.junk)
copy_field(stats,output.$append)

# Conditionals

if exists(error)
	set_field(is_valid, no)
	log(error)
elsif exists(warning)
	set_field(is_valid, yes)
	log(warning)
else
	set_field(is_valid, yes)
end

# Loops

do list(path)
	add_field(foo,bar)
end

# Nested expressions

do marc_each()
	if marc_has(f700)
		marc_map(f700a,authors.$append)
	end
end
```

Content assist is triggered with Ctrl-Space. The input above is also used in `FixParsingTest.xtend`.
