/**
 * 
 */
package org.culturegraph.mf.stream.converter;

import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author schaeferd
 * @date 09.04.2015 
 * @description 
 */
public final class RdfEncoder extends DefaultStreamPipe<ObjectReceiver<Statement>> {

	public static final char REFERENCE_MARKER = '*';
	public static final char LANGUAGE_MARKER = '$';
	public static final String RDF_REFERENCE = "~rdf:resource";
	public static final String RDF_ABOUT = "~rdf:about";
	public static final String XML_LANG = "~xml:lang";
	
	private final ValueFactory factory;
	private URI rdfAbout;
		
		
	public RdfEncoder(){
		try {
			factory = ValueFactoryImpl.getInstance();
		} catch (Exception e) {
			throw new MetafactureException(e);
		}
	}
	
	@Override
	public void startRecord(final String identifier) {
        rdfAbout = factory.createURI(identifier); 
        
	}

	@Override
	public void endRecord() {
		
		
	}

	@Override
	public void startEntity(final String name) {
		// Default implementation does nothing
	}

	@Override
	public void endEntity() {
		// Default implementation does nothing
	}

	@Override
	public void literal(final String name, final String value) {
		final int index = name.indexOf(LANGUAGE_MARKER);
		BNode blanknode = factory.createBNode();
		
		
		if (StringUtils.isNotEmpty(name) && name.charAt(0)==REFERENCE_MARKER) {
			startEntity(name.substring(1));
			literal(RDF_REFERENCE, blanknode.stringValue());
			endEntity();
		}
		else if(index>0){
			startEntity(name.substring(0,index));
			literal(XML_LANG, name.substring(index+1));
			literal("", value);
			endEntity();
		}
		else{
			Statement returnStat = factory.createStatement(rdfAbout, factory.createURI(name), factory.createLiteral(value));
			getReceiver().process(returnStat);
		}
		
		
		
		
		
	}
	
	
	
}
