package com.github.lancethomps.lava.common.web;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

public class DataView extends AbstractView {

  private final String data;

  private final byte[] dataBytes;

  public DataView() {
    super();
    data = null;
    dataBytes = null;
  }

  public DataView(byte[] dataBytes) {
    super();
    data = null;
    this.dataBytes = dataBytes;
  }

  public DataView(String data) {
    super();
    this.data = data;
    dataBytes = null;
  }

  public static void writeDataToResponse(String data, HttpServletResponse response) throws Exception {
    new DataView(data).renderMergedOutputModel(null, null, response);
  }

  public void writeData(HttpServletRequest request, HttpServletResponse response) throws Exception {
    renderMergedOutputModel(null, request, response);
  }

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
