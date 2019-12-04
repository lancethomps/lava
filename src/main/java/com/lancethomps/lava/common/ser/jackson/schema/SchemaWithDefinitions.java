package com.lancethomps.lava.common.ser.jackson.schema;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.lancethomps.lava.common.ser.Serializer;

public class SchemaWithDefinitions {

  private Map<String, JsonSchema> definitions;

  private ObjectSchema schema;

  public SchemaWithDefinitions() {
    super();
  }

  public SchemaWithDefinitions(ObjectSchema schema, Map<String, JsonSchema> definitions) {
    super();
    this.schema = schema;
    this.definitions = definitions;
  }

  public Map<String, JsonSchema> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(Map<String, JsonSchema> definitions) {
    this.definitions = definitions;
  }

  public ObjectSchema getSchema() {
    return schema;
  }

  public void setSchema(ObjectSchema schema) {
    this.schema = schema;
  }

  public Map<String, ObjectNode> toProperties() {
    return (schema == null) || (schema.getProperties() == null) ? null
      : schema.getProperties().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> (ObjectNode) Serializer.toTree(e.getValue())));
  }

}
