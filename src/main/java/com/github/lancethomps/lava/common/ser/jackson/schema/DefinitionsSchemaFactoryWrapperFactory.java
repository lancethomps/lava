package com.github.lancethomps.lava.common.ser.jackson.schema;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;

public class DefinitionsSchemaFactoryWrapperFactory extends WrapperFactory {

  @Override
  public SchemaFactoryWrapper getWrapper(SerializerProvider p) {
    SchemaFactoryWrapper wrapper = new DefinitionsSchemaFactory();
    if (p != null) {
      wrapper.setProvider(p);
    }
    return wrapper;
  }

  @Override
  public SchemaFactoryWrapper getWrapper(SerializerProvider p, VisitorContext rvc) {
    SchemaFactoryWrapper wrapper = new DefinitionsSchemaFactory();
    if (p != null) {
      wrapper.setProvider(p);
    }
    wrapper.setVisitorContext(rvc);
    return wrapper;
  }

}
