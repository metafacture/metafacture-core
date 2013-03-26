/*
 *  Copyright 2013 Christoph Böhme
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
package net.b3e.mf.extra.pipe;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import net.b3e.mf.extra.util.DigestAlgorithm;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * @author Christoph Böhme
 *
 */
public final class FileDigestCalculator extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	private static final int BUFFER_SIZE = 1024;
	
	private static final int HIGH_NIBBLE = 0xf0;
	private static final int LOW_NIBBLE = 0x0f;	
	private static final char[] NIBBLE_TO_HEX = 
			{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private final MessageDigest messageDigest;

	
	public FileDigestCalculator(final DigestAlgorithm algorithm) {
		this.messageDigest = algorithm.getInstance();
	}
	
	public FileDigestCalculator(final String algorithm) {
		this.messageDigest = DigestAlgorithm.valueOf(algorithm.toUpperCase()).getInstance();
	}

	@Override
	public void process(final String file) {
		final String digest;
		try (final InputStream stream = new FileInputStream(file)) {
			digest = bytesToHex(getDigest(stream, messageDigest));
			
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
		getReceiver().process(digest);
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
			// NO CHECKSTYLE MagicNumber FOR 1 LINE:
			hex[i * 2] = NIBBLE_TO_HEX[(bytes[i] & HIGH_NIBBLE) >>> 4];
			hex[i * 2 + 1] = NIBBLE_TO_HEX[bytes[i] & LOW_NIBBLE];
		}
		return new String(hex);
	}
	
}
