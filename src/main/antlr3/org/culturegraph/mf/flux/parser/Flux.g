/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
import org.apache.commons.lang.StringEscapeUtils;
}

flux
  :
  varDef* mainflow
  // {
  //System.out.println($mainflow.tree.toStringTree());
  // }
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

mainflow
  :
  (
    StdIn
    | exp
  )
  '|'! flow ';'!
  ;

tee
  :
  ('{' flow '}')+
    ->
      ^(
        TEE
        ^(SUBFLOW flow)+
       )
  ;

flow
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

StringLiteral
  :
  '"'
  (
    EscapeSequence
    |
    ~(
      '\\'
      | '"'
     )
  )*
  '"' 
      {
       // strip the quotes from the resulting token and unescape
       setText(StringEscapeUtils.unescapeJava(getText().substring(1,
       		getText().length() - 1)));
      }
  ;

fragment
EscapeSequence
  :
  '\\'
  (
    'b'
    | 't'
    | 'n'
    | 'f'
    | 'r'
    | '\"'
    | '\''
    | '\\'
  )
  | UnicodeEscape
  | OctalEscape
  ;

fragment
OctalEscape
  :
  '\\' ('0'..'3') ('0'..'7') ('0'..'7')
  | '\\' ('0'..'7') ('0'..'7')
  | '\\' ('0'..'7')
  ;

fragment
UnicodeEscape
  :
  '\\' 'u' HexDigit HexDigit HexDigit HexDigit
  ;

fragment
HexDigit
  :
  (
    '0'..'9'
    | 'a'..'f'
    | 'A'..'F'
  )
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
  '\r'? '\n' 
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
