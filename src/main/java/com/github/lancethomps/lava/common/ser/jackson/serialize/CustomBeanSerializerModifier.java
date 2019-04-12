package com.github.lancethomps.lava.common.ser.jackson.serialize;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.MapType;

public class CustomBeanSerializerModifier extends BeanSerializerModifier {

  public CustomBeanSerializerModifier() {
    super();
  }

  @Override
  public JsonSerializer<?> modifyMapSerializer(
    SerializationConfig config,
    MapType valueType,
    BeanDescription beanDesc,
    JsonSerializer<?> serializer
  ) {
    return new CustomOrderedKeysMapSerializer((MapSerializer) serializer, config.getAnnotationIntrospector().findFilterId(beanDesc.getClassInfo()),
      config.isEnabled(ORDER_MAP_ENTRIES_BY_KEYS)
    );
  }

}
