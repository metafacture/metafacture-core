package org.metafacture.fix.tests;

import com.google.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.interpreter.FixInterpreter;
import org.metafacture.fix.tests.FixInjectorProvider;
import org.metafacture.metamorph.Metafix;

@ExtendWith(InjectionExtension.class)
@InjectWith(FixInjectorProvider.class)
@SuppressWarnings("all")
public class InterpreterTest {
  @Inject
  @Extension
  private FixInterpreter _fixInterpreter;

  @Inject
  @Extension
  private ParseHelper<Fix> _parseHelper;

  @Test
  public void testSimpleProgram() {
    try {
      Metafix _metafix = new Metafix();
      final Procedure1<Metafix> _function = (Metafix it) -> {
        Assert.assertTrue(it.expressions.isEmpty());
      };
      final Metafix metafix = ObjectExtensions.<Metafix>operator_doubleArrow(_metafix, _function);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("add_field(hello,world)");
      _builder.newLine();
      final Fix program = this._parseHelper.parse(_builder);
      this._fixInterpreter.run(metafix, program);
      final Procedure1<Metafix> _function_1 = (Metafix it) -> {
        Assert.assertEquals(1, it.expressions.size());
      };
      ObjectExtensions.<Metafix>operator_doubleArrow(metafix, _function_1);
      InputOutput.<String>println(("metafix expressions: " + metafix.expressions));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testNestedProgram() {
    try {
      final Metafix metafix = new Metafix();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("do marc_each()");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("if marc_has(f700)");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("marc_map(f700a,authors.$append)");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("end");
      _builder.newLine();
      _builder.append("end");
      _builder.newLine();
      final Fix program = this._parseHelper.parse(_builder);
      this._fixInterpreter.run(metafix, program);
      Assert.assertEquals(3, metafix.expressions.size());
      InputOutput.<String>println(("metafix expressions: " + metafix.expressions));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
