package com.github.lancethomps.lava.common.ser.jackson.schema;

import static com.github.lancethomps.lava.common.ser.Serializer.JSON_MAPPER;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.lancethomps.lava.common.logging.Logs;

public class SchemaFactory {

  private static final Logger LOG = Logger.getLogger(SchemaFactory.class);

  public static JsonSchema generateJsonSchema(Class<?> clazz) {
    try {
      SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
      JSON_MAPPER.acceptJsonFormatVisitor(JSON_MAPPER.constructType(clazz), visitor);
      return visitor.finalSchema();
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error generating JSON schema for class [%s]", clazz);
    }
    return null;
  }

}
