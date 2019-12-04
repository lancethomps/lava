package com.lancethomps.lava.common.ser.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class LimitedObjectMapper extends ObjectMapper {

  private static final long serialVersionUID = 1269892369733873933L;

  private int limit = -1;

  public LimitedObjectMapper() {
    super();
  }

  public LimitedObjectMapper(JsonFactory factory) {
    super(factory);
  }

  public LimitedObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc) {
    super(jf, sp, dc);
  }

  public LimitedObjectMapper(LimitedObjectMapper src) {
    super(src);
    limit = src.limit;
  }

  public LimitedObjectMapper(ObjectMapper src) {
    super(src);
  }

  @Override
  public LimitedObjectMapper copy() {
    return new LimitedObjectMapper(this);
  }

  public int getLimit() {
    return limit;
  }

  public LimitedObjectMapper setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  @Override
  public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
    LimitedByteArrayBuilder bb = new LimitedByteArrayBuilder(_jsonFactory._getBufferRecycler(), limit);
    try {
      _configAndWriteValue(_jsonFactory.createGenerator(bb, JsonEncoding.UTF8), value);
    } catch (JsonProcessingException e) {
      throw e;
    } catch (IOException e) {
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
