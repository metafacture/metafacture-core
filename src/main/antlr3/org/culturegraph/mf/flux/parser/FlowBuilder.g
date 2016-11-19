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
tree grammar FlowBuilder;

options {
  language     = Java;
  tokenVocab   = Flux;
  ASTLabelType = CommonTree;
}

@header {
package org.culturegraph.mf.flux.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.culturegraph.mf.flux.parser.FluxProgramm;
import org.culturegraph.mf.flux.FluxParseException;
}

@members {
private FluxProgramm flux = new FluxProgramm();
private Map<String, String> vars = new HashMap<String, String>();

public final void addVaribleAssignements(final Map<String, String> vars) {
	this.vars.putAll(vars);
}
}

flux returns [FluxProgramm retValue = flux]
  :
  varDefs
  (
    flow
        {
         flux.nextFlow();
        }
  )*

  {
   flux.compile();
  }
  ;

flow
  :
  (
    StdIn
         {
          flux.setStdInStart();
         }
    | e=exp
           {
            flux.setStringStart($e.value);
           }
    | ws=Wormhole
                 {
                  flux.setWormholeStart($ws.text);
                 }
  )
  flowtail
  (
    we=Wormhole
               {
                flux.setWormholeEnd($we.text);
               }
  )?
  ;

varDefs
  :
  varDef*
  ;

varDef
  :
  ^(ASSIGN name=Identifier e=exp?)

  {
   vars.put($name.text, $e.value);
  }
  |
  ^(DEFAULT name=Identifier e=exp?)

  {
   if (!vars.containsKey($name.text)) {
   	vars.put($name.text, $e.value);
   }
  }
  ;

tee
  :
  ^(
    TEE
       {
        flux.startTee();
        //System.out.println("start tee");
       }
    (
      ^(SUBFLOW flowtail)

      {
       flux.endSubFlow();
       // System.out.println("end subflow");
      }
    )+
   )

  {
   flux.endTee();
   //System.out.println("end tee");
  }
  ;

flowtail
  :
  (
    pipe
    | tee
  )+
  ;

exp returns [String value]
  :
  s=StringLiteral
                 {
                  $value = $s.text;
                 }
  | id=Identifier
                 {
                  $value = vars.get($id.text);
                  if ($value == null) {
                  	throw new FluxParseException("Variable " + $id.text + " not assigned.");
                  }
                 }
  |
  ^('+' e1=exp e2=exp)

  {
   $value = $e1.value + $e2.value;
  }
  ;

pipe
@init {
final Map<String, String> namedArgs = new HashMap<String, String>();
final List<Object> cArgs = new ArrayList<Object>();
}
  :
  ^(
    name=QualifiedName
    (
      e=exp
           {
            cArgs.add($e.value);
           }
    )?
    (
      VarRef
            {
             cArgs.add(Collections.unmodifiableMap(vars));
            }
    )?
    (
      a=arg
           {
            namedArgs.put($a.key, $a.value);
           }
    )*
   )

  {
   flux.addElement($name.text, namedArgs, cArgs);
  }
  ;

arg returns [String key, String value]
  :
  ^(ARG k=Identifier e=exp)

  {
   $key = $k.text;
   $value = $e.value;
  }
  ;
