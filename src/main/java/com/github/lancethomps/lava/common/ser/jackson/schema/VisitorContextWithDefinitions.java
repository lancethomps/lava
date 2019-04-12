package com.github.lancethomps.lava.common.ser.jackson.schema;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;

public class VisitorContextWithDefinitions extends VisitorContext {

  private final Map<String, JsonSchema> definitions = new LinkedHashMap<>();

  private final HashSet<JavaType> seenSchemas = new HashSet<JavaType>();

  public void addDefinition(String id, JsonSchema schema) {
    definitions.put(id, schema);
  }

  @Override
  public String addSeenSchemaUri(JavaType aSeenSchema) {
    if ((aSeenSchema != null) && !aSeenSchema.isPrimitive()) {
      seenSchemas.add(aSeenSchema);
      return javaTypeToUrn(aSeenSchema);
    }
    return null;
  }

  public Map<String, JsonSchema> getDefinitions() {
    return definitions;
  }

  @Override
  public String getSeenSchemaUri(JavaType aSeenSchema) {
    return (seenSchemas.contains(aSeenSchema)) ? javaTypeToUrn(aSeenSchema) : null;
  }

  @Override
  public String javaTypeToUrn(JavaType jt) {
    return "java:" + jt.toCanonical();
  }

}
