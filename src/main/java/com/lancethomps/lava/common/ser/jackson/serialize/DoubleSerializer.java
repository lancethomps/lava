package com.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.lancethomps.lava.common.math.Numbers;

public class DoubleSerializer extends JsonSerializer<Double> {

  private final int sigFigs;

  public DoubleSerializer(int sigFigs) {
    this.sigFigs = sigFigs;
  }

  @Override
  public void serialize(Double val, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (val != null) {
      jsonGenerator.writeNumber(Numbers.round(val, sigFigs));
    } else {
      jsonGenerator.writeNull();
    }
  }

}
