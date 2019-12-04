package com.lancethomps.lava.common.ser.jackson;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.PostConstructor;

public class PostConstructDeserializer extends BeanDeserializer {

  private static final Logger LOG = Logger.getLogger(PostConstructDeserializer.class);

  private static final long serialVersionUID = 8436965950483899743L;

  private final BeanDeserializerBase deserializer;

  public PostConstructDeserializer(BeanDeserializerBase deserializer) {
    super(deserializer);
    this.deserializer = deserializer;
  }

  @Override
  public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    Object result = deserializer.deserialize(jp, ctxt);
    callAfterDeserialization(result);
    return result;
  }

  @Override
  public Object deserialize(JsonParser jp, DeserializationContext ctxt, Object intoValue) throws IOException {
    Object result = deserializer.deserialize(jp, ctxt, intoValue);
    callAfterDeserialization(result);
    return result;
  }

  @Override
  public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
    throws IOException {
    Object result = deserializer.deserializeWithType(jp, ctxt, typeDeserializer);
    callAfterDeserialization(result);
    return result;
  }

  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    deserializer.resolve(ctxt);
  }

  private void callAfterDeserialization(Object result) {
    if (result instanceof PostConstructor) {
      Logs.logTrace(LOG, "PostConstructor.afterDeserialization for type [%s]", result.getClass());
      ((PostConstructor) result).afterDeserialization();
    }
  }

}
