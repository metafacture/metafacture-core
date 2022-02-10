
## Functions and Cookbook

### Thinks to remember when working with fix

- We recommend to use double quotationmarks for every argument and attribute-values in functions, binds and conditionals.
- If using an `list`-bind with an variable. The `"var"`-attribute need quotation marks.
- Every fix needs a trailing line-break.


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

#### array

Turns hash/object into an array.


#### copy_field

#### format

#### hash

#### move_field

#### parse_text

#### paste

#### random

#### reject

#### remove_field

#### rename

#### retain

#### set_array

#### set_field

#### set_hash

#### vacuum

#### append

#### capitalize

#### count

#### downcase

#### filter

#### index

#### join_field

#### lookup

#### prepend

#### replace_all

#### reverse

#### sort_field

#### split_field

#### substring

#### sum

#### trim

#### uniq

#### upcase

### Binds

#### do list

####

### Conditionals

#### contain

#### equal

#### exists

#### match

####

