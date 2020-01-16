package com.lancethomps.lava.common.ser.jackson.schema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.Serializer;

public class SchemaFactory {

  private static final Logger LOG = LogManager.getLogger(SchemaFactory.class);

  public static JsonSchema generateJsonSchema(Class<?> clazz) {
    try {
      SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
      Serializer.JSON_MAPPER.acceptJsonFormatVisitor(Serializer.JSON_MAPPER.constructType(clazz), visitor);
      return visitor.finalSchema();
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error generating JSON schema for class [%s]", clazz);
    }
    return null;
  }

}
