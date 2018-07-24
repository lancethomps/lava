package com.github.lancethomps.lava.common.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.StringBuilderWriter;

/**
 * The Class ResponseWrapper.
 */
public class ResponseForwardWrapper extends HttpServletResponseWrapper {

	/** The Constant DEFAULT_SIZE. */
	private static final int DEFAULT_SIZE = 10240;

	/** The output stream. */
	private BufferedServletOutputStream outputStream;

	/** The print writer. */
	private PrintWriter printWriter;

	/** The string writer. */
	private StringBuilderWriter writer;

	/**
	 * Instantiates a new response wrapper.
	 *
	 * @param response the response
	 */
	public ResponseForwardWrapper(HttpServletResponse response) {
		super(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		if (outputStream != null) {
			return outputStream.getBuffer().length;
		}

		return writer.getBuilder().length();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() already called");
		}

		if (outputStream == null) {
			outputStream = new BufferedServletOutputStream();
		}

		return outputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getWriter()
	 */
	@Override
	public PrintWriter getWriter() {
		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() already called");
		}

		if (writer == null) {
			writer = new StringBuilderWriter(DEFAULT_SIZE);
			printWriter = new PrintWriter(writer);
		}

		return printWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#resetBuffer()
	 */
	@Override
	public void resetBuffer() {
		if (outputStream != null) {
			outputStream.reset();
		}

		if (writer != null) {
			writer.getBuilder().setLength(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String output = "";

		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException e) {
				;
			}
			output = new String(outputStream.getBuffer(), StandardCharsets.UTF_8);
		} else if (writer != null) {
			output = writer.toString();
		}

		return output;
	}
}
