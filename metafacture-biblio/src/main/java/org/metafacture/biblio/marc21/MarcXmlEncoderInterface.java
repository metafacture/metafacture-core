package org.metafacture.biblio.marc21;

public interface MarcXmlEncoderInterface {

    /**
     * Sets the flag to decide whether to emit the {@value MarcXmlEncoder#NAMESPACE_NAME}
     * namespace
     *
     * @param emitNamespace true if the namespace is emitted, otherwise false
     */
     void setEmitNamespace(final boolean emitNamespace);

    /**
     * Sets the flag to decide whether to omit the XML declaration.
     *
     * <strong>Default value: {@value MarcXmlEncoder#OMIT_XML_DECLARATION}</strong>
     *
     * @param currentOmitXmlDeclaration true if the XML declaration is omitted, otherwise
     *                                  false
     */
     void omitXmlDeclaration(final boolean currentOmitXmlDeclaration);

    /**
     * Sets the XML version.
     *
     * <strong>Default value: {@value MarcXmlEncoder#XML_VERSION}</strong>
     *
     * @param xmlVersion the XML version
     */
     void setXmlVersion(final String xmlVersion);

    /**
     * Sets the XML encoding.
     *
     * <strong>Default value: {@value MarcXmlEncoder#XML_ENCODING}</strong>
     *
     * @param xmlEncoding the XML encoding
     */
     void setXmlEncoding(final String xmlEncoding);

    /**
     * Formats the resulting xml by indentation. Aka "pretty printing".
     *
     * <strong>Default value: {@value MarcXmlEncoder#PRETTY_PRINTED}</strong>
     *
     * @param formatted true if formatting is activated, otherwise false
     */
     void setFormatted(final boolean formatted);
}