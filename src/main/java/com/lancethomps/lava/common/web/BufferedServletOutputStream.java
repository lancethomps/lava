package com.lancethomps.lava.common.web;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class BufferedServletOutputStream extends ServletOutputStream {

  private ByteArrayOutputStream bos = new ByteArrayOutputStream();

  @Override
  public void flush() throws IOException {
    bos.flush();
  }

  public byte[] getBuffer() {
    return bos.toByteArray();
  }

  @Override
  public boolean isReady() {
    return false;
  }

  public void reset() {
    bos.reset();
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {

  }

  @Override
  public void write(int b) throws IOException {
    bos.write(b);
  }

}
