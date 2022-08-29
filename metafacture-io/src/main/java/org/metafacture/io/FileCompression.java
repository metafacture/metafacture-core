/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.io;

import org.metafacture.framework.MetafactureException;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides a convenient interface for using stream compressors
 * and decompressors.
 *
 * @author Christoph Böhme
 *
 */
public enum FileCompression {

    NONE {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            return new ProxyOutputStream(writeTo);
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            return new ProxyInputStream(readFrom) {
                //nothing to do
            };
        }
    },

    AUTO {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            if (fileName == null) {
                throw new IllegalArgumentException("fileName is required for auto-selecting compressor");
            }

            final String extension = FilenameUtils.getExtension(fileName);
            final FileCompression compressor;
            if ("gz".equalsIgnoreCase(extension)) {
                compressor = GZIP;
            }
            else if ("gzip".equalsIgnoreCase(extension)) {
                compressor = GZIP;
            }
            else if ("bz2".equalsIgnoreCase(extension)) {
                compressor = BZIP2;
            }
            else if ("bzip2".equalsIgnoreCase(extension)) {
                compressor = BZIP2;
            }
            else if ("xz".equalsIgnoreCase(extension)) {
                compressor = XZ;
            }
            else {
                compressor = NONE;
            }

            return compressor.createCompressor(writeTo, fileName);
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            final InputStream bufferedStream = bufferStream(readFrom);
            try {
                return decompressConcatenated ?
                    APACHE_COMPRESSOR_FACTORY_DECOMPRESS_CONCATENATED.createCompressorInputStream(bufferedStream) :
                    APACHE_COMPRESSOR_FACTORY_NO_DECOMPRESS_CONCATENATED.createCompressorInputStream(bufferedStream);
            }
            catch (final CompressorException e) {
                return NONE.createDecompressor(bufferedStream, decompressConcatenated);
            }
        }
    },

    BZIP2 {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorOutputStream(
                        CompressorStreamFactory.BZIP2, bufferStream(writeTo));
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorInputStream(
                        CompressorStreamFactory.BZIP2, bufferStream(readFrom), decompressConcatenated);
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }
    },

    GZIP {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorOutputStream(
                        CompressorStreamFactory.GZIP, bufferStream(writeTo));
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorInputStream(
                        CompressorStreamFactory.GZIP, bufferStream(readFrom), decompressConcatenated);
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }
    },

    PACK200 {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorOutputStream(
                        CompressorStreamFactory.PACK200, bufferStream(writeTo));
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorInputStream(
                        CompressorStreamFactory.PACK200, bufferStream(readFrom), decompressConcatenated);
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }
    },

    XZ {
        @Override
        public OutputStream createCompressor(final OutputStream writeTo, final String fileName) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorOutputStream(
                        CompressorStreamFactory.XZ, bufferStream(writeTo));
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }

        @Override
        public InputStream createDecompressor(final InputStream readFrom, final boolean decompressConcatenated) {
            try {
                return APACHE_COMPRESSOR_FACTORY.createCompressorInputStream(
                        CompressorStreamFactory.XZ, bufferStream(readFrom), decompressConcatenated);
            }
            catch (final CompressorException e) {
                throw new MetafactureException(e);
            }
        }
    };

    public static final boolean DEFAULT_DECOMPRESS_CONCATENATED = false;

    private static final CompressorStreamFactory APACHE_COMPRESSOR_FACTORY_DECOMPRESS_CONCATENATED = new CompressorStreamFactory(true);
    private static final CompressorStreamFactory APACHE_COMPRESSOR_FACTORY_NO_DECOMPRESS_CONCATENATED = new CompressorStreamFactory(false);
    private static final CompressorStreamFactory APACHE_COMPRESSOR_FACTORY = DEFAULT_DECOMPRESS_CONCATENATED ?
        APACHE_COMPRESSOR_FACTORY_DECOMPRESS_CONCATENATED : APACHE_COMPRESSOR_FACTORY_NO_DECOMPRESS_CONCATENATED;

    private static final int BUFFER_SIZE = 8 * 1024 * 1024;

    /**
     * Creates a compressor.
     *
     * @param writeTo  the {@link OutputStream} to write to
     * @param fileName the filename
     * @return the {@link OutputStream}
     */
    public abstract OutputStream createCompressor(OutputStream writeTo, String fileName);

    /**
     * Creates a decompressor.
     *
     * @param readFrom               {the @link InputStream} to read from.
     * @param decompressConcatenated true if decompress concatenated, otherwise
     *                               false
     * @return the {@link InputStream}
     */
    public abstract InputStream createDecompressor(InputStream readFrom, boolean decompressConcatenated);

    /**
     * Creates a decompressor.
     *
     * @param readFrom {the @link InputStream} to read from.
     * @return the {@link InputStream}
     */
    public InputStream createDecompressor(final InputStream readFrom) {
        return createDecompressor(readFrom, DEFAULT_DECOMPRESS_CONCATENATED);
    }

    private static OutputStream bufferStream(final OutputStream stream) {
        if (stream instanceof BufferedOutputStream) {
            return stream;
        }
        return new BufferedOutputStream(stream, BUFFER_SIZE);
    }

    private static InputStream bufferStream(final InputStream stream) {
        if (stream instanceof BufferedInputStream) {
            return stream;
        }
        return new BufferedInputStream(stream, BUFFER_SIZE);
    }

}
