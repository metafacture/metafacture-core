/*
 * Copyright 2016 Christoph Böhme
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.objects.Triple;

/**
 * Interprets the input string as a file name and computes a cryptographic hash
 * for the file.
 *
 * @author Christoph Böhme
 *
 */
@Description("Uses the input string as a file name and computes a cryptographic hash the file")
@In(String.class)
@Out(Triple.class)
@FluxCommand("digest-file")
public final class FileDigestCalculator extends
		DefaultObjectPipe<String, ObjectReceiver<Triple>> {

	private static final int BUFFER_SIZE = 1024;

	private static final int HIGH_NIBBLE = 0xf0;
	private static final int LOW_NIBBLE = 0x0f;
	private static final char[] NIBBLE_TO_HEX =
			{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private final DigestAlgorithm algorithm;
	private final MessageDigest messageDigest;


	public FileDigestCalculator(final DigestAlgorithm algorithm) {
		this.algorithm = algorithm;
		this.messageDigest = this.algorithm.getInstance();
	}

	public FileDigestCalculator(final String algorithm) {
		this.algorithm = DigestAlgorithm.valueOf(algorithm.toUpperCase());
		this.messageDigest = this.algorithm.getInstance();
	}

	@Override
	public void process(final String file) {
		final String digest;
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			digest = bytesToHex(getDigest(stream, messageDigest));
		} catch (IOException e) {
			throw new MetafactureException(e);
		} finally {
			if (stream != null) {
				try { stream.close(); }
				catch (final IOException e) { }
			}
		}
		getReceiver().process(new Triple(file, algorithm.name(), digest));
	}

	private static byte[] getDigest(final InputStream stream, final MessageDigest messageDigest) throws IOException {
		final byte[] buffer = new byte[BUFFER_SIZE];

	    int read = stream.read(buffer, 0, BUFFER_SIZE);
	    while (read > -1) {
	    	messageDigest.update(buffer, 0, read);
	        read = stream.read(buffer, 0, BUFFER_SIZE);
	    }
	    return messageDigest.digest();
	}

	private static String bytesToHex(final byte[] bytes) {
		final char[] hex = new char[bytes.length * 2];
		for (int i=0; i < bytes.length; ++i) {
			hex[i * 2] = NIBBLE_TO_HEX[(bytes[i] & HIGH_NIBBLE) >>> 4];
			hex[i * 2 + 1] = NIBBLE_TO_HEX[bytes[i] & LOW_NIBBLE];
		}
		return new String(hex);
	}

	/**
	 * Message digests which can be used by modules.
	 *
	 * @author Christoph Böhme
	 */
	public enum DigestAlgorithm {

		MD2("MD2"),
		MD5("MD5"),
		SHA1("SHA-1"),
		SHA256("SHA-256"),
		SHA384("SHA-384"),
		SHA512 ("SHA-512");

		private final String identifier;

		private DigestAlgorithm(final String identifier) {
			this.identifier = identifier;
		}

		public MessageDigest getInstance() {
			try {
				return MessageDigest.getInstance(identifier);
			} catch (NoSuchAlgorithmException e) {
				throw new MetafactureException (e);
			}
		}

	}

}
