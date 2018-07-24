package com.github.lancethomps.lava.common.ser.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

/**
 * The Class LimitedObjectMapper.
 */
public class LimitedObjectMapper extends ObjectMapper {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1269892369733873933L;

	/** The limit. */
	private int limit = -1;

	/**
	 * Instantiates a new limited object mapper.
	 */
	public LimitedObjectMapper() {
		super();
	}

	/**
	 * Instantiates a new limited object mapper.
	 *
	 * @param factory the factory
	 */
	public LimitedObjectMapper(JsonFactory factory) {
		super(factory);
	}

	/**
	 * Instantiates a new limited object mapper.
	 *
	 * @param jf the jf
	 * @param sp the sp
	 * @param dc the dc
	 */
	public LimitedObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc) {
		super(jf, sp, dc);
	}

	/**
	 * Instantiates a new limited object mapper.
	 *
	 * @param src the src
	 */
	public LimitedObjectMapper(LimitedObjectMapper src) {
		super(src);
		limit = src.limit;
	}

	/**
	 * Instantiates a new limited object mapper.
	 *
	 * @param src the src
	 */
	public LimitedObjectMapper(ObjectMapper src) {
		super(src);
	}

	@Override
	public LimitedObjectMapper copy() {
		return new LimitedObjectMapper(this);
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the limit.
	 *
	 * @param limit the limit to set
	 * @return the limited object mapper
	 */
	public LimitedObjectMapper setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
		LimitedByteArrayBuilder bb = new LimitedByteArrayBuilder(_jsonFactory._getBufferRecycler(), limit);
		try {
			_configAndWriteValue(_jsonFactory.createGenerator(bb, JsonEncoding.UTF8), value);
		} catch (JsonProcessingException e) { // to support [JACKSON-758]
			throw e;
		} catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
			throw JsonMappingException.fromUnexpectedIOE(e);
		}
		byte[] result = bb.toByteArray();
		bb.release();
		return result;
	}

	@Override
	public String writeValueAsString(Object value) throws JsonProcessingException {
		LimitedSegmentedStringWriter sw = new LimitedSegmentedStringWriter(_jsonFactory._getBufferRecycler(), limit);
		try {
			_configAndWriteValue(_jsonFactory.createGenerator(sw), value);
		} catch (JsonProcessingException e) {
			throw e;
		} catch (IOException e) {
			throw JsonMappingException.fromUnexpectedIOE(e);
		}
		return sw.getAndClear();
	}
}
