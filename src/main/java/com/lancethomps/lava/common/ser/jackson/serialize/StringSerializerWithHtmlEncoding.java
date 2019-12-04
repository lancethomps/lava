package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

public class StringSerializerWithHtmlEncoding extends StdScalarSerializer<Object> {

  private static final Encoder HTML_ENCODER = ESAPI.encoder();

  private static final long serialVersionUID = 1L;

  public StringSerializerWithHtmlEncoding() {
    super(String.class, false);
  }

  @Override
  public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    gen.writeString(HTML_ENCODER.encodeForHTML((String) value));
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(HTML_ENCODER.encodeForHTML((String) value));
  }

}
