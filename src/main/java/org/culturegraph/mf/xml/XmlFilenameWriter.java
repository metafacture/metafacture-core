/*
 * Copyright 2013 Pascal Christoph (hbz)
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
package org.culturegraph.mf.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.function.Function;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * A sink, writing an xml file. The filename is constructed from the xpath given
 * via setProperty().
 *
 * @author Pascal Christoph
 * @author Christoph Böhme
 */
@Description("Writes the xml into the filesystem. The filename is constructed from the xpath given as 'property'.\n"
		+ " Variables are\n" + "- 'target' (determining the output directory)\n"
		+ "- 'property' (the element in the XML entity. Constitutes the main part of the file's name.)\n"
		+ "- 'startIndex' ( a subfolder will be extracted out of the filename. This marks the index' beginning )\n"
		+ "- 'stopIndex' ( a subfolder will be extracted out of the filename. This marks the index' end )\n")
@In(StreamReceiver.class)
@Out(Void.class)
@FluxCommand("write-xml-files")
public final class XmlFilenameWriter
		extends DefaultStreamPipe<ObjectReceiver<String>>
		implements FilenameExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(
			XmlFilenameWriter.class);

	private final XPath xPath = XPathFactory.newInstance().newXPath();
	private final FilenameUtil filenameUtil = new FilenameUtil();

	private String compression;

	/**
	 * Default constructor
	 */
	public XmlFilenameWriter() {
		setFileSuffix(".xml");
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
		filenameUtil.encoding = encoding;
	}

	@Override
	public String getEncoding() {
		return filenameUtil.encoding;
	}

	@Override
	public void setEndIndex(final int endIndex) {
		filenameUtil.endIndex = endIndex;
	}

	@Override
	public void setFileSuffix(final String fileSuffix) {
		filenameUtil.fileSuffix = fileSuffix;

	}

	@Override
	public void setProperty(final String property) {
		filenameUtil.property = property;
	}

	@Override
	public void setStartIndex(final int startIndex) {
		filenameUtil.startIndex = startIndex;
	}

	@Override
	public void setTarget(final String target) {
		filenameUtil.target = target;
	}

	@Override
	public void literal(final String str, final String xml) {
		final String identifier = extractIdentifier(xml);
		if (identifier == null) {
			return;
		}
		final File file = buildTargetFileName(identifier);
		filenameUtil.ensurePathExists(file);
		if (compression == null) {
			writeXml(xml, file);
		} else if ("bz2".equals(compression)) {
			final File compressedFile = new File(file.getPath() + ".bz2");
			writeXml(xml, compressedFile, this::createBZip2Compressor);
		}
	}

	private String extractIdentifier(String xml) {
		final String identifier;
		try {
			identifier = xPath.evaluate(this.filenameUtil.property,
					new InputSource(new StringReader(xml)));
		} catch (XPathExpressionException e) {
			throw new MetafactureException(e);
		}
		if (identifier == null || identifier.length() < filenameUtil.endIndex) {
			LOG.info("No identifier found, skip writing");
			LOG.debug("the xml: {}", xml);
			return null;
		}
		return identifier;
	}

	private File buildTargetFileName(String identifier) {
		final String directory = identifier.substring(filenameUtil.startIndex,
				filenameUtil.endIndex);
		return Paths.get(filenameUtil.target)
				.resolve(directory)
				.resolve(identifier + filenameUtil.fileSuffix)
				.toFile();
	}

	private void writeXml(String xml, File file) {
		writeXml(xml, file, Function.identity());
	}

	private void writeXml(String xml, File file,
			Function<OutputStream, OutputStream> compressorFactory) {
		try (
				OutputStream fileStream = new FileOutputStream(file);
				OutputStream compressedStream = compressorFactory.apply(fileStream);
				Writer writer = new OutputStreamWriter(compressedStream,
						filenameUtil.encoding);
		) {
			writer.write(xml);
		} catch (IOException | UncheckedIOException e) {
			throw new MetafactureException(e);
		}
	}

	private OutputStream createBZip2Compressor(OutputStream stream) {
		try {
			return new BZip2CompressorOutputStream(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
