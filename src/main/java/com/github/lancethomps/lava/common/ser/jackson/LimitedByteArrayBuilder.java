package com.github.lancethomps.lava.common.ser.jackson;

import java.io.OutputStream;
import java.util.LinkedList;

import com.fasterxml.jackson.core.util.BufferRecycler;
import com.github.lancethomps.lava.common.ser.SerializationLimitException;
import com.github.lancethomps.lava.common.ser.SerializerFactory;

public class LimitedByteArrayBuilder extends OutputStream {

  public static final byte[] NO_BYTES = new byte[0];

  private static final int DEFAULT_BLOCK_ARRAY_SIZE = 40;

  private static final int INITIAL_BLOCK_SIZE = 500;

  private static final int MAX_BLOCK_SIZE = (1 << 18);

  private final BufferRecycler bufferRecycler;
  private final int limit;
  private final LinkedList<byte[]> pastBlocks = new LinkedList<byte[]>();
  private byte[] currBlock;
  private int currBlockPtr;
  private int pastLen;

  public LimitedByteArrayBuilder() {
    this(null, 0);
  }

  public LimitedByteArrayBuilder(BufferRecycler br, int firstBlockSize, int limit) {
    bufferRecycler = br;
    currBlock = (br == null) ? new byte[firstBlockSize] : br.allocByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER);
    this.limit = limit;
  }

  public LimitedByteArrayBuilder(BufferRecycler br, int limit) {
    this(br, INITIAL_BLOCK_SIZE, limit);
  }

  public LimitedByteArrayBuilder(int firstBlockSize) {
    this(null, firstBlockSize);
  }

  public void append(int i) {
    if (currBlockPtr >= currBlock.length) {
      allocMore();
    }
    currBlock[currBlockPtr++] = (byte) i;
  }

  public void appendThreeBytes(int b24) {
    if ((currBlockPtr + 2) < currBlock.length) {
      currBlock[currBlockPtr++] = (byte) (b24 >> 16);
      currBlock[currBlockPtr++] = (byte) (b24 >> 8);
      currBlock[currBlockPtr++] = (byte) b24;
    } else {
      append(b24 >> 16);
      append(b24 >> 8);
      append(b24);
    }
  }

  public void appendTwoBytes(int b16) {
    if ((currBlockPtr + 1) < currBlock.length) {
      currBlock[currBlockPtr++] = (byte) (b16 >> 8);
      currBlock[currBlockPtr++] = (byte) b16;
    } else {
      append(b16 >> 8);
      append(b16);
    }
  }

  @Override
  public void close() {
  }

  public byte[] completeAndCoalesce(int lastBlockLength) {
    currBlockPtr = lastBlockLength;
    return toByteArray();
  }

  public byte[] finishCurrentSegment() {
    allocMore();
    return currBlock;
  }

  @Override
  public void flush() {
  }

  public byte[] getCurrentSegment() {
    return currBlock;
  }

  public int getCurrentSegmentLength() {
    return currBlockPtr;
  }

  public void setCurrentSegmentLength(int len) {
    currBlockPtr = len;
  }

  public void release() {
    reset();
    if ((bufferRecycler != null) && (currBlock != null)) {
      bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER, currBlock);
      currBlock = null;
    }
  }

  public void reset() {
    pastLen = 0;
    currBlockPtr = 0;

    if (!pastBlocks.isEmpty()) {
      pastBlocks.clear();
    }
  }

  public byte[] resetAndGetFirstSegment() {
    reset();
    return currBlock;
  }

  public byte[] toByteArray() {
    int totalLen = pastLen + currBlockPtr;

    if (totalLen == 0) {
      return NO_BYTES;
    }
    checkLimit();

    byte[] result = new byte[totalLen];
    int offset = 0;

    for (byte[] block : pastBlocks) {
      int len = block.length;
      System.arraycopy(block, 0, result, offset, len);
      offset += len;
    }
    System.arraycopy(currBlock, 0, result, offset, currBlockPtr);
    offset += currBlockPtr;
    if (offset != totalLen) {
      throw new RuntimeException("Internal error: total len assumed to be " + totalLen + ", copied " + offset + " bytes");
    }

    if (!pastBlocks.isEmpty()) {
      reset();
    }
    return result;
  }

  @Override
  public void write(byte[] b) {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) {
    while (true) {
      int max = currBlock.length - currBlockPtr;
      int toCopy = Math.min(max, len);
      if (toCopy > 0) {
        System.arraycopy(b, off, currBlock, currBlockPtr, toCopy);
        off += toCopy;
        currBlockPtr += toCopy;
        len -= toCopy;
      }
      if (len <= 0) {
        break;
      }
      allocMore();
    }
  }

  @Override
  public void write(int b) {
    append(b);
  }

  private void allocMore() {
    pastLen += currBlock.length;

    int newSize = Math.max((pastLen >> 1), (INITIAL_BLOCK_SIZE + INITIAL_BLOCK_SIZE));

    if (newSize > MAX_BLOCK_SIZE) {
      newSize = MAX_BLOCK_SIZE;
    }
    pastBlocks.add(currBlock);
    currBlock = new byte[newSize];
    currBlockPtr = 0;
  }

  private void checkLimit() throws SerializationLimitException {
    if (limit > 0) {
      SerializerFactory.checkLimit(limit, pastLen + currBlockPtr);
    }
  }

}
