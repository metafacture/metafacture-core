package org.metafacture.metafix;

import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.interpreter.FixInterpreter;
import org.metafacture.metafix.tests.FixInjectorProvider;

import com.google.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.InputOutput;
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
    public void shouldInterpretSimple() throws Exception {
        interpret(1,
                "add_field(hello,world)"
        );
    }

    @Test
    public void shouldInterpretNested() throws Exception {
        interpret(3,
                "do marc_each()",
                "\tif marc_has(f700)",
                "\t\tmarc_map(f700a,authors.$append)",
                "\tend",
                "end",
                ""
        );
    }

    private void interpret(final int expressions, final String... fix) throws Exception {
        final Metafix metafix = new Metafix();
        Assert.assertTrue(metafix.getExpressions().isEmpty());

        fixInterpreter.run(metafix, parseHelper.parse(String.join("\n", fix)));
        InputOutput.println("metafix expressions: " + metafix.getExpressions());
        Assert.assertEquals(expressions, metafix.getExpressions().size());
    }

}
