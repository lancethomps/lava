package com.github.lancethomps.lava.common.web;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

/**
 * The Class DataView.
 */
public class DataView extends AbstractView {

	/** The data. */
	private final String data;

	/** The data bytes. */
	private final byte[] dataBytes;

	/**
	 * Instantiates a new data view.
	 */
	public DataView() {
		super();
		data = null;
		dataBytes = null;
	}

	/**
	 * Instantiates a new data view.
	 *
	 * @param dataBytes the data bytes
	 */
	public DataView(byte[] dataBytes) {
		super();
		data = null;
		this.dataBytes = dataBytes;
	}

	/**
	 * Instantiates a new data view.
	 *
	 * @param data the data
	 */
	public DataView(String data) {
		super();
		this.data = data;
		dataBytes = null;
	}

	/**
	 * Write data to response.
	 *
	 * @param data the data
	 * @param response the response
	 * @throws Exception the exception
	 */
	public static void writeDataToResponse(String data, HttpServletResponse response) throws Exception {
		new DataView(data).renderMergedOutputModel(null, null, response);
	}

	/**
	 * Write data.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws Exception the exception
	 */
	public void writeData(HttpServletRequest request, HttpServletResponse response) throws Exception {
		renderMergedOutputModel(null, request, response);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map,
	 * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (data != null) {
			try (ServletOutputStream os = response.getOutputStream()) {
				byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
				response.setContentLength(bytes.length);
				os.write(bytes);
				os.flush();
			}
		} else if (dataBytes != null) {
			try (ServletOutputStream os = response.getOutputStream()) {
				response.setContentLength(dataBytes.length);
				os.write(dataBytes);
				os.flush();
			}
		}
	}

}
