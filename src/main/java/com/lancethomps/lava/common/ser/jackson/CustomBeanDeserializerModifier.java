package com.lancethomps.lava.common.ser.jackson;

import static com.lancethomps.lava.common.logging.Logs.logWarn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.AbstractDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.lancethomps.lava.common.ser.OutputExpression;
import com.lancethomps.lava.common.ser.PostConstructor;
import com.lancethomps.lava.common.ser.jackson.deserialize.CustomSingleValueBeanDeserializer;
import com.lancethomps.lava.common.ser.jackson.deserialize.EnumDeserializerWithCustomValues;

public class CustomBeanDeserializerModifier extends BeanDeserializerModifier {

  private static final Logger LOG = LogManager.getLogger(CustomBeanDeserializerModifier.class);

  @Override
  public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
    if (beanDesc.getBeanClass() == OutputExpression.class) {
      return new PostConstructDeserializer(new CustomSingleValueBeanDeserializer(deserializer, val -> new OutputExpression().setExpression(val)));
    } else if (PostConstructor.class.isAssignableFrom(beanDesc.getBeanClass()) && !(deserializer instanceof AbstractDeserializer)) {
      if (!(deserializer instanceof BeanDeserializerBase)) {
        logWarn(
          LOG,
          "Cannot create PostConstructDeserializer for PostConstruct class [%s] because JsonDeserializer is of type [%s] - need " +
            "BeanDeserializerBase!",
          beanDesc.getBeanClass(),
          deserializer.getClass()
        );
      } else {
        return new PostConstructDeserializer((BeanDeserializerBase) deserializer);
      }
    }
    return deserializer;
  }

  @Override
  public JsonDeserializer<?> modifyEnumDeserializer(
    DeserializationConfig config,
    JavaType type,
    BeanDescription beanDesc,
    JsonDeserializer<?> deserializer
  ) {
    if (deserializer instanceof EnumDeserializer) {
      return new EnumDeserializerWithCustomValues((EnumDeserializer) deserializer, true);
    }
    return deserializer;
  }

}
