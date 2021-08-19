package org.metafacture.metafix.jvmmodel;

import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.fix.Fix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Extension;

import java.util.Arrays;
import javax.inject.Inject;

public class FixJvmModelInferrer extends AbstractModelInferrer {

    @Inject
    @Extension
    private JvmTypesBuilder jvmTypesBuilder;

    @Inject
    @Extension
    private IQualifiedNameProvider iQualifiedNameProvider;

    public FixJvmModelInferrer() {
    }

    public void infer(final EObject fix, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPrelinkingPhase) {
        if (fix == null) {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.asList(fix, acceptor, isPrelinkingPhase).toString());
        }

        infer(fix instanceof Fix ? (Fix) fix : fix, acceptor, isPrelinkingPhase);
    }

    private void infer(final Fix fix, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPrelinkingPhase) {
        acceptor.accept(jvmTypesBuilder.toClass(fix, iQualifiedNameProvider.getFullyQualifiedName(fix)), (JvmGenericType it) -> {
            jvmTypesBuilder.setDocumentation(it, jvmTypesBuilder.getDocumentation(fix));
            jvmTypesBuilder.operator_add(it.getSuperTypes(), _typeReferenceBuilder.typeRef(Metafix.class));
        });
    }

}
