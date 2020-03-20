package org.metafacture.fix.tests;

import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.interpreter.FixInterpreter;
import org.metafacture.metamorph.Metafix;

import com.google.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(FixInjectorProvider.class)
public class InterpreterTest {

    @Inject
    @Extension
    private FixInterpreter fixInterpreter;

    @Inject
    @Extension
    private ParseHelper<Fix> parseHelper;

    public InterpreterTest() {
    }

    @Test
    public void testSimpleProgram() throws Exception {
        final Metafix metafix = ObjectExtensions.<Metafix>operator_doubleArrow(new Metafix(), (Metafix it) -> {
            Assert.assertTrue(it.getExpressions().isEmpty());
        });
        final StringConcatenation builder = new StringConcatenation();
        builder.append("add_field(hello,world)");
        builder.newLine();
        final Fix program = this.parseHelper.parse(builder);
        this.fixInterpreter.run(metafix, program);
        ObjectExtensions.<Metafix>operator_doubleArrow(metafix, (Metafix it) -> {
            Assert.assertEquals(1, it.getExpressions().size());
        });
        InputOutput.<String>println("metafix expressions: " + metafix.getExpressions());
    }

    @Test
    public void testNestedProgram() throws Exception {
        final Metafix metafix = new Metafix();
        final StringConcatenation builder = new StringConcatenation();
        builder.append("do marc_each()");
        builder.newLine();
        builder.append("\t");
        builder.append("if marc_has(f700)");
        builder.newLine();
        builder.append("\t\t");
        builder.append("marc_map(f700a,authors.$append)");
        builder.newLine();
        builder.append("\t");
        builder.append("end");
        builder.newLine();
        builder.append("end");
        builder.newLine();
        final Fix program = this.parseHelper.parse(builder);
        this.fixInterpreter.run(metafix, program);
        Assert.assertEquals(3, metafix.getExpressions().size()); // checkstyle-disable-line MagicNumber
        InputOutput.<String>println("metafix expressions: " + metafix.getExpressions());
    }

}
