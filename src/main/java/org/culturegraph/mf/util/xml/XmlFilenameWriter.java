/* Copyright 2013 Pascal Christoph, hbz. 
 * Licensed under the Eclipse Public License 1.0 */

package org.culturegraph.mf.util.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

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
public final class XmlFilenameWriter extends DefaultStreamPipe<ObjectReceiver<String>>implements FilenameExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(XmlFilenameWriter.class);
	private static final XPath xPath = XPathFactory.newInstance().newXPath();

	private FilenameUtil filenameUtil = new FilenameUtil();
	private String compression;

	/**
	 * Default constructor
	 */
	public XmlFilenameWriter() {
		setFileSuffix(".xml");
	}

	/**
	 * Sets the compression, if any. Default is no compression.
	 * 
	 * @param compression
	 *            The compression. At the moment only 'bz2' is possible.
	 */
	public void setCompression(String compression) {
		this.compression = compression;
	}

	@Override
	public void literal(final String str, String xml) {
		String identifier = null;
		try {
			identifier = xPath.evaluate(filenameUtil.property, new InputSource(new StringReader(xml)));
		} catch (XPathExpressionException e2) {
			e2.printStackTrace();
		}
		if (identifier == null || identifier.length() < filenameUtil.endIndex) {
			LOG.info("No identifier found, skip writing");
			LOG.debug("the xml:" + xml);
			return;
		}
		String directory = identifier;
		if (directory.length() >= filenameUtil.endIndex) {
			directory = directory.substring(filenameUtil.startIndex, filenameUtil.endIndex);
		}
		final String file = FilenameUtils.concat(filenameUtil.target,
				FilenameUtils.concat(directory + File.separator, identifier + filenameUtil.fileSuffix));
		filenameUtil.ensurePathExists(file);
		try {
			if (this.compression == null) {
				final Writer writer = new OutputStreamWriter(new FileOutputStream(file), filenameUtil.encoding);
				IOUtils.write(xml, writer);
				writer.close();
			} else {
				if (this.compression.equals("bz2")) {
					final OutputStream out = new FileOutputStream(file + ".bz2");
					CompressorOutputStream cos = new CompressorStreamFactory()
							.createCompressorOutputStream(CompressorStreamFactory.BZIP2, out);
					IOUtils.copy(new StringReader(xml), cos);
					cos.close();
					out.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MetafactureException(e);
		} catch (CompressorException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getEncoding() {
		return filenameUtil.encoding;
	}

	@Override
	public void setEncoding(String encoding) {
		filenameUtil.encoding = encoding;

	}

	@Override
	public void setTarget(String target) {
		filenameUtil.target = target;
	}

	@Override
	public void setProperty(String property) {
		filenameUtil.property = property;
	}

	@Override
	public void setFileSuffix(String fileSuffix) {
		filenameUtil.fileSuffix = fileSuffix;

	}

	@Override
	public void setStartIndex(int startIndex) {
		filenameUtil.startIndex = startIndex;

	}

	@Override
	public void setEndIndex(int endIndex) {
		filenameUtil.endIndex = endIndex;
	}
}
