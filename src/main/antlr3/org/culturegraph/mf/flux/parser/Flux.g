/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
grammar Flux;

options {
  language     = Java;
  output       = AST;
  ASTLabelType = CommonTree;
}

tokens {
  ARG;
  ASSIGN;
  DEFAULT;
  TEE;
  SUBFLOW;
  QualifiedName;
  StartString;
}

@header {
package org.culturegraph.mf.flux.parser;
}

@lexer::header {
package org.culturegraph.mf.flux.parser;
}

flux
  :
  varDef* flow*
  ;

varDef
  :
  'default ' i=Identifier '=' exp ';'
    ->
      ^(DEFAULT Identifier exp)
  | i=Identifier '=' exp ';'
    ->
      ^(ASSIGN Identifier exp)
  ;

flow
  :
  (
    StdIn
    | exp
    | Wormhole
  )
  '|'! flowtail ('|'! Wormhole)? ';'!
  ;

tee
  :
  ('{' flowtail '}')+
    ->
      ^(
        TEE
        ^(SUBFLOW flowtail)+
       )
  ;

flowtail
  :
  (
    pipe
    | tee
  )
  (
    '|'!
    (
      pipe
      | tee
    )
  )*
  ;

StdIn
  :
  '>'
  ;

pipe
  :
  qualifiedName ('(' pipeArgs ')')?
    ->
      ^(QualifiedName[$qualifiedName.text] pipeArgs*)
  ;

exp
  :
  atom ('+'^ atom)*
  ;

atom
  :
  StringLiteral
  | Identifier
  ;

pipeArgs
  :
  (
    exp
    | exp ','! VarRef
    | VarRef
    | namedArg
  )
  (','! namedArg)*
  ;

namedArg
  :
  Identifier '=' exp
    ->
      ^(ARG Identifier exp)
  ;

qualifiedName
  :
  Identifier ('.' Identifier)*
  ;

VarRef
  :
  '*'
  ;

Identifier
  :
  Letter
  (
    Letter
    | Digit
  )*
  ;

Wormhole
  :
  '@' id=Identifier
                   {
                    setText(id.getText());
                   }
  ;

StringLiteral
  @init { final StringBuilder stringBuilder = new StringBuilder(); }
  :
  '"'
  (
    EscapeSequence[stringBuilder]
    | normal = ~('\\'|'"') { stringBuilder.appendCodePoint(normal); }
  )*
  '"'
  { setText(stringBuilder.toString()); }
  ;

fragment
EscapeSequence[StringBuilder stringBuilder]
  :
  '\\'
  (
  NamedEscape[stringBuilder]
  | UnicodeEscape[stringBuilder]
  | OctalEscape[stringBuilder]
  )
  ;

fragment
NamedEscape[StringBuilder stringBuilder]
  : 'b' { stringBuilder.append('\b'); }
  | 't' { stringBuilder.append('\t'); }
  | 'n' { stringBuilder.append('\n'); }
  | 'f' { stringBuilder.append('\f'); }
  | 'r' { stringBuilder.append('\r'); }
  | '"' { stringBuilder.append('"'); }
  | '\'' { stringBuilder.append('\''); }
  | '\\' { stringBuilder.append('\\'); }
  ;

fragment
OctalEscape[StringBuilder stringBuilder]
  @init {}
  : ( digit1=LeadingOctalDigit digit2=OctalDigit digit3=OctalDigit
  | digit2=OctalDigit digit3=OctalDigit
  | digit3=OctalDigit
  ) {
    String octalString =
     ( digit1 != null ? digit1.getText() : "") +
     ( digit2 != null ? digit2.getText() : "") +
     digit3.getText();
    stringBuilder.appendCodePoint(Integer.valueOf(octalString, 8));
  }
  ;

fragment
LeadingOctalDigit
  : '0'..'3'
  ;

fragment
OctalDigit
  : '0'..'7'
  ;

fragment
UnicodeEscape[StringBuilder stringBuilder]
  : 'u' digit1=HexDigit digit2=HexDigit digit3=HexDigit digit4=HexDigit
  {
    final String hexString = digit1.getText() + digit2.getText() +
        digit3.getText() + digit4.getText();
    stringBuilder.appendCodePoint(Integer.valueOf(hexString, 16));
  }
  ;

fragment
HexDigit
  : '0'..'9'
  | 'a'..'f'
  | 'A'..'F'
  ;

fragment
Letter
  :
  '\u0024'
  | '\u0041'..'\u005a'
  | '\u005f'
  | '\u0061'..'\u007a'
  | '\u00c0'..'\u00d6'
  | '\u00d8'..'\u00f6'
  | '\u00f8'..'\u00ff'
  | '\u0100'..'\u1fff'
  | '\u3040'..'\u318f'
  | '\u3300'..'\u337f'
  | '\u3400'..'\u3d2d'
  | '\u4e00'..'\u9fff'
  | '\uf900'..'\ufaff'
  | '-'
  ;

fragment
Digit
  :
  '0'..'9'
  ;

LINE_COMMENT
  :
  '//'
  ~(
    '\n'
    | '\r'
   )*
  (
    '\r'? '\n'
    | EOF
  )

  {
    $channel = HIDDEN;
  }
  ;

WS
  :
  (
    ' '
    | '\r'
    | '\t'
    | '\u000C'
    | '\n'
  )

  {
   $channel = HIDDEN;
  }
  ;
