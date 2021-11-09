package org.metafacture.metafix;

import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.tests.FixInjectorProvider;

import com.google.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(FixInjectorProvider.class)
public class FixParsingTest {

    private static final String LITERAL_END = "end";

    @Inject
    private ParseHelper<Fix> parseHelper;

    public FixParsingTest() {
    }

    @Test
    public void shouldParseSimple() throws Exception {
        parse(
                "map(a,b)"
        );
    }

    @Test
    public void shouldParseQuotedStrings() throws Exception {
        parse(
                "add_field(hello,'world')",
                "add_field(hello,\"world\")",
                "add_field(hello,\"w-o:r l/d\")",
                "add_field(hello,'\tw\n\torld')",
                "add_field(hello,'\\tw\\n\\torld')",
                "add_field(hello,'\"world\"')"
        );
    }

    @Test
    public void shouldParseMappings() throws Exception {
        parse(
                "# simple field name mappings",
                "",
                "map(a,b)",
                "",
                "# nested field structure",
                "",
                "map(e1)",
                "map(e1.e2)",
                "map(e1.e2.d)",
                "",
                "# pass-through for unmapped fields",
                "",
                "map(_else)",
                ""
        );
    }

    @Test
    public void shouldParseTransformations() throws Exception {
        parse(
                "# FIX is a macro-language for data transformations",
                "",
                "# Simple fixes",
                "",
                "add_field(hello,\"world\")",
                "remove_field(my.deep.nested.junk)",
                "copy_field(stats,output.$append)",
                "",
                "# Conditionals",
                "",
                "if exists(error)",
                "\tset_field(is_valid, no)",
                "\tlog(error)",
                "elsif exists(warning)",
                "\tset_field(is_valid, yes)",
                "\tlog(warning)",
                "else",
                "\tset_field(is_valid, yes)",
                LITERAL_END,
                "",
                "# Loops",
                "",
                "do list(path)",
                "\tadd_field(foo,bar)",
                LITERAL_END,
                "",
                "# Nested expressions",
                "",
                "do marc_each()",
                "\tif marc_has(f700)",
                "\t\tmarc_map(f700a,authors.$append)",
                "\t" + LITERAL_END,
                LITERAL_END,
                ""
        );
    }

    private void parse(final String... fix) throws Exception {
        final Fix result = parseHelper.parse(String.join("\n", fix));
        InputOutput.println("Result: " + result);
        Assertions.assertNotNull(result);

        final EList<Resource.Diagnostic> errors = result.eResource().getErrors();
        Assertions.assertTrue(errors.isEmpty(), "Unexpected errors: " + IterableExtensions.join(errors, ", "));
    }

}
