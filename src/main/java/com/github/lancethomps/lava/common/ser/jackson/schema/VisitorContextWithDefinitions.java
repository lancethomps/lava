package com.github.lancethomps.lava.common.ser.jackson.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;

public class VisitorContextWithDefinitions extends VisitorContext {

  private final Map<String, JsonSchema> definitions = new LinkedHashMap<>();

  public void addDefinition(String id, JsonSchema schema) {
    definitions.put(id, schema);
  }

  public Map<String, JsonSchema> getDefinitions() {
    return definitions;
  }

  @Override
  public String javaTypeToUrn(JavaType jt) {
    return jt.getRawClass().getSimpleName();
  }

}
