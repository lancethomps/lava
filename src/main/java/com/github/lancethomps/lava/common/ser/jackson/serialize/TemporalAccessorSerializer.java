package com.github.lancethomps.lava.common.ser.jackson.serialize;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.github.lancethomps.lava.common.logging.Logs;

public class TemporalAccessorSerializer<T extends TemporalAccessor> extends StdScalarSerializer<T> {

  private static final Logger LOG = Logger.getLogger(TemporalAccessorSerializer.class);

  private static final long serialVersionUID = -5999799118350670886L;

  private final Function<T, ?> function;

  public TemporalAccessorSerializer(Class<T> type, Function<T, ?> function) {
    super(type);
    this.function = function;
  }

  public Function<T, ?> getFunction() {
    return function;
  }

  @Override
  public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (value != null) {
      try {
        Object out = function.apply(value);
        if (out instanceof Long) {
          gen.writeNumber((long) out);
        } else if (out instanceof Integer) {
          gen.writeNumber((int) out);
        } else {
          gen.writeString(out.toString());
        }
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Issue writing TemporalAccessor of type [%s]!", _handledType);
        gen.writeNull();
      }
    } else {
      gen.writeNull();
    }
  }

}
