package org.metafacture.fix.jvmmodel;

import org.metafacture.fix.fix.Fix;
import org.metafacture.metamorph.Metafix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

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

    protected void infer(final Fix fix, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPrelinkingPhase) {
        final Procedure1<JvmGenericType> function = (JvmGenericType it) -> {
            this.jvmTypesBuilder.setDocumentation(it, this.jvmTypesBuilder.getDocumentation(fix));
            this.jvmTypesBuilder.<JvmTypeReference>operator_add(it.getSuperTypes(), this._typeReferenceBuilder.typeRef(Metafix.class));
        };
        acceptor.<JvmGenericType>accept(this.jvmTypesBuilder.toClass(fix, this.iQualifiedNameProvider.getFullyQualifiedName(fix)), function);
    }

    public void infer(final EObject fix, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPrelinkingPhase) {
        if (fix == null) {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(fix, acceptor, isPrelinkingPhase).toString());
        }

        infer(fix instanceof Fix ? (Fix) fix : fix, acceptor, isPrelinkingPhase);
    }

}
