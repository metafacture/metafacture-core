package org.metafacture.fix.tests;

import org.metafacture.fix.fix.Fix;

import com.google.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
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
    public void load0() throws Exception {
        final StringConcatenation builder = new StringConcatenation();
        builder.append("map(a,b)");
        this.loadModel(builder.toString());
    }

    @Test
    public void load1() throws Exception {
        final StringConcatenation builder = new StringConcatenation();
        builder.append("# simple field name mappings");
        builder.newLine();
        builder.newLine();
        builder.append("map(a,b)");
        builder.newLine();
        builder.newLine();
        builder.append("# nested field structure");
        builder.newLine();
        builder.newLine();
        builder.append("map(e1)");
        builder.newLine();
        builder.append("map(e1.e2)");
        builder.newLine();
        builder.append("map(e1.e2.d)");
        builder.newLine();
        builder.newLine();
        builder.append("# pass-through for unmapped fields");
        builder.newLine();
        builder.newLine();
        builder.append("map(_else)");
        builder.newLine();
        this.loadModel(builder.toString());
    }

    @Test // checkstyle-disable-line ExecutableStatementCount|JavaNCSS
    public void load2() throws Exception {
        final StringConcatenation builder = new StringConcatenation();
        builder.append("# FIX is a macro-language for data transformations");
        builder.newLine();
        builder.newLine();
        builder.append("# Simple fixes");
        builder.newLine();
        builder.newLine();
        builder.append("add_field(hello,world)");
        builder.newLine();
        builder.append("remove_field(my.deep.nested.junk)");
        builder.newLine();
        builder.append("copy_field(stats,output.$append)");
        builder.newLine();
        builder.newLine();
        builder.append("# Conditionals");
        builder.newLine();
        builder.newLine();
        builder.append("if exists(error)");
        builder.newLine();
        builder.append("\t");
        builder.append("set_field(is_valid, no)");
        builder.newLine();
        builder.append("\t");
        builder.append("log(error)");
        builder.newLine();
        builder.append("elsif exists(warning)");
        builder.newLine();
        builder.append("\t");
        builder.append("set_field(is_valid, yes)");
        builder.newLine();
        builder.append("\t");
        builder.append("log(warning)");
        builder.newLine();
        builder.append("else");
        builder.newLine();
        builder.append("\t");
        builder.append("set_field(is_valid, yes)");
        builder.newLine();
        builder.append(LITERAL_END);
        builder.newLine();
        builder.newLine();
        builder.append("# Loops");
        builder.newLine();
        builder.newLine();
        builder.append("do list(path)");
        builder.newLine();
        builder.append("\t");
        builder.append("add_field(foo,bar)");
        builder.newLine();
        builder.append(LITERAL_END);
        builder.newLine();
        builder.newLine();
        builder.append("# Nested expressions");
        builder.newLine();
        builder.newLine();
        builder.append("do marc_each()");
        builder.newLine();
        builder.append("\t");
        builder.append("if marc_has(f700)");
        builder.newLine();
        builder.append("\t\t");
        builder.append("marc_map(f700a,authors.$append)");
        builder.newLine();
        builder.append("\t");
        builder.append(LITERAL_END);
        builder.newLine();
        builder.append(LITERAL_END);
        builder.newLine();
        this.loadModel(builder.toString());
    }

    public void loadModel(final String fix) throws Exception {
        final Fix result = this.parseHelper.parse(fix);
        InputOutput.<String>println("Result: " + result);
        Assertions.assertNotNull(result);
        final EList<Resource.Diagnostic> errors = result.eResource().getErrors();
        final StringConcatenation builder = new StringConcatenation();
        builder.append("Unexpected errors: ");
        builder.append(IterableExtensions.join(errors, ", "));
        Assertions.assertTrue(errors.isEmpty(), builder.toString());
    }

}
