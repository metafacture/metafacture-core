package org.metafacture.fix.interpreter;

import org.metafacture.fix.fix.Do;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.fix.If;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.metamorph.Metafix;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;
import org.eclipse.xtext.xbase.lib.InputOutput;

public class FixInterpreter extends XbaseInterpreter {

    private Metafix metafix;

    public FixInterpreter() {
    }

    public void run(final Metafix metafix, final EObject program) { // checkstyle-disable-line HiddenField
        if (metafix != null && program != null) {
            this.metafix = metafix;
            if (program instanceof Fix) {
                final EList<Expression> elements = ((Fix) program).getElements();
                for (final Expression expression : elements) {
                    this.process(expression);
                }
            }
        }
    }

    public void process(final Expression expression) { // checkstyle-disable-line CyclomaticComplexity|NPathComplexity
        this.metafix.getExpressions().add(expression);
        boolean matched = false;
        if (expression instanceof If) {
            matched = true;
            InputOutput.<String>println("if: " + expression);
            final EList<Expression> elements = ((If) expression).getElements();
            for (final Expression ifExpression : elements) {
                this.process(ifExpression);
            }
            if (((If) expression).getElseIf() != null) {
                InputOutput.<String>println("else if: " + ((If) expression).getElseIf());
                for (final Expression elseIfExpression : ((If) expression).getElseIf().getElements()) {
                    this.process(elseIfExpression);
                }
            }
            if (((If) expression).getElse() != null) {
                InputOutput.<String>println("else: " + ((If) expression).getElseIf());
                for (final Expression elseExpression : ((If) expression).getElse().getElements()) {
                    this.process(elseExpression);
                }
            }
        }
        if (!matched) {
            if (expression instanceof Do) {
                matched = true;
                InputOutput.<String>println("do: " + expression);
                for (final Expression doExpression : ((Do) expression).getElements()) {
                    this.process(doExpression);
                }
            }
        }
        if (!matched) {
            if (expression instanceof MethodCall) {
                matched = true;
                InputOutput.<String>println("method call: " + expression);
            }
        }
    }

}
