About
-----

This is early work in progress towards an implementation of the Fix language for Metafacture.

See [https://github.com/elag/FIG](https://github.com/elag/FIG)

State
-----

This repo contains an Xtext web project with a basic Fix grammar, which generates a parser and a web editor.

It also contains an interpreter to build `Metafix` objects from the Fix DSL. These are intended as a replacement for the `Metamorph` stream module in Metafacture workflows. In Flux workflows, the idea is to replace `morph(sample.xml)` with something like `fix(sample.fix)`.

[![Build Status](https://travis-ci.org/metafacture/metafacture-fix.svg?branch=master)](https://travis-ci.org/metafacture/metafacture-fix)

Setup
-----

Go to the Xtext parent project:

`cd metafacture-fix/org.metafacture.fix.parent`

Run the tests (in `org.metafacture.fix/src/test/java`):

`./gradlew clean test`

Editor
------

Start the server:

`./gradlew jettyRun`

Visit [http://localhost:8080/](http://localhost:8080/), and paste this into the editor:

```
# Fix is a macro-language for data transformations
			
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

Module
------

The `Metafix` stream module currently supports:

```
# simple field name mappings

map(a,b)

# nested field structure

map(e1)
map(e1.e2)
map(e1.e2.d)

# pass-through for unmapped fields

map(_else)
```

See also `MetafixDslTest.java`.

Workflows
--------

Run workflows, passing `data`, `flux`, and `fix`:

[http://localhost:8080/xtext-service/run?data='1'{'a': '5', 'z': 10}&flux=as-lines|decode-formeta|fix|encode-formeta(style="multiline")&fix=map(a,b) map(_else)](http://localhost:8080/xtext-service/run?data=%271%27{%27a%27:%20%275%27,%20%27z%27:%2010}&flux=as-lines|decode-formeta|fix|encode-formeta(style=%22multiline%22)&fix=map(a,c)%20map(_else))

Xtext
-----

To import the projects in Eclipse, choose File > Import > Existing Gradle Project:

![Import projects](docs/xtext-import.png)

This repo has been originally set up with Xtext 2.17.0 and Eclipse for Java 2019-03, following [https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html](https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html). Below are some details to reproduce the original setup:

In the New > Xtext Project wizard, the language details are specified:

![Language details](docs/xtext-setup-1.png)

As well as the project and build customization:

![Build details](docs/xtext-setup-2.png)
