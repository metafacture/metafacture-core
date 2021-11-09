if exists("b:current_syntax")
  finish
endif

syn keyword fixBind bind do doset
syn keyword fixConditional else elsif if unless
syn keyword fixKeyword block end
syn keyword fixOperator and or
syn keyword fixPreProc use
syn keyword fixSelector reject select
syn match fixBareString /\v[^[:space:]\\,;:=>()"'\$*]+/
syn match fixComment /\v(#|\/\/).*$/
syn match fixFunction /\v([a-z][_0-9a-zA-Z]*\.)*[a-z][_0-9a-zA-Z]*\s*\(/me=e-1,he=e-1
syn match fixOperator /\v(\&\&|\|\|)/
syn match fixWildcard /\v\$(append|first|last|prepend)>/
syn match fixWildcard /\v\*/
syn region fixCommentRegion start="\v\/\*" skip=/\v\\./ end="\v\*\/"
syn region fixDoubleQuotedString start=/\v"/ skip=/\v\\./ end=/\v"/
syn region fixIfBlock start="if" end="end" fold transparent
syn region fixSingleQuotedString start=/\v'/ skip=/\v\\./ end=/\v'/

hi link fixBareString String
hi link fixBind Keyword
hi link fixComment Comment
hi link fixCommentRegion Comment
hi link fixConditional Keyword
hi link fixDoubleQuotedString String
hi link fixFunction Function
hi link fixKeyword Keyword
hi link fixOperator Operator
hi link fixPreProc PreProc
hi link fixSelector Keyword
hi link fixSingleQuotedString String
hi link fixWildcard Special

let b:current_syntax = "metafacture-fix"
