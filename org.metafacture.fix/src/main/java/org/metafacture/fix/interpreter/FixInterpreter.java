package org.metafacture.fix.interpreter;

import org.metafacture.fix.fix.Do;
import org.metafacture.fix.fix.ElsIf;
import org.metafacture.fix.fix.Else;
import org.metafacture.fix.fix.Expression;
import org.metafacture.fix.fix.Fix;
import org.metafacture.fix.fix.If;
import org.metafacture.fix.fix.MethodCall;
import org.metafacture.metamorph.Metafix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;
import org.eclipse.xtext.xbase.lib.InputOutput;

public class FixInterpreter extends XbaseInterpreter {

    private Metafix metafix;

    public FixInterpreter() {
    }

    public void run(final Metafix metafixParam, final EObject program) {
        if (metafixParam != null && program != null) {
            metafix = metafixParam;

            if (program instanceof Fix) {
                for (final Expression expression : ((Fix) program).getElements()) {
                    process(expression);
                }
            }
        }
    }

    private void process(final Expression expression) { // checkstyle-disable-line CyclomaticComplexity|NPathComplexity
        metafix.getExpressions().add(expression);

        boolean matched = false;

        if (expression instanceof If) {
            matched = true;

            final If ifExpression = (If) expression;
            InputOutput.println("if: " + ifExpression);

            for (final Expression element : ifExpression.getElements()) {
                process(element);
            }

            final ElsIf elseIfExpression = ifExpression.getElseIf();
            if (elseIfExpression != null) {
                InputOutput.println("else if: " + elseIfExpression);

                for (final Expression element : elseIfExpression.getElements()) {
                    process(element);
                }
            }

            final Else elseExpression = ifExpression.getElse();
            if (elseExpression != null) {
                InputOutput.println("else: " + elseExpression);

                for (final Expression element : elseExpression.getElements()) {
                    process(element);
                }
            }
        }

        if (!matched) {
            if (expression instanceof Do) {
                matched = true;

                final Do doExpression = (Do) expression;
                InputOutput.println("do: " + doExpression);

                for (final Expression element : doExpression.getElements()) {
                    process(element);
                }
            }
        }

        if (!matched) {
            if (expression instanceof MethodCall) {
                matched = true;
                InputOutput.println("method call: " + expression);
                // TODO
            }
        }
    }

}
