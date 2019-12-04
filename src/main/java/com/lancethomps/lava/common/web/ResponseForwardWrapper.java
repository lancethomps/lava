package com.lancethomps.lava.common.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.StringBuilderWriter;

public class ResponseForwardWrapper extends HttpServletResponseWrapper {

  private static final int DEFAULT_SIZE = 10240;

  private BufferedServletOutputStream outputStream;

  private PrintWriter printWriter;

  private StringBuilderWriter writer;

  public ResponseForwardWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public int getBufferSize() {
    if (outputStream != null) {
      return outputStream.getBuffer().length;
    }

    return writer.getBuilder().length();
  }

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

  @Override
  public void resetBuffer() {
    if (outputStream != null) {
      outputStream.reset();
    }

    if (writer != null) {
      writer.getBuilder().setLength(0);
    }
  }

  @Override
  public String toString() {
    String output = "";

    if (outputStream != null) {
      try {
        outputStream.flush();
      } catch (IOException e) {
      }
      output = new String(outputStream.getBuffer(), StandardCharsets.UTF_8);
    } else if (writer != null) {
      output = writer.toString();
    }

    return output;
  }

}
