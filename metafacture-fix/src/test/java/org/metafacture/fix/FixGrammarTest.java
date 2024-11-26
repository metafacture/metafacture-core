/*
 * Copyright 2024 Fabian Steeg, hbz
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metafacture.fix;

import static org.junit.Assert.assertEquals;

import org.metafacture.fix.parser.FixLexer;
import org.metafacture.fix.parser.FixParser;
import org.metafacture.fix.parser.RecordTransformer;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Tests for the Fix grammar.
 *
 * @author Fabian Steeg
 */
public final class FixGrammarTest {

    @Test
    public void parseSimpleFix() {
        assertEquals("add_field ( test )", parse("add_field(test)"));
    }

    @Test
    public void parseSimpleFixWithOption() {
        assertEquals("add_field ( test1 , option : test2 )", parse("add_field(test1,option:test2)"));
    }

    @Test
    public void parseFixWithIf() {
        assertEquals("if some ( test1 ) add ( something ) elsif some ( test2 ) add ( somethingElse , opt : one ) else add ( anotherThing , opt : two ) end",
                parse(
                    "if some(test1)",
                    "  add(something)",
                    "elsif some(test2)",
                    "  add(somethingElse, opt: one)",
                    "else",
                    "  add(anotherThing, opt: two)",
                    "  # comment",
                    "end"));
    }

    @Test
    public void parseError() {
        assertEquals("<unexpected: [@3,14:14='<EOF>',<-1>,1:14], resync=add_field(> <mismatched token: [@3,14:14='<EOF>',<-1>,1:14], resync=test>",
                parse("add_field(test"));
    }

    private String parse(final String... fixLines) {
        final String input = String.join("\n", fixLines);
        try {
            RecordTransformer transformer = Metafix.compile(createInputStream(input), null);
            System.out.println("Created RecordTransformer for fix: " + transformer);
            return parseToStringTree(input);
        } catch (IOException | RecognitionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseToStringTree(final String input) throws IOException, RecognitionException {
        final ByteArrayInputStream fixInput = createInputStream(input);
        final FixLexer lexer = new FixLexer(new ANTLRInputStream(fixInput));
        final FixParser parser = new FixParser(new CommonTokenStream(lexer));
        return ((Tree) parser.fix().getTree()).toStringTree();
    }

    private ByteArrayInputStream createInputStream(final String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

}
