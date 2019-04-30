package org.metafacture.fix.interpreter

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter
import org.metafacture.fix.fix.Do
import org.metafacture.fix.fix.Expression
import org.metafacture.fix.fix.Fix
import org.metafacture.fix.fix.If
import org.metafacture.fix.fix.MethodCall
import org.metafacture.metamorph.Metafix

class FixInterpreter extends XbaseInterpreter {

	Metafix metafix

	def run(Metafix metafix, EObject program) {
		if (metafix !== null && program !== null) {
			this.metafix = metafix
			if (program instanceof Fix) {
				for (expression : program.elements) {
					process(expression)
				}
			}
		}
	}

	def void process(Expression expression) {
		metafix.expressions += expression
		switch expression {
			If: {
				println("if: " + expression)
				for (ifExpression : expression.elements) {
					process(ifExpression)
				}
				if (expression.elseIf !== null) {
					println("else if: " + expression.elseIf)
					for (elseIfExpression : expression.elseIf.elements) {
						process(elseIfExpression)
					}
				}
				if (expression.^else !== null) {
					println("else: " + expression.elseIf)
					for (elseExpression : expression.^else.elements) {
						process(elseExpression)
					}
				}
			}
			Do: {
				println("do: " + expression)
				for (doExpression : expression.elements) {
					process(doExpression)
				}
			}
			MethodCall: {
				println("method call: " + expression)
			}
		}
	}

}
