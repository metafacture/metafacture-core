# About

This is early work in progress towards tools and an implementation of the Fix language for Metafacture as an alternative to configuring data transformations with [Metamorph](https://github.com/metafacture/metafacture-core/wiki#morph). Inspired by [Catmandu::FIX](https://github.com/LibreCat/Catmandu/) Metafix processes metadata not as a data stream but as whole records. The basic idea is to rebuild constructs from Catmandu::Fix like [functions](https://github.com/LibreCat/Catmandu/wiki/Functions), [selectors](https://github.com/LibreCat/Catmandu/wiki/Selectors) and [binds](https://github.com/LibreCat/Catmandu/wiki/Binds) in Java and add additional functionalities that the Metamorph toolbox.

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

The repo contains and uses a new `Metafix` stream module for Metafacture which plays the role of the `Metamorph` module in Fix-based Metafacture workflows. For the current implementation of the `Metafix` stream module see `MetafixDslTest.java`. For a real-world usage sample see [https://gitlab.com/oersi/oersi-etl/-/blob/master/data/production/openRub/openRub.fix](https://gitlab.com/oersi/oersi-etl/-/blob/master/data/production/openRub/openRub.fix).

# Xtext

This repo has been originally set up with Xtext 2.17.0 and Eclipse for Java 2019-03, following [https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html](https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html).


## Functions and Cookbook

### Best practices and guidelines for working with Metafacture Fix

- We recommend to use double quotation marks for arguments and values in functions, binds and conditionals.
- If using a `list` bind with a variable, the `var` option requires quotation marks (`do list(path: "<sourceField>", "var": "<variableName>")`).
- Fix turns repeated fields into arrays internally but only marked arrays (with `[]` at the end of the field name) are also emitted as "arrays" (entities with indexed literals), all other arrays are emitted as repeated fields.
- Every Fix file should end with a final newline.

### Functions

#### `include`

---- TODO: THIS NEEDS MORE CONTENT -----

#### `nothing`

Does nothing. It is used for benchmarking in Catmandu.

```perl
nothing()
```

#### `put_filemap`

Defines an external map for lookup from a file.

```perl
put_filemap("<sourceFile>", "<mapName>", sep_char: "\t")
```

The separator (`sep_char`) will vary depending on the source file, e.g.:

| Type | Separator  |
|------|------------|
| CSV  | `,` or `;` |
| TSV  | `\t`       |

#### `put_map`

Defines an internal map for lookup from key/value pairs.

```perl
put_map("<mapName>",
  "dog": "mammal",
  "parrot": "bird",
  "shark": "fish"
)
```

#### `put_var`

Defines a single internal variable that can be referenced with `$[<variableName>]`.

```perl
put_var("<variableName>", "<variableValue>")
```

#### `put_vars`

Defines multiple internal variables that can be referenced with `$[<variableName>]`.

```perl
put_vars(
  "<variableName_1>": "<variableValue_1>",
  "<variableName_2>": "<variableValue_2>"
)
```

#### `add_field`

Creates (or appends to) a field with a defined value.

```perl
add_field("<targetFieldName>", "<fieldValue>")
```

#### `set_field`

Creates (or replaces) a field with a defined value.

```perl
set_field("<targetFieldName>", "<fieldValue>")
```

#### `copy_field`

Copies (or appends to) a field from an existing field.

```perl
copy_field("<sourceField>", "<targetField>")
```

#### `move_field`

Moves (or appends to) a field from an existing field. Can be used to rename a field.

```perl
move_field("<sourceField>", "<targetField>")
```

#### `remove_field`

Removes a field.

```perl
remove_field("<sourceField>")
```

#### `set_array`

Creates a new array (with optional values).

```perl
set_array("<targetFieldName>")
set_array("<targetFieldName>", "<value_1>"[, ...])
```

#### `set_hash`

Creates a new hash (with optional values).

```perl
set_hash("<targetFieldName>")
set_hash("<targetFieldName>", "subfieldName": "<subfieldValue>"[, ...])
```

#### `retain`

Deletes all fields except the ones listed (incl. subfields).

```perl
retain("<sourceField_1>"[, ...])
```

#### `rename`

Replaces a regular expression pattern in subfield names of a field. Does not change the name of the source field itself.

```perl
rename("<sourceField>", "<regexp>", "<replacement>")
```

#### `array`

Converts a hash/object into an array.

```perl
array("<sourceField>")
```

E.g.:

```perl
array("foo")
# {"name":"value"} => ["name", "value"]
```

#### `hash`

Converts an array into a hash/object.

```perl
hash("<sourceField>")
```

E.g.:
```perl
hash("foo")
# ["name", "value"] => {"name":"value"}
```

#### `format`

Replaces the value with a formatted (`sprintf`-like) version.

---- TODO: THIS NEEDS MORE CONTENT -----

```perl
format("<sourceField>", "<formatString>")
```

#### `parse_text`

Parses a text into an array or hash of values.

---- TODO: THIS NEEDS MORE CONTENT -----

```perl
parse_text("<sourceField>", "<parsePattern>")
```

#### `paste`

Joins multiple field values into a new field. Can be combined with additional literal strings.

The default `join_char` is a single space. Literal strings have to start with `~`.

```perl
paste("<targetField>", "<sourceField_1>"[, ...][, join_char: ", "])
```

E.g.:

```perl
# a: eeny
# b: meeny
# c: miny
# d: moe
paste("my.string", "~Hi", "a", "~how are you?")
# "my.string": "Hi eeny how are you?"
```

#### `random`

Creates (or replaces) a field with a random number (less than the specified maximum).

```perl
random("<targetField>", "<maximum>")
```

#### `vacuum`

Deletes empty fields, arrays and objects.

```perl
vacuum()
```

#### `append`

Adds a string at the end of a field value.

```perl
append("<sourceField>", "<appendString>")
```

#### `prepend`

Adds a string at the beginning of a field value.

```perl
prepend("<sourceField>", "<prependString>")
```

#### `filter`

Only keeps field values that match the regular expression pattern.

```perl
filter("<sourceField>", "<regexp>")
```

#### `capitalize`

Upcases the first character in a field value.

```perl
capitalize("<sourceField>")
```

#### `downcase`

Downcases all characters in a field value.

```perl
downcase("<sourceField>")
```

#### `upcase`

Upcases all characters in a field value.

```perl
upcase("<sourceField>")
```

#### `count`

Counts the number of elements in an array or a hash and replaces the field value with this number.

```perl
count("<sourceField>")
```

#### `index`

Returns the index position of a substring in a field and replaces the field value with this number.

```perl
index("<sourceField>", "<substring>")
```

#### `join_field`

Joins an array of strings into a single string.

```perl
join_field("<sourceField>", "<separator>")
```

#### `lookup`

Looks up matching values in a map and replaces the field value with this match. External files as well as internal maps can be used.

```perl
lookup("<sourceField>", "<mapFile>", sep_char: ”,”)
lookup("<sourceField>", "<mapName>")
lookup("<sourceField>", "<mapName>", default: "NA")
```

#### `replace_all`

Replaces a regular expression pattern in field values with a replacement string. Regexp capturing is possible; refer to capturing groups by number (`$<number>`) or name (`${<name>}`).

```perl
replace_all("<sourceField>", "<regexp>", "<replacement>")
```

#### `reverse`

Reverses the character order of a string or the element order of an array.

```perl
reverse("<sourceField>")
```

#### `sort_field`

Sorts strings in an array. Alphabetically and A-Z by default. Optional numerical and reverse sorting.

```perl
sort_field("<sourceField>")
sort_field("<sourceField>", reverse: "true")
sort_field("<sourceField>", numeric: "true")
```

#### `split_field`

Splits a string into an array and replaces the field value with this array.

```perl
split_field("<sourceField>", "<separator>")
```

#### `substring`

Replaces a string with its substring as defined by the start and end positions.

```perl
substring("<sourceField>", "<startPosition>", "<endPosition>")
```

#### `sum`

Sums numbers in an array and replaces the field value with this number.

```perl
sum("<sourceField>")
```

#### `trim`

Deletes whitespace at the beginning and the end of a field value.

```perl
trim("<sourceField>")
```

#### `uniq`

Deletes duplicate values in an array.

```perl
uniq("<sourceField>")
```

### Selectors

#### `reject`

Ignores records that match a condition.

```perl
if <condition>
  reject()
end
```

### Binds

#### `do list`

Iterates over each element of an array. In contrast to Catmandu, it can also iterate over a single object or string.

```perl
do list(path: "<sourceField>")
  ...
end
```

Only the current element is accessible in this case (as the root element).

When specifying a variable name for the current element, the record remains accessible as the root element and the current element is accessible through the variable name:

```perl
do list(path: "<sourceField>", "var": "<variableName>")
  ...
end
```

### Conditionals

Conditionals start with `if` in case of affirming the condition or `unless` rejecting the condition.

Conditionals require a final `end`.

Additional conditionals can be set with `elsif` and `else`.

```perl
if <condition(params, ...)>
  ...
end
```

```perl
unless <condition(params, ...)>
  ...
end
```

```perl
if <condition(params, ...)>
  ...
elsif
  ...
else
  ...
end
```

#### `exists`

Executes the functions if/unless the field exists.

```perl
if exists("<sourceField>")
```

#### `contain`

##### `all_contain`

Executes the functions if/unless the field contains the value. If it is an array or a hash all field values must contain the string.

##### `any_contain`

Executes the functions if/unless the field contains the value. If it is an array or a hash one or more field values must contain the string.

##### `none_contain`

Executes the functions if/unless the field does not contain the value. If it is an array or a hash none of the field values may contain the string.

#### `equal`

##### `all_equal`

Executes the functions if/unless the field value equals the string. If it is an array or a hash all field values must equal the string.

##### `any_equal`

Executes the functions if/unless the field value equals the string. If it is an array or a hash one or more field values must equal the string.

##### `none_equal`

Executes the functions if/unless the field value does not equal the string. If it is an array or a hash none of the field values may equal the string.

#### `match`

##### `all_match`

Executes the functions if/unless the field value matches the regular expression pattern. If it is an array or a hash all field values must match the regular expression pattern.

##### `any_match`

Executes the functions if/unless the field value matches the regular expression pattern. If it is an array or a hash one or more field values must match the regular expression pattern.

##### `none_match`

Executes the functions if/unless the field value does not match the regular expression pattern. If it is an array or a hash none of the field values may match the regular expression pattern.

## Glossary

### Array-Wildcards

Array-Wildcards resemble [Catmandus concept of wildcards](http://librecat.org/Catmandu/#wildcards).

When working with arrays and repeated fields you can use wildcards to select all or certain elements of an array as well as select an additional new element.
You use them instead of the index number. These can also be used (some only) when generating new elements of an array.
- `*`: selects all elements of an array
- `$first`: selects only the first element of an array
- `$last`: selects only the last element of an array
- `$prepend`: selects the position infront of the first element array. This can be used, when generating new elements in an array.
- `$append`: selects the position behind the last element of an array. This can be used, when generating new elements in an array.

### General path wildcards

General path wildcards resemble [Metamorphs concept of wildcards](https://github.com/metafacture/metafacture-core/wiki/Metamorph-User-Guide#addressing-pieces-of-data)

Beside the array-wildcards you can use other general wildcards to select variations of an path. These wildcards cannot be used to generating new elements.
These wildcards are no part of the Catmandu Fix. They cannot be used (yet?) to select variation of whole paths but element names.

- `*` is a placeholder for for unlimited characters
- `?` is a placeholder for a single arbitrary character
- `|` allows for multiple versions either of the whole path or of parts, when used in a group `(...|...)`
- `[...]` can be used as placeholder for distinct characters
