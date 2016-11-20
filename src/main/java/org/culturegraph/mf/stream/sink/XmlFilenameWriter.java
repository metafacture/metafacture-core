/* Copyright 2013 Pascal Christoph, hbz.
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
package org.culturegraph.mf.stream.sink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * A sink, writing an xml file. The filename is constructed from the xpath given
 * via setProperty().
 *
 * @author Pascal Christoph
 */
@Description("Writes the xml into the filesystem. The filename is constructed from the xpath given as 'property'.\n"
		+ " Variables are\n" + "- 'target' (determining the output directory)\n"
		+ "- 'property' (the element in the XML entity. Constitutes the main part of the file's name.)\n"
		+ "- 'startIndex' ( a subfolder will be extracted out of the filename. This marks the index' beginning )\n"
		+ "- 'stopIndex' ( a subfolder will be extracted out of the filename. This marks the index' end )\n")
@In(StreamReceiver.class)
@Out(Void.class)
@FluxCommand("write-xml-files")
public final class XmlFilenameWriter extends DefaultStreamPipe<ObjectReceiver<String>>
		implements FilenameExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(XmlFilenameWriter.class);
	private static final XPath xPath = XPathFactory.newInstance().newXPath();

	private final FilenameUtil filenameUtil = new FilenameUtil();
	private String compression;
	private Writer writer;
	private OutputStream out;

	/**
	 * Default constructor
	 */
	public XmlFilenameWriter() {
		setFileSuffix(".xml");
	}

	@Override
	public String getEncoding() {
		return this.filenameUtil.encoding;
	}

	@Override
	public void literal(final String str, final String xml) {
		String identifier = null;
		try {
			identifier = XmlFilenameWriter.xPath.evaluate(this.filenameUtil.property,
					new InputSource(new StringReader(xml)));
		} catch (final XPathExpressionException e2) {
			e2.printStackTrace();
		}
		if (identifier == null || identifier.length() < this.filenameUtil.endIndex) {
			XmlFilenameWriter.LOG.info("No identifier found, skip writing");
			XmlFilenameWriter.LOG.debug("the xml:" + xml);
			return;
		}
		String directory = identifier;
		if (directory.length() >= this.filenameUtil.endIndex) {
			directory = directory.substring(this.filenameUtil.startIndex,
					this.filenameUtil.endIndex);
		}
		final String file = FilenameUtils.concat(this.filenameUtil.target, FilenameUtils
				.concat(directory + File.separator, identifier + this.filenameUtil.fileSuffix));
		this.filenameUtil.ensurePathExists(file);
		try {
			if (this.compression == null) {
				this.writer = new OutputStreamWriter(new FileOutputStream(file),
						this.filenameUtil.encoding);
				IOUtils.write(xml, this.writer);
				this.writer.close();
			} else {
				if (this.compression.equals("bz2")) {
					this.out = new FileOutputStream(file + ".bz2");
					IOUtils.copy(new StringReader(xml), new CompressorStreamFactory()
							.createCompressorOutputStream(CompressorStreamFactory.BZIP2, this.out));
					new CompressorStreamFactory()
							.createCompressorOutputStream(CompressorStreamFactory.BZIP2, this.out)
							.close();
					this.out.close();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new MetafactureException(e);
		} catch (final CompressorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the compression. Default is no compression.
	 *
	 * @param compression
	 *            The compression. At the moment only 'bz2' is possible.
	 */
	public void setCompression(final String compression) {
		this.compression = compression;
	}

	@Override
	public void setEncoding(final String encoding) {
		this.filenameUtil.encoding = encoding;

	}

	@Override
	public void setEndIndex(final int endIndex) {
		this.filenameUtil.endIndex = endIndex;
	}

	@Override
	public void setFileSuffix(final String fileSuffix) {
		this.filenameUtil.fileSuffix = fileSuffix;

	}

	@Override
	public void setProperty(final String property) {
		this.filenameUtil.property = property;
	}

	@Override
	public void setStartIndex(final int startIndex) {
		this.filenameUtil.startIndex = startIndex;

	}

	@Override
	public void setTarget(final String target) {
		this.filenameUtil.target = target;
	}
}
