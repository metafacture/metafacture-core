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
