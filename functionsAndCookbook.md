
## Functions and Cookbook

### Thinks to remember when working with fix

- We recommend to use double quotationmarks for every argument and attribute-values in functions, binds and conditionals.
- If using an `list`-bind with an variable. The `"var"`-attribute need quotation marks.
- Every fix needs a trailing line-break.
- Fix turns internally repeated fields into array but outputs only marked arrays (with `[]` at the end of the fieldname) as array all others only as repeated fields.

### Functions

#### `include`

#### `nothing`

The function does nothing. It is used for benchmarking in Catmandu.

`nothing()`


#### `put_filemap`

Defines a external map which can be used for lookup.

`put_filemap("[source-file]", "[mapVariable]", sep_char:"\t")`

`sep_char` can vary due to the source-file:
tsv: `\t`
csv: `;` or `,`

#### `put_map`

Creates an internal map for lookup as list of key-value-pairs.

```
put_map("[mapVariable]",
  "dog":"mammal",
  "parrot":"bird",
  "shark":"fish"
	)
```

#### `put_var`

Creates a single internal variables, that can be resused with `$[[variableName]]`

`put_var("[variableName]", "[variableValue]")`

#### `put_vars`

Creates a list of multiple internal variables, that can be resused with `$[[variableName]]`

`put_vars("[variableName_1]": "[variableValue_1]", "[variableName_2]": "[variableValue_2]")`

___________________________________

#### `add_field` / `set_field`

Generates a new simple `string`-field with an defined value.

`add_field("[targetFieldName]", "[fieldValue]")`
`set_field("[targetFieldName]", "[fieldValue]")`

#### `copy_field`

Copy field and generate a new one.

`copy_field("[sourceField]", "[targetField]")`

#### `move_field`

Move field and generate a new one. Can also be used to rename a certain field.

`move_field("[sourceField]", "[targetField]")`

#### `remove_field`

Removes selected field.

`remove_field("[sourceField]")`

#### `set_array`

Generates a new array with an defined value but can be empty too.

`set_array("[targetFieldName]")`

`set_array("[targetFieldName]", "[value_1]" [, ...])`

#### `set_hash`

Generates a new hash with an defined value but can be empty too.

`set_hash("[targetFieldName]")`

`set_hash("[targetFieldName]", "subfieldName": "[subfieldValue]" [, ...])`
#### `retain`

Deletes all not named fields and only keeps selected fields incl. subfields.

`retain("[sourceField_1]", ... , "[sourceField_n]")`

#### `rename`

Replaces regex-pattern in subfield names of selected source field. Does not change the pattern of the selected field itself.

`rename("[sourceField]","[regexp]","[substitut-string]")`

#### `array`

Turns hash/object into an array.

`array("[sourceField]")`

e.g.:
`array("foo")`

`foo => {"name":"value"}` => `[ "name" , "value" ]`

#### `hash`

Turns array into hash/object.

`hash("[sourceField]")`

e.g.:
`hash("foo")`
`foo =>  [ "name" , "value" ]` => ` {"name":"value"}`


#### `format`

Replace the value with a formatted (sprintf-like) version.

---- TODO: THIS NEEDS MORE CONTENT -----

`format("[sourceField]", "[formatString]")`

#### `parse_text`

Parses a text into an array or hash of values

---- TODO: THIS NEEDS MORE CONTENT -----

`parse_text("[sourceField]", "[parsePattern]")`

#### `paste`

Joins multiple field values into a newly generated field. Can be combined with additional custom strings.

`paste("[targetField]", "[sourceField_1]", "[sourceField_2]" [,[...],"[sourceField_n]", join_char:", "])`

Default `join_char` is a single space.
Custom strings need to start with `~`

e.g.:
in:

```
a: eeny
b: meeny
c: miny
d: moe
```

`paste("my.string","~Hi","a","~how are you?")`

out:
`my.string: Hi eeny how are you?`

#### `random`

Generates a new field with an random value up to a defined number.
Replaces existing fields.

`random("[targetField]", "[number]")`

#### `vacuum`
Deletes empty fields, arrays and objects.

`vacuum()`

#### `append`

Adds string at the end of field value.

`append("[sourceField]","[appendString]")`

#### `prepend`

Adds string at the beginning of field value.

`prepend("[sourceField]","[appendString]")`

#### `filter`

Only keeps fields with field values, that matches filterpattern.
Filter pattern can be the regexp or simple string.

`filter("[sourceField]","[filterPattern]")`

#### `capitalize`

Capitalizes all characters in field value.

`capitalize("[sourceField]")`

