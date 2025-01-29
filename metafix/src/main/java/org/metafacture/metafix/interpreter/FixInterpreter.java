package org.metafacture.metafix.interpreter;

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.fix.Do;
import org.metafacture.metafix.fix.ElsIf;
import org.metafacture.metafix.fix.Else;
import org.metafacture.metafix.fix.Expression;
import org.metafacture.metafix.fix.Fix;
import org.metafacture.metafix.fix.If;
import org.metafacture.metafix.fix.MethodCall;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FixInterpreter extends XbaseInterpreter {

    private static final Logger LOG = LoggerFactory.getLogger(FixInterpreter.class);

    private Metafix metafix;

    /**
     * Creates an instance of {@link FixInterpreter}.
     */
    public FixInterpreter() {
    }

    /**
     * Interprets the Fix program.
     *
     * @param metafixParam the Metafix instance
     * @param program      the Fix instance
     */
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
            LOG.debug("if: " + ifExpression);

            for (final Expression element : ifExpression.getElements()) {
                process(element);
            }

            final List<ElsIf> elseIfExpressions = ifExpression.getElseIf();
            for (final ElsIf elseIfExpression : elseIfExpressions) {
                LOG.debug("else if: " + elseIfExpression);

                for (final Expression element : elseIfExpression.getElements()) {
                    process(element);
                }
            }

            final Else elseExpression = ifExpression.getElse();
            if (elseExpression != null) {
                LOG.debug("else: " + elseExpression);

                for (final Expression element : elseExpression.getElements()) {
                    process(element);
                }
            }
        }

        if (!matched) {
            if (expression instanceof Do) {
                matched = true;

                final Do doExpression = (Do) expression;
                LOG.debug("do: " + doExpression);

                for (final Expression element : doExpression.getElements()) {
                    process(element);
                }
            }
        }

        if (!matched) {
            if (expression instanceof MethodCall) {
                matched = true;
                LOG.debug("method call: " + expression);
                // TODO
            }
        }
    }

}
