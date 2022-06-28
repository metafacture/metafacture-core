# Metafacture Fix

Metafacture Fix (Metafix) is work in progress towards tools and an implementation of the Fix language for [Metafacture](https://metafacture.org/) as an alternative to configuring data transformations with [Metamorph](https://github.com/metafacture/metafacture-core/wiki#morph). Inspired by [Catmandu Fix](https://librecat.org/Catmandu/#fix-language), Metafix processes metadata not as a continuous data stream but as discrete records. The basic idea is to rebuild constructs from the (Catmandu) Fix language like [functions](https://librecat.org/Catmandu/#functions), [selectors](https://librecat.org/Catmandu/#selectors) and [binds](https://librecat.org/Catmandu/#binds) in Java and combine with additional functionalities from the Metamorph toolbox.

See also [Fix Interest Group](https://github.com/elag/FIG) for an initiative towards an implementation-independent specification for the Fix Language.

This repo contains the actual implementation of the Fix language as a Metafacture module and related components. It started as an [Xtext](#xtext) web project with a Fix grammar, from which a parser, a web editor, and a language server are generated. The repo also contains an extension for VS code/codium based on that language server. (The web editor has effectively been replaced by the [Metafacture Playground](https://metafacture.org/playground), but remains here for its integration into the language server, which [we want to move over](https://github.com/metafacture/metafacture-playground/issues?q=is%3Aissue+language+server+is%3Aopen) to the playground.)

## Setup

[![Build](https://github.com/metafacture/metafacture-fix/workflows/Build/badge.svg)](https://github.com/metafacture/metafacture-fix/actions?query=workflow%3A%22Build%22)

*Note: If you're using Windows, configure Git option `core.autocrlf` before cloning: `git config --global core.autocrlf false`.*

Clone the Git repository:

`git clone https://github.com/metafacture/metafacture-fix.git`

Go to the Git repository root:

`cd metafacture-fix/`

Run the tests (in `metafix/src/test/java`) and checks (`.editorconfig`, `config/checkstyle/checkstyle.xml`):

`./gradlew clean check`

(To import the projects in Eclipse, choose `File > Import > Existing Gradle Project` and select the `metafacture-fix` directory.)

## Usage

The repo contains and uses a new `Metafix` stream module for Metafacture which plays the role of the `Metamorph` module in Fix-based Metafacture workflows. For the current implementation of the `Metafix` stream module see the tests in `metafix/src/test/java`. To play around with some examples, check out the [Metafacture Playground](https://metafacture.org/playground). For real-world usage samples see [openRub.fix](https://gitlab.com/oersi/oersi-etl/-/blob/master/data/production/openRub/openRub.fix) and [duepublico.fix](https://gitlab.com/oersi/oersi-etl/-/blob/master/data/production/duepublico/duepublico.fix). For reference documentation, see [Functions and cookbook](#functions-and-cookbook).

### Extension

The project `metafix-vsc` provides an extension for Visual Studio Code / Codium for `fix` via the language server protocol (LSP). In the current state the extension supports auto completion, simple syntax highlighting and auto closing brackets and quotes. This project was created using this [tutorial](https://www.typefox.io/blog/building-a-vs-code-extension-with-xtext-and-the-language-server-protocol) and the corresponding [example](https://github.com/TypeFox/languageserver-example).

Build extension:

1. Install Visual Studio Code / alternative: VS Codium
2. Install Node.js (including npm)
3. In metafacture-fix execute:
Unix: `./gradlew installServer`
Windows: `.\gradlew.bat installServer`
4. In `metafix-vsc/` execute (tip: if you use windows, install cygwin to execute npm commands):
`npm install`

To start the extension in development mode (starting a second code/codium instance), follow A. To create a vsix file to install the extension permanently follow B.

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

### Web editor

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

[http://localhost:8080/xtext-service/run?data='1'{'a': '5', 'z': 10}&flux=as-lines|decode-formeta|fix|encode-formeta(style="multiline")&fix=map(a,b) map(\_else)](http://localhost:8080/xtext-service/run?data=%271%27{%27a%27:%20%275%27,%20%27z%27:%2010}&flux=as-lines|decode-formeta|fix|encode-formeta(style=%22multiline%22)&fix=map(a,c)%20map(\_else))

## Functions and cookbook

### Best practices and guidelines for working with Metafacture Fix

- We recommend to use double quotation marks for arguments and values in functions, binds and conditionals.
- If using a `list` bind with a variable, the `var` option requires quotation marks (`do list(path: "<sourceField>", "var": "<variableName>")`).
- Fix turns repeated fields into arrays internally but only marked arrays (with `[]` at the end of the field name) are also emitted as "arrays" (entities with indexed literals), all other arrays are emitted as repeated fields.
- Every Fix file should end with a final newline.

### Glossary

#### Array wildcards

Array wildcards resemble [Catmandu's concept of wildcards](http://librecat.org/Catmandu/#wildcards).

When working with arrays and repeated fields you can use wildcards instead of an index number to select elements of an array.

| Wildcard | Meaning |
|----------|:--------|
| `*` | Selects _all_ elements of an array. |
| `$first` | Selects only the _first_ element of an array. |
| `$last` | Selects only the _last_ element of an array. |
| `$prepend` | Selects the position _before_ the first element of an array. Can only be used when adding new elements to an array. |
| `$append` | Selects the position _after_ the last element of an array. Can only be used when adding new elements to an array. |

#### Path wildcards

Path wildcards resemble [Metamorph's concept of wildcards](https://github.com/metafacture/metafacture-core/wiki/Metamorph-User-Guide#addressing-pieces-of-data). They are not supported in Catmandu (it has [specialized Fix functions](https://librecat.org/Catmandu/#marc-mab-pica-paths) instead).

You can use path wildcards to select fields matching a pattern. They only match path _segments_ (field names), though, not _whole_ paths of nested fields. These wildcards cannot be used to add new elements.

| Wildcard | Meaning |
|----------|:--------|
| `*` | Placeholder for zero or more characters. |
| `?` | Placeholder for exactly one character. |
| `\|` | Alternation of multiple patterns. |
| `[...]` | Enumeration of characters. |

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

#### `timestamp`

Adds field or overwrites field with current timestamp.

Options:

  - `format` patterns as in [java.text.SimpleDateFormat](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html)
  - `timezone` as in [java.util.TimeZone](https://docs.oracle.com/javase/8/docs/api/java/util/TimeZone.html)  (Standard: UTC)
  - `language` area codes as in [java.util.Locale](https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html)

```perl
timestamp("<targetField>", [, format: "<formatPattern>"][, timezone: "<timezoneCode>"][, language: "<areaCode>"])
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

Replaces a string with its substring as defined by the start position (offset) and length.

```perl
substring("<sourceField>", "<startPosition>", "<length>")
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

## Xtext

This repo has been originally set up with [Xtext](https://www.eclipse.org/Xtext/) 2.17.0 and Eclipse for Java 2019-03, following [https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html](https://www.eclipse.org/Xtext/documentation/104_jvmdomainmodel.html).
