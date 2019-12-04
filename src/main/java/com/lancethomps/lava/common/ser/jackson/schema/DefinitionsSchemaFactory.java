package com.lancethomps.lava.common.ser.jackson.schema;

import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.ArrayVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;

public class DefinitionsSchemaFactory extends SchemaFactoryWrapper {

  public DefinitionsSchemaFactory() {
    this(null);
  }

  public DefinitionsSchemaFactory(SerializerProvider provider) {
    super(provider, new DefinitionsSchemaFactoryWrapperFactory());
    visitorContext = new VisitorContextWithDefinitions();
  }

  @Override
  public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
    ArrayVisitor visitor = ((ArrayVisitor) super.expectArrayFormat(convertedType));
    addInfo(visitor.getSchema(), convertedType);
    return visitor;
  }

  @Override
  public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
    ObjectVisitor visitor = ((ObjectVisitor) super.expectObjectFormat(convertedType));
    addInfo(visitor.getSchema(), convertedType);
    return visitor;
  }

  public Map<String, JsonSchema> getDefinitions() {
    return ((VisitorContextWithDefinitions) visitorContext).getDefinitions();
  }

  private void addInfo(JsonSchema schema, JavaType type) {
    if (!schema.isSimpleTypeSchema()) {
      throw new RuntimeException("given non simple type schema: " + schema.getType());
    }
    if (!(schema instanceof ReferenceSchema) && (schema.getId() != null)) {
      ((VisitorContextWithDefinitions) visitorContext).addDefinition(schema.getId(), schema);
    }
    schema.asSimpleTypeSchema().setTitle(type.getRawClass().getSimpleName());
  }

}
