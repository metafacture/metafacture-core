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
tree grammar RecordTransformerBuilder;

options {
language = Java;
tokenVocab = Fix;
ASTLabelType = CommonTree;
}

@header {
package org.metafacture.fix.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.metafacture.fix.parser.RecordTransformer;
}

@members {
private RecordTransformer transformer = new RecordTransformer();
}

transformer returns [RecordTransformer retValue = transformer] : expression*;

expression	: (doExpr | unless | ifExpr | methodCall | COMMENT) WS*;

methodCall	: method=ID '(' param=(ID|option) (',' (ID|option))* ')'
{
  transformer.processMethod($method.text, $param.text);
};

doExpr	: 'do' methodCall expression* 'end';

unless	: 'unless' methodCall expression* 'end';

ifExpr	: 'if' methodCall expression* elsIf* elseExpr? 'end';

elsIf	: 'elsif' methodCall expression*;

elseExpr: 'else' expression*;

option	: ID ':' ID;
