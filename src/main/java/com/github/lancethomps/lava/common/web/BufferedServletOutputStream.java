package com.github.lancethomps.lava.common.web;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * The Class BufferedServletOutputStream.
 */
public class BufferedServletOutputStream extends ServletOutputStream {

	/** The bos. */
	private ByteArrayOutputStream bos = new ByteArrayOutputStream();

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		bos.write(b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		bos.flush();
	}

	/**
	 * Gets the buffer.
	 *
	 * @return the buffer
	 */
	public byte[] getBuffer() {
		return bos.toByteArray();
	}

	/**
	 * Reset.
	 */
	public void reset() {
		bos.reset();
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// NB: purposely doing nothing here
	}

}
