package com.github.lancethomps.lava.common.ser;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * The Class ExternalizableOutput.
 */
public final class ExternalizableOutput extends OutputStream {

	/** The out. */
	private final ObjectOutput out;

	/**
	 * Instantiates a new externalizable output.
	 *
	 * @param out the out
	 */
	public ExternalizableOutput(ObjectOutput out) {
		this.out = out;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void write(int ch) throws IOException {
		out.write(ch);
	}

	@Override
	public void write(byte[] data) throws IOException {
		out.write(data);
	}

	@Override
	public void write(byte[] data, int offset, int len) throws IOException {
		out.write(data, offset, len);
	}
}
