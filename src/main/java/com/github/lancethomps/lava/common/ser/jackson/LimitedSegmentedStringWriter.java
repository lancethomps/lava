package com.github.lancethomps.lava.common.ser.jackson;

import java.io.Writer;

import com.github.lancethomps.lava.common.ser.SerializationLimitException;
import com.github.lancethomps.lava.common.ser.SerializerFactory;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

/**
 * The Class LimitedSegmentedStringWriter.
 */
public final class LimitedSegmentedStringWriter extends Writer {

	/** The _buffer. */
	private final TextBuffer buffer;

	/** The limit. */
	private final int limit;

	/**
	 * Instantiates a new limited segmented string writer.
	 *
	 * @param br the br
	 */
	public LimitedSegmentedStringWriter(BufferRecycler br) {
		this(br, -1);
	}

	/**
	 * Instantiates a new limited segmented string writer.
	 *
	 * @param br the br
	 * @param limit the limit
	 */
	public LimitedSegmentedStringWriter(BufferRecycler br, int limit) {
		super();
		buffer = new TextBuffer(br);
		this.limit = limit;
	}

	@Override
	public Writer append(char c) {
		write(c);
		return this;
	}

	@Override
	public Writer append(CharSequence csq) {
		String str = csq.toString();
		buffer.append(str, 0, str.length());
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) {
		String str = csq.subSequence(start, end).toString();
		buffer.append(str, 0, str.length());
		return this;
	}

	@Override
	public void close() {
	} // NOP

	@Override
	public void flush() {
	} // NOP

	/**
	 * Gets the and clear.
	 *
	 * @return the and clear
	 * @throws SerializationException the serialization exception
	 */
	public String getAndClear() throws SerializationException {
		try {
			checkLimit();
			String result = buffer.contentsAsString();
			return result;
		} finally {
			buffer.releaseBuffers();
		}
	}

	@Override
	public void write(char[] cbuf) {
		buffer.append(cbuf, 0, cbuf.length);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		buffer.append(cbuf, off, len);
	}

	@Override
	public void write(int c) {
		buffer.append((char) c);
	}

	@Override
	public void write(String str) {
		buffer.append(str, 0, str.length());
	}

	@Override
	public void write(String str, int off, int len) {
		buffer.append(str, off, len);
	}

	/**
	 * Check limit.
	 *
	 * @throws SerializationLimitException the serialization limit exception
	 */
	private void checkLimit() throws SerializationLimitException {
		if (limit > 0) {
			SerializerFactory.checkLimit(limit, buffer.size());
		}
	}
}
