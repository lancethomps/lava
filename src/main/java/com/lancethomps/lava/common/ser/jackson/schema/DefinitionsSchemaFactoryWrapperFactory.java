package com.lancethomps.lava.common.ser.jackson.schema;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;

public class DefinitionsSchemaFactoryWrapperFactory extends WrapperFactory {

  @Override
  public SchemaFactoryWrapper getWrapper(SerializerProvider provider) {
    return new DefinitionsSchemaFactory(provider);
  }

  @Override
  public SchemaFactoryWrapper getWrapper(SerializerProvider provider, VisitorContext rvc) {
    SchemaFactoryWrapper wrapper = new DefinitionsSchemaFactory(provider);
    wrapper.setVisitorContext(rvc);
    return wrapper;
  }

}
