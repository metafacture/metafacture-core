package org.metafacture.metafix;

import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.interpreter.FixInterpreter;
import org.metafacture.metafix.tests.FixInjectorProvider;

import com.google.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Extension;
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
                "do list(path:'700??','var':'$i')",
                "\tif any_equal('$i.4','aut')",
                "\t\tcopy_field($i.a,authors.$append)",
                "\tend",
                "end",
                ""
        );
    }

    private void interpret(final int expressions, final String... fix) throws Exception {
        final Metafix metafix = new Metafix();
        Assert.assertTrue(metafix.getExpressions().isEmpty());

        fixInterpreter.run(metafix, parseHelper.parse(String.join("\n", fix)));
        Assert.assertEquals("metafix expressions: " + metafix.getExpressions(), expressions, metafix.getExpressions().size());
    }

}
