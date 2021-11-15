# About

This is early work in progress towards tools and an implementation of the Fix language for Metafacture as an alternative to configuring data transformations with [Metamorph](https://github.com/metafacture/metafacture-core/wiki#morph). The basic idea is to map constructs from Catmandu::Fix like [functions](https://github.com/LibreCat/Catmandu/wiki/Functions), [selectors](https://github.com/LibreCat/Catmandu/wiki/Selectors) and [binds](https://github.com/LibreCat/Catmandu/wiki/Binds) to equivalent constructs from Metafacture like Metamorph [functions](https://github.com/metafacture/metafacture-core/wiki/Metamorph-functions) and collectors ([wiki](https://github.com/metafacture/metafacture-core/wiki/Metamorph-collectors), [commit](https://github.com/metafacture/metafacture-core/commit/0530d6ad72ced992b479bff94d6f56bbef77bb2d)).

See [https://github.com/elag/FIG](https://github.com/elag/FIG)

# State

[![Build and Deploy](https://github.com/metafacture/metafacture-fix/workflows/Build%20and%20Deploy/badge.svg)](https://github.com/metafacture/metafacture-fix/actions?query=workflow%3A%22Build+and+Deploy%22)

This repo contains an Xtext web project with a basic Fix grammar, which generates a parser, a web editor, and a language server. The repo also contains an extension for VS code/codium based on that language server. The web editor UI contains input fields for sample data and a [Flux](https://github.com/metafacture/metafacture-core/wiki#flux) definition to run workflows with the given Fix. A test deployment of the web server is available at: [http://test.lobid.org/fix](http://test.lobid.org/fix).

# Setup

If you're using Windows, configure git option core.autocrlf before cloning repository:
`git config core.autocrlf false`
Otherwise git will change all line endings to Windows crlf when you check out code (and vice versa) but that will lead to failures with gradle's check task.

## Clone

Clone the Git repository:

`git clone https://github.com/metafacture/metafacture-fix.git`

Go to the Git repository root:

`cd metafacture-fix/`

Run the tests (in `metafix/src/test/java`) and checks (`.editorconfig`, `config/checkstyle/checkstyle.xml`):

`./gradlew clean check`

(To import the projects in Eclipse, choose File > Import > Existing Gradle Project and select the `metafacture-fix` directory.)

## Extension

The project `metafix-vsc` provides an extension for Visual Studio Code / Codium for `fix` via the language server protocol (LSP). In the current state the extension supports auto completion, simple syntax highlighting and auto closing brackets and quotes. This project was created using this [tutorial](https://www.typefox.io/blog/building-a-vs-code-extension-with-xtext-and-the-language-server-protocol) and the corresponding [example](https://github.com/TypeFox/languageserver-example).


Build extension:

1. Install Visual Studio Code / alternative: VS Codium
2. Install Node.js (including npm)
3. In metafacture-fix execute:
Unix: `./gradlew installServer`
Windows: `.\gradlew.bat installServer`
4. In `metafix-vsc/` execute (tip: if you use windows, install cygwin to execute npm commands):
`npm install`

To start the extension in development mode (starting a second code/codium instance), follow A. To create an vsix file to install the extension permanently follow B.

A) Run in dev mode:
1. Open `metafix-vsc/` in Visual Studio Code / Codium
2. Launch vscode extension by pressing F5 (opens new window of Visual Studio Code)
3. Open new file (file-ending `.fix`) or open existing fix-file (see sample below)

B) Install vsix file:
1. Install vsce: `npm install -g vsce`
2. In `metafix-vsc/` execute: `vsce package`
vsce will create a vsix file in the vsc directory which can be used for installation:
3. Open VS Code / Codium
4. Click 'Extensions' section
5. Click menu bar and choose 'Install from VSIX...'



## Web Server

Start the web server:

`./gradlew jettyRun`

Visit [http://localhost:8080/](http://localhost:8080/), and paste this into the editor:

```
# Fix is a macro-language for data transformations

# Simple fixes

add_field(hello,"world")
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

Content assist is triggered with Ctrl-Space. The input above is also used in `FixParsingTest.java`.

Run workflows on the web server, passing `data`, `flux`, and `fix`:

[http://localhost:8080/xtext-service/run?data='1'{'a': '5', 'z': 10}&flux=as-lines|decode-formeta|fix|encode-formeta(style="multiline")&fix=map(a,b) map(_else)](http://localhost:8080/xtext-service/run?data=%271%27{%27a%27:%20%275%27,%20%27z%27:%2010}&flux=as-lines|decode-formeta|fix|encode-formeta(style=%22multiline%22)&fix=map(a,c)%20map(_else))

## Module

The repo contains and uses a new `Metafix` stream module for Metafacture which plays the role of the `Metamorph` module in Fix-based Metafacture workflows. For the current implementation of the `Metafix` stream module see `MetafixDslTest.java`. For a real-world usage sample see [https://gitlab.com/oersi/oersi-etl/-/blob/develop/data/production/edu-sharing.fix](https://gitlab.com/oersi/oersi-etl/-/blob/develop/data/production/edu-sharing.fix).

# Xtext

This repo has been originally set up with Xtext 2.17.0 and Eclipse for Java 2019-03, following [https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html](https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html).
