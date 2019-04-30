package org.metafacture.fix.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.^extension.ExtendWith
import org.metafacture.fix.fix.Fix
import org.metafacture.metamorph.Metafix

import static org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.metafacture.fix.interpreter.FixInterpreter

@ExtendWith(InjectionExtension)
@InjectWith(FixInjectorProvider)
class InterpreterTest { 
	
	@Inject extension FixInterpreter
	@Inject extension ParseHelper<Fix>
	
	@Test def void testSimpleProgram() {
		val metafix = new Metafix => [
			assertTrue(expressions.isEmpty)
		]
		val program = '''
			add_field(hello,world)
		'''.parse
		metafix.run(program)
		metafix => [
			assertEquals(1, expressions.size)
		]
		println("metafix expressions: " + metafix.expressions)
	}  
	
	@Test def void testNestedProgram() {
		val metafix = new Metafix
		val program = '''
			do marc_each()
				if marc_has(f700)
					marc_map(f700a,authors.$append)
				end
			end
		'''.parse
		metafix.run(program)
		assertEquals(3, metafix.expressions.size)
		println("metafix expressions: " + metafix.expressions)
	}
	
}
