/*  Copyright 2013 Pascal Christoph, hbz
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.culturegraph.mf.stream.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.source.Opener;
import org.xml.sax.SAXException;

import ORG.oclc.oai.harvester2.app.RawWrite;

/**
 * Opens an OAI-PMH stream and passes a reader to the receiver.
 * 
 * @author Pascal Christoph (dr0i)
 * 
 */
@Description("Opens an OAI-PMH stream and passes a reader to the receiver. Mandatory arguments are: BASE_URL, DATE_FROM, DATE_UNTIL, METADATA_PREFIX, SET_SPEC .")
@In(String.class)
@Out(java.io.Reader.class)
public final class OaiPmhOpener extends
		DefaultObjectPipe<String, ObjectReceiver<Reader>> implements Opener {

	private String encoding = "UTF-8";

	final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();

	private String dateFrom;

	private String dateUntil;

	private String setSpec;

	private String metadataPrefix;

	/**
	 * Default constructor
	 */
	public OaiPmhOpener() {

	}

	/**
	 * Sets the encoding to use. The default setting is UTF-8.
	 * 
	 * @param encoding new default encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Sets the beginning of the retrieving of updated data. The form is
	 * YYYY-MM-DD .
	 * 
	 * @param dateFrom The form is YYYY-MM-DD .
	 */
	public void setDateFrom(final String dateFrom) {
		this.dateFrom = dateFrom;
	}

	/**
	 * Sets the end of the retrieving of updated data. The form is YYYY-MM-DD .
	 * 
	 * @param dateUntil The form is YYYY-MM-DD .
	 */
	public void setDateUntil(final String dateUntil) {
		this.dateUntil = dateUntil;
	}

	/**
	 * Sets the OAI-PM metadata prefix .
	 * 
	 * @param metadataPrefix the OAI-PM metadata prefix
	 */
	public void setMetadataPrefix(final String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

	/**
	 * Sets the OAI-PM set specification .
	 * 
	 * @param setSpec th OAI-PM set specification
	 */
	public void setSetSpec(final String setSpec) {
		this.setSpec = setSpec;
	}

	@Override
	public void process(final String baseUrl) {

		try {
			RawWrite.run(baseUrl, this.dateFrom, this.dateUntil, this.metadataPrefix,
					this.setSpec, OUTPUT_STREAM);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			getReceiver().process(
					new InputStreamReader(new ByteArrayInputStream(OUTPUT_STREAM
							.toByteArray()), encoding));
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}
}
