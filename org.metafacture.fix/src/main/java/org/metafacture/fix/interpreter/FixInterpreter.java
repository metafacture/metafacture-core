package org.metafacture.fix.interpreter;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.metafacture.fix.fix.Do;
import org.metafacture.fix.fix.ElsIf;
import org.metafacture.fix.fix.Else;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.fix.If;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.metamorph.Metafix;

@SuppressWarnings("all")
public class FixInterpreter extends XbaseInterpreter {
  private Metafix metafix;

  public void run(final Metafix metafix, final EObject program) {
    if (((metafix != null) && (program != null))) {
      this.metafix = metafix;
      if ((program instanceof Fix)) {
        EList<Expression> _elements = ((Fix)program).getElements();
        for (final Expression expression : _elements) {
          this.process(expression);
        }
      }
    }
  }

  public void process(final Expression expression) {
    this.metafix.expressions.add(expression);
    boolean _matched = false;
    if (expression instanceof If) {
      _matched=true;
      InputOutput.<String>println(("if: " + expression));
      EList<Expression> _elements = ((If)expression).getElements();
      for (final Expression ifExpression : _elements) {
        this.process(ifExpression);
      }
      ElsIf _elseIf = ((If)expression).getElseIf();
      boolean _tripleNotEquals = (_elseIf != null);
      if (_tripleNotEquals) {
        ElsIf _elseIf_1 = ((If)expression).getElseIf();
        String _plus = ("else if: " + _elseIf_1);
        InputOutput.<String>println(_plus);
        EList<Expression> _elements_1 = ((If)expression).getElseIf().getElements();
        for (final Expression elseIfExpression : _elements_1) {
          this.process(elseIfExpression);
        }
      }
      Else _else = ((If)expression).getElse();
      boolean _tripleNotEquals_1 = (_else != null);
      if (_tripleNotEquals_1) {
        ElsIf _elseIf_2 = ((If)expression).getElseIf();
        String _plus_1 = ("else: " + _elseIf_2);
        InputOutput.<String>println(_plus_1);
        EList<Expression> _elements_2 = ((If)expression).getElse().getElements();
        for (final Expression elseExpression : _elements_2) {
          this.process(elseExpression);
        }
      }
    }
    if (!_matched) {
      if (expression instanceof Do) {
        _matched=true;
        InputOutput.<String>println(("do: " + expression));
        EList<Expression> _elements = ((Do)expression).getElements();
        for (final Expression doExpression : _elements) {
          this.process(doExpression);
        }
      }
    }
    if (!_matched) {
      if (expression instanceof MethodCall) {
        _matched=true;
        InputOutput.<String>println(("method call: " + expression));
      }
    }
  }
}
