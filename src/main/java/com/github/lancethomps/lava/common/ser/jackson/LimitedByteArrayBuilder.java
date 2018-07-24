package com.github.lancethomps.lava.common.ser.jackson;

import java.io.OutputStream;
import java.util.LinkedList;

import com.github.lancethomps.lava.common.ser.SerializationLimitException;
import com.github.lancethomps.lava.common.ser.SerializerFactory;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * The Class LimitedByteArrayBuilder.
 */
public class LimitedByteArrayBuilder extends OutputStream {

	/** The Constant NO_BYTES. */
	public static final byte[] NO_BYTES = new byte[0];

	/** The Constant DEFAULT_BLOCK_ARRAY_SIZE. */
	private static final int DEFAULT_BLOCK_ARRAY_SIZE = 40;

	/** The Constant INITIAL_BLOCK_SIZE. */
	// Size of the first block we will allocate.
	private static final int INITIAL_BLOCK_SIZE = 500;

	// Maximum block size we will use for individual non-aggregated
	/** The Constant MAX_BLOCK_SIZE. */
	// blocks. Let's limit to using 256k chunks.
	private static final int MAX_BLOCK_SIZE = (1 << 18);

	/** The buffer recycler. */
	// Optional buffer recycler instance that we can use for allocating the first block.
	private final BufferRecycler bufferRecycler;

	/** The curr block. */
	private byte[] currBlock;

	/** The curr block ptr. */
	private int currBlockPtr;

	/** The limit. */
	private final int limit;

	/** The past blocks. */
	private final LinkedList<byte[]> pastBlocks = new LinkedList<byte[]>();

	/** The past len. */
	// Number of bytes within byte arrays in {@link _pastBlocks}.
	private int pastLen;

	/**
	 * Instantiates a new limited byte array builder.
	 */
	public LimitedByteArrayBuilder() {
		this(null, 0);
	}

	/**
	 * Instantiates a new limited byte array builder.
	 *
	 * @param br the br
	 * @param firstBlockSize the first block size
	 * @param limit the limit
	 */
	public LimitedByteArrayBuilder(BufferRecycler br, int firstBlockSize, int limit) {
		bufferRecycler = br;
		currBlock = (br == null) ? new byte[firstBlockSize] : br.allocByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER);
		this.limit = limit;
	}

	/**
	 * Instantiates a new limited byte array builder.
	 *
	 * @param br the br
	 * @param limit the limit
	 */
	public LimitedByteArrayBuilder(BufferRecycler br, int limit) {
		this(br, INITIAL_BLOCK_SIZE, limit);
	}

	/**
	 * Instantiates a new limited byte array builder.
	 *
	 * @param firstBlockSize the first block size
	 */
	public LimitedByteArrayBuilder(int firstBlockSize) {
		this(null, firstBlockSize);
	}

	/**
	 * Append.
	 *
	 * @param i the i
	 */
	public void append(int i) {
		if (currBlockPtr >= currBlock.length) {
			allocMore();
		}
		currBlock[currBlockPtr++] = (byte) i;
	}

	/**
	 * Append three bytes.
	 *
	 * @param b24 the b 24
	 */
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

	/**
	 * Append two bytes.
	 *
	 * @param b16 the b 16
	 */
	public void appendTwoBytes(int b16) {
		if ((currBlockPtr + 1) < currBlock.length) {
			currBlock[currBlockPtr++] = (byte) (b16 >> 8);
			currBlock[currBlockPtr++] = (byte) b16;
		} else {
			append(b16 >> 8);
			append(b16);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() {
		/* NOP */ }

	/**
	 * Method that will complete "manual" output process, coalesce
	 * content (if necessary) and return results as a contiguous buffer.
	 *
	 * @param lastBlockLength Amount of content in the current segment
	 * buffer.
	 *
	 * @return Coalesced contents
	 */
	public byte[] completeAndCoalesce(int lastBlockLength) {
		currBlockPtr = lastBlockLength;
		return toByteArray();
	}

	/**
	 * Method called when the current segment buffer is full; will
	 * append to current contents, allocate a new segment buffer
	 * and return it.
	 *
	 * @return the byte[]
	 */
	public byte[] finishCurrentSegment() {
		allocMore();
		return currBlock;
	}

	/*
	 * /********************************************************** /* Non-stream API (similar to TextBuffer), since 1.6 /**********************************************************
	 */

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() {
		/* NOP */ }

	/**
	 * Gets the current segment.
	 *
	 * @return the current segment
	 */
	public byte[] getCurrentSegment() {
		return currBlock;
	}

	/**
	 * Gets the current segment length.
	 *
	 * @return the current segment length
	 */
	public int getCurrentSegmentLength() {
		return currBlockPtr;
	}

	/**
	 * Clean up method to call to release all buffers this object may be
	 * using. After calling the method, no other accessors can be used (and
	 * attempt to do so may result in an exception)
	 */
	public void release() {
		reset();
		if ((bufferRecycler != null) && (currBlock != null)) {
			bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER, currBlock);
			currBlock = null;
		}
	}

	/**
	 * Reset.
	 */
	public void reset() {
		pastLen = 0;
		currBlockPtr = 0;

		if (!pastBlocks.isEmpty()) {
			pastBlocks.clear();
		}
	}

	/**
	 * Method called when starting "manual" output: will clear out
	 * current state and return the first segment buffer to fill.
	 *
	 * @return the byte[]
	 */
	public byte[] resetAndGetFirstSegment() {
		reset();
		return currBlock;
	}

	/*
	 * /********************************************************** /* OutputStream implementation /**********************************************************
	 */

	/**
	 * Sets the current segment length.
	 *
	 * @param len the new current segment length
	 */
	public void setCurrentSegmentLength(int len) {
		currBlockPtr = len;
	}

	/**
	 * Method called when results are finalized and we can get the
	 * full aggregated result buffer to return to the caller.
	 *
	 * @return the byte[]
	 */
	public byte[] toByteArray() {
		int totalLen = pastLen + currBlockPtr;

		if (totalLen == 0) { // quick check: nothing aggregated?
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
		if (offset != totalLen) { // just a sanity check
			throw new RuntimeException("Internal error: total len assumed to be " + totalLen + ", copied " + offset + " bytes");
		}
		// Let's only reset if there's sizable use, otherwise will get reset later on
		if (!pastBlocks.isEmpty()) {
			reset();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) {
		append(b);
	}

	/*
	 * /********************************************************** /* Internal methods /**********************************************************
	 */

	/**
	 * Alloc more.
	 */
	private void allocMore() {
		pastLen += currBlock.length;

		/*
		 * Let's allocate block that's half the total size, except never smaller than twice the initial block size. The idea is just to grow with reasonable rate, to optimize between
		 * minimal number of chunks and minimal amount of wasted space.
		 */
		int newSize = Math.max((pastLen >> 1), (INITIAL_BLOCK_SIZE + INITIAL_BLOCK_SIZE));
		// plus not to exceed max we define...
		if (newSize > MAX_BLOCK_SIZE) {
			newSize = MAX_BLOCK_SIZE;
		}
		pastBlocks.add(currBlock);
		currBlock = new byte[newSize];
		currBlockPtr = 0;
	}

	/**
	 * Check limit.
	 *
	 * @throws SerializationLimitException the serialization limit exception
	 */
	private void checkLimit() throws SerializationLimitException {
		if (limit > 0) {
			SerializerFactory.checkLimit(limit, pastLen + currBlockPtr);
		}
	}

}
