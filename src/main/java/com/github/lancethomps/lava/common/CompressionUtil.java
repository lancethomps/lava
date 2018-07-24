package com.github.lancethomps.lava.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterOutputStream;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/**
 * The Class CompressionUtil.
 */
public class CompressionUtil {

	/** The decompressor. */
	private static final LZ4FastDecompressor DECOMPRESSOR = LZ4Factory.fastestInstance().fastDecompressor();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(CompressionUtil.class);

	/**
	 * Bytes to base64.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static byte[] bytesToBase64(byte[] bytes) {
		return Base64.encodeBase64(bytes, true);
	}

	/**
	 * Bytes to base64 string.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String bytesToBase64String(byte[] bytes) {
		return newStringUtf8(bytesToBase64(bytes));
	}

	/**
	 * Compress gzip.
	 *
	 * @param data the data
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static byte[] compressGzip(String data) throws IOException {
		byte[] uncompressedData = data.getBytes(StandardCharsets.UTF_8);
		byte[] result = new byte[] {};
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
			GZIPOutputStream gzipOs = new GZIPOutputStream(bos)) {
			gzipOs.write(uncompressedData);
			gzipOs.close();
			result = bos.toByteArray();
		}
		return result;
	}

	/**
	 * Decode base64.
	 *
	 * @param base64 the base64
	 * @return the string
	 */
	public static String decodeBase64(String base64) {
		return newStringUtf8(Base64.decodeBase64(base64));
	}

	/**
	 * Decode base 64 to bytes.
	 *
	 * @param base64 the base 64
	 * @return the byte[]
	 */
	public static byte[] decodeBase64ToBytes(String base64) {
		return Base64.decodeBase64(base64);
	}

	/**
	 * Decompress.
	 *
	 * @param input the input
	 * @return the byte[]
	 */
	public static byte[] decompress(byte[] input) {
		ByteBuffer buffer = ByteBuffer.wrap(input, 0, 4);
		int originalSize = buffer.getInt();

		byte[] decompressedBytes = new byte[originalSize];
		DECOMPRESSOR.decompress(input, 4, decompressedBytes, 0, originalSize);
		return decompressedBytes;
	}

	/**
	 * Decompress gzip.
	 *
	 * @param compressedData the compressed data
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String decompressGzip(byte[] compressedData) throws IOException {
		byte[] result = new byte[] {};
		try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gzipIS.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			result = bos.toByteArray();
		}
		return newStringUtf8(result);
	}

	/**
	 * Decompress gzip.
	 *
	 * @param base64 the base64
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String decompressGzip(String base64) throws IOException {
		byte[] decoded = Base64.decodeBase64(base64);
		BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(decoded))));
		StringBuilder data = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			data.append(line);
		}
		return data.toString();
	}

	/**
	 * Deflate.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String deflate(byte[] bytes) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DeflaterOutputStream dos = new DeflaterOutputStream(baos);
			dos.write(bytes);
			dos.finish();
			return newStringUtf8(Base64.encodeBase64(baos.toByteArray(), true));
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error deflating data!");
		}
		return null;
	}

	/**
	 * Deflate.
	 *
	 * @param data the data
	 * @return the string
	 */
	public static String deflate(String data) {
		return deflate(getBytes(data));
	}

	/**
	 * Gets the bytes.
	 *
	 * @param data the data
	 * @return the bytes
	 */
	public static byte[] getBytes(String data) {
		return getBytes(data, UTF_8.toString());
	}

	/**
	 * Gets the bytes.
	 *
	 * @param data the data
	 * @param charset the charset
	 * @return the bytes
	 */
	public static byte[] getBytes(String data, String charset) {
		try {
			return data.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			Logs.logError(LOG, e, "Error deflating data!");
		}
		return new byte[] {};
	}

	/**
	 * Inflate.
	 *
	 * @param base64 the base64
	 * @return the string
	 */
	public static String inflate(String base64) {
		return inflate(base64, UTF_8.toString());
	}

	/**
	 * Inflate.
	 *
	 * @param base64 the base64
	 * @param charset the charset
	 * @return the string
	 */
	public static String inflate(String base64, String charset) {
		try {
			return inflateAsBaos(base64).toString(charset);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error inflating data [%s]!", base64);
		}
		return null;
	}

	/**
	 * Inflate as baos.
	 *
	 * @param data the data
	 * @return the byte array output stream
	 */
	public static ByteArrayOutputStream inflateAsBaos(byte[] data) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InflaterOutputStream ios = new InflaterOutputStream(baos);
			ios.write(data);
			ios.finish();
			return baos;
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error inflating data!");
		}
		return null;
	}

	/**
	 * Inflate as baos.
	 *
	 * @param base64 the base 64
	 * @return the byte array output stream
	 */
	public static ByteArrayOutputStream inflateAsBaos(String base64) {
		return inflateAsBaos(Base64.decodeBase64(base64));
	}

	/**
	 * Inflate as bytes.
	 *
	 * @param data the data
	 * @return the byte[]
	 */
	public static byte[] inflateAsBytes(byte[] data) {
		ByteArrayOutputStream baos = inflateAsBaos(data);
		return baos == null ? null : baos.toByteArray();
	}

	/**
	 * Inflate as bytes.
	 *
	 * @param base64 the base64
	 * @return the byte[]
	 */
	public static byte[] inflateAsBytes(String base64) {
		ByteArrayOutputStream baos = inflateAsBaos(base64);
		return baos == null ? null : baos.toByteArray();
	}

	/**
	 * New string.
	 *
	 * @param encode the encode
	 * @param encoding the encoding
	 * @return the string
	 */
	public static String newString(byte[] encode, String encoding) {
		String str = null;
		try {
			str = new String(encode, encoding);
		} catch (UnsupportedEncodingException ue) {
			throw new RuntimeException(ue);
		}
		return str;
	}

	/**
	 * New string utf8.
	 *
	 * @param encode the encode
	 * @return the string
	 */
	public static String newStringUtf8(byte[] encode) {
		String str = null;
		try {
			str = new String(encode, UTF_8.toString());
		} catch (UnsupportedEncodingException ue) {
			throw new RuntimeException(ue);
		}
		return str;
	}
}
