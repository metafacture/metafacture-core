/**
 * 
 */
package org.culturegraph.mf.stream.converter;

import static org.junit.Assert.*;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author schaeferd
 * @date 09.04.2015 
 * @description 
 */
public class RdfEncoderTest {
	
	private RdfEncoder rdfEncoder;
	private static final String SUBJECTURI = "http://example.org/123"; 
	private static final String PREDICATEURI = "http://example.org/stuff/1.0/name";
	private static final String OBJECTURI = "Peter";
	private static final String BLANKNODEID = "http://example.org/bnode123456oido";
	
	@Mock
	private ObjectReceiver<Statement> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		rdfEncoder = new RdfEncoder();
		rdfEncoder.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		rdfEncoder.closeStream();
	}
	
	@Test
	public void shouldOutputLiteralAsTriple() {
		rdfEncoder.startRecord(SUBJECTURI);
		rdfEncoder.literal(PREDICATEURI, OBJECTURI);
		rdfEncoder.endRecord();

		verify(receiver).process(buildStatement(SUBJECTURI, PREDICATEURI, OBJECTURI));
	
	}
	
	@Test
	public void shouldOutputBlankNodeAsTriple() {
		rdfEncoder.startRecord(BLANKNODEID);
		rdfEncoder.literal("~rdf:resource", OBJECTURI);
		rdfEncoder.endRecord();
		
		//LOG.info(Statement);

		verify(receiver).process(buildStatement(BLANKNODEID, "~rdf:resource", OBJECTURI));
	
	}
	
	public Statement buildStatement(String uri, String predicate, String object) {
		
		Statement returnStatement;
		
		ValueFactoryImpl factory = ValueFactoryImpl.getInstance();
		returnStatement = factory.createStatement(factory.createURI(uri), factory.createURI(predicate), factory.createLiteral(object));
		
		
		return returnStatement;
	}

}
