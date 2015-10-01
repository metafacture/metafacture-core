package org.culturegraph.mf.morph.functions.model;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

/**
 * @author tgaengler
 * @author phorn
 */
public final class ValueConverter {

	public static final byte[] MAGIC_HEADER_PREFIX = new byte[] { 1, 2, 3, 4 };

	public static String encodeTypeInfo(final String originalData, final ValueType type) {

		final byte[] originalDataBytes = originalData.getBytes(StandardCharsets.UTF_8);
		final byte[] magicHeader = new byte[] { (byte) type.ordinal() };

		final byte[] mergedBytes = new byte[MAGIC_HEADER_PREFIX.length + magicHeader.length + originalDataBytes.length];
		System.arraycopy(MAGIC_HEADER_PREFIX, 0, mergedBytes, 0, MAGIC_HEADER_PREFIX.length);
		System.arraycopy(magicHeader, 0, mergedBytes, MAGIC_HEADER_PREFIX.length, magicHeader.length);
		System.arraycopy(originalDataBytes, 0, mergedBytes, MAGIC_HEADER_PREFIX.length + magicHeader.length, originalDataBytes.length);

		return new String(mergedBytes, StandardCharsets.UTF_8);
	}

	public static Map.Entry<ValueType, String> decodeTypeInfo(final String mergedData) {

		final byte[] mergedDataBytes = mergedData.getBytes(StandardCharsets.UTF_8);
		final ValueType dataType;
		final String data;

		if (
				mergedDataBytes.length > 5 &&
						mergedDataBytes[0] == 1 &&
						mergedDataBytes[1] == 2 &&
						mergedDataBytes[2] == 3 &&
						mergedDataBytes[3] == 4
				) {

			final byte[] dataBytes = Arrays.copyOfRange(mergedDataBytes, 5, mergedDataBytes.length);
			data = new String(dataBytes, StandardCharsets.UTF_8);
			dataType = ValueType.values()[mergedDataBytes[4]];
		} else {

			dataType = ValueType.Literal;
			data = mergedData;
		}

		return new AbstractMap.SimpleImmutableEntry<>(dataType, data);
	}
}
