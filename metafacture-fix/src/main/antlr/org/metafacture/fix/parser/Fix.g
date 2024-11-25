/*
 *  Copyright 2024 Fabian Steeg, hbz
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

grammar Fix;

options {
language = Java;
output = AST;
ASTLabelType = CommonTree;
}

@header {
package org.metafacture.fix.parser;
}

@lexer::header {
package org.metafacture.fix.parser;
}

fix	: expression*;

expression	: (doExpr | unless | ifExpr | methodCall | COMMENT) WS*;

methodCall	: ID '(' (ID|option) (',' (ID|option))* ')';

doExpr	: 'do' methodCall expression* 'end';

unless	: 'unless' methodCall expression* 'end';

ifExpr	: 'if' methodCall expression* elsIf* elseExpr? 'end';

elsIf	: 'elsif' methodCall expression*;

elseExpr	: 'else' expression*;

option	: ID ':' ID;

ID	: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

COMMENT	: '#' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;};

WS	: (' '| '\t'| '\r'| '\n') {$channel=HIDDEN;};
