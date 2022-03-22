" Only load this indent file when no other was loaded yet.
if exists("b:did_indent")
  finish
endif
let b:did_indent = 1

setlocal indentexpr=GetMetafactureFixIndent()
setlocal indentkeys+=0=do,0=if,0=unless,0=elsif,0=else,0=end

let b:undo_indent = "setlocal indentexpr< indentkeys<"

if exists("*GetMetafactureFixIndent")
  finish
endif

function! GetMetafactureFixIndent()
  let lnum = prevnonblank(v:lnum - 1)

  if lnum == 0
    return 0
  endif

  if synIDattr(synID(v:lnum, 1, 1), "name") =~ '^fixComment*'
    return indent(v:lnum)
  endif

  let new_indent = indent(lnum)

  if getline(lnum) =~ '^\s*\(\(do\|if\|unless\|elsif\)\>\|else$\)\|($'
    let new_indent += shiftwidth()
  endif

  if getline(v:lnum) =~ '^\s*\(elsif\>\|\(else\|end\|)\)$\)'
    let new_indent -= shiftwidth()
  endif

  return new_indent
endfunction