#### `downcase`

Downcases first character in field value.

`downcase("[sourceField]")`

#### `upcase`

Upcases first character in field value.

`upcase("[sourceField]")`

#### `count`

Counts numbers of elements in array or in hash.

`count("[sourceField]")`

#### `index`

Returns index position of defined value in array.

`index("[sourceField]", ("[value]")`

#### `join_field`

Joins array of strings to a single field.

`join_field("[sourceArray]", "[sep_characters]")`

#### lookup

Looks up matching values in map file. External file as well as internal defined maps can be used. `default` sets

`lookup("[sourceField]","[mapFile]",sep_char:”,”)`

`lookup("[sourceField]","[mapVariable]")`

`lookup("[sourceField]","[mapVariable]", default:"NA")`

`lookup("[sourceField]","[mapVariable]", delete:”true”)`

#### `replace_all`

Replaces defined characters or regex-patterns in field value with defined values. Regex-Grouping is possible.

`replace_all("[sourceField]","[pattern]", "[value]")`

#### `reverse`

Reverses the character order of array or element order in hash or array.

`reverse("[sourceField]")`

#### sort_field

Sorts strings in array. Alphabetically and A-Z by default. Optional numerical annd reverse sorting.

`sort_field("[sourceField]")`

`sort_field("[sourceField]",reverse:"true")`

`sort_field("[sourceField]",numeric:"true")`


#### `split_field`

Splits simple field by defined seperation character.

`split_field("[sourceField]","[sepCharacter]")`

#### `substring`

Reduces field value to defined substring. Substring is defined by position index.

substring("[sourceField]","[startPosition]","[endPosition]")

#### `sum`

Sums values in an array.

`sum("[sourceField]")`

#### trim

Deletes spacing at the beginning and the end of a field value.

`trim("[sourceField]")`

#### `uniq`

Deletes duplicate values in array of strings.

`uniq("[sourceField]")`

___________________________________

### Selector

Sort introduction

#### reject

Ignores records that match the condition.
Can be written in short form

`reject [condition]`

But can also ignore all records. And be included in conditional:

```
if [condition]
    reject()
end
```


### Binds

Short introduction

#### do list

Iterates over each string or object of an array. In contrast to Catmandu it also can iterate over single object or string.

```
do list(path:"[sourceField]")
    [functions]
end
```
Select strings and subfields of objects in array in this scenario are selected with an starting `"."`. You can only change stuff in the object or the array.


```
do list(path:"[sourceField]", "var": "[pathVar]")
    [functions]
end
```

When setting a `"var"` you can also manipulate and change stuff outside the array-object / and string.

Instead of the simple `"."` one selects strings and objects of the array starting with the `"var"`-value.


#### TODO more binds???

### Conditionals

Conditionals start with `if` in case of affirming the condition or `unless` rejecting the condition.

Conditionals need a trainling `end`

Additional contitionals can be set with `elsif` and `else`.

```
if [condition(params,...)]
    fix(..)
    fix(..)
end
```

```
unless [condition(params,...)]
    fix(..)
    fix(..)
end
```

```
if [condition(params,...)]
    fix(..)
    fix(..)
else
    fix(..)
    fix(..)
end
```

```
if [condition(params,...)]
    fix(..)
    fix(..)
elsif
    fix(..)
    fix(..)
end
```

#### exists

Executes the functions if/unless the field exists.

`if exists("[sourceField]")`

#### contain

##### `all_contain`

Executes the functions if/unless the field contains the value. If it is an array or an hash all field values must contain the string.

##### `any_contain`

Executes the functions if/unless the field contains the value. If it is an array or an hash one or more field values contain the string.

##### `none_contain`

Executes the functions if/unless the field does not contain the value. If it is an array or an hash none of the field values contain the string.

#### equal

##### `all_equal`

Executes the functions if/unless the field value equals the string. If it is an array or an hash all field values must equal the string.

##### `any_equal`

Executes the functions if/unless the field equals the string. If it is an array or an hash one or more field values must equal the string.

##### `none_equal`

Executes the functions if/unless the field value does not equals the value. If it is an array or an hash none of the field values euquals the string.

#### match

##### `all_match`

Executes the functions if/unless the field value matches the regex-pattern. If it is an array or an hash all field values must match the regex-pattern.

##### `any_match`

Executes the functions if/unless the field matches the regex-pattern. If it is an array or an hash one or more field values must match the regex-pattern.


##### `none_match`

Executes the functions if/unless the field value does not match the regex-pattern.  the value. If it is an array or an hash none of the field values matches the regex-pattern.
