package com.github.lancethomps.lava.common.ser.jackson;

import static com.github.lancethomps.lava.common.lambda.Lambdas.actionIfTrue;
import static com.github.lancethomps.lava.common.logging.Logs.logWarn;
import static com.github.lancethomps.lava.common.ser.Serializer.isLogMissingProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.string.StringUtil;

public class CustomDeserializationProblemHandler extends DeserializationProblemHandler {

  private static final Logger LOG = Logger.getLogger(CustomDeserializationProblemHandler.class);
  private static Set<Class<?>> ignoreMissingInstantiators;
  private static Set<String> ignorePackages = new HashSet<>();
  private static Set<String> ignoreProperties = new HashSet<>();
  private static Set<Class<?>> ignoreTypes;

  public static void addIgnorePackage(String pkg) {
    ignorePackages.add(pkg);
  }

  public static void addIgnorePackages(Collection<String> packages) {
    ignorePackages.addAll(packages);
  }

  public static void addIgnoreProperties(Collection<String> properties) {
    ignoreProperties.addAll(properties);
  }

  public static void addIgnoreProperty(String pkg) {
    ignoreProperties.add(pkg);
  }

  public static Set<Class<?>> getIgnoreMissingInstantiators() {
    return ignoreMissingInstantiators;
  }

  public static void setIgnoreMissingInstantiators(Set<Class<?>> ignoreMissingInstantiators) {
    CustomDeserializationProblemHandler.ignoreMissingInstantiators = ignoreMissingInstantiators;
  }

  public static Set<String> getIgnorePackages() {
    return new HashSet<>(ignorePackages);
  }

  public static void setIgnorePackages(Set<String> ignorePackages) {
    CustomDeserializationProblemHandler.ignorePackages = ignorePackages == null ? new HashSet<>() : ignorePackages;
  }

  public static Set<String> getIgnoreProperties() {
    return new HashSet<>(ignoreProperties);
  }

  public static void setIgnoreProperties(Set<String> ignoreProperties) {
    CustomDeserializationProblemHandler.ignoreProperties = ignoreProperties == null ? new HashSet<>() : ignoreProperties;
  }

  public static Set<Class<?>> getIgnoreTypes() {
    return ignoreTypes;
  }

  public static void setIgnoreTypes(Set<Class<?>> ignoreTypes) {
    CustomDeserializationProblemHandler.ignoreTypes = ignoreTypes;
  }

  public static void removeIgnorePackage(String pkg) {
    ignorePackages.remove(pkg);
  }

  public static void removeIgnorePackages(Collection<String> packages) {
    ignorePackages.removeAll(packages);
  }

  public static void removeIgnoreProperties(Collection<String> properties) {
    ignoreProperties.removeAll(properties);
  }

  public static void removeIgnoreProperty(String pkg) {
    ignoreProperties.remove(pkg);
  }

  @Override
  public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass, JsonParser p, String msg) throws IOException {
    if ((ignoreMissingInstantiators != null) && ignoreMissingInstantiators.contains(instClass)) {
      Logs.logTrace(LOG, "Ignoring missing instantiator for class [%s]", instClass);
      return null;
    }
    Logs.logInfo(LOG, "Missing instantiator for [%s]", instClass);
    return NOT_HANDLED;
  }

  @Override
  public boolean handleUnknownProperty(
    DeserializationContext ctxt,
    JsonParser jp,
    JsonDeserializer<?> deserializer,
    Object beanOrClass,
    String propertyName
  ) throws IOException {
    Supplier<Boolean> skipSupplier = () -> {
      try {
        return skipAndReturnFalse(ctxt, jp, deserializer, beanOrClass, propertyName);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    };

    if (propertyName.startsWith("#")) {
      return skipSupplier.get();
    }
    if (!ignoreProperties.contains(propertyName)) {
      if (BeanDeserializerBase.class.isAssignableFrom(deserializer.getClass())) {
        if (tryCamelCaseProperty(ctxt, jp, (BeanDeserializerBase) deserializer, beanOrClass, propertyName)) {
          return true;
        }
      }
      Class<?> clazz = (beanOrClass instanceof Class) ? (Class<?>) beanOrClass : beanOrClass.getClass();
      if ((ignoreTypes != null) && ignoreTypes.contains(clazz)) {
        return skipSupplier.get();
      }
      for (String pkg : ignorePackages) {
        if (clazz.getName().startsWith(pkg)) {
          return skipSupplier.get();
        }
      }
      actionIfTrue(
        isLogMissingProperties(),
        () -> logWarn(LOG, JsonMappingException.from(ctxt, "Unknown Property"), "Unknown property: name=%s type=%s", propertyName, clazz.getName())
      );
    }
    return skipSupplier.get();
  }

  @Override
  public Object handleWeirdKey(DeserializationContext ctxt, Class<?> rawKeyType, String keyValue, String failureMsg) throws IOException {
    Logs.logWarn(LOG, "Handling weird key [%s of type %s]: %s", keyValue, rawKeyType, failureMsg);
    return null;
  }

  private boolean skipAndReturnFalse(
    DeserializationContext ctxt,
    JsonParser jp,
    JsonDeserializer<?> deserializer,
    Object beanOrClass,
    String propertyName
  ) throws IOException {
    jp.skipChildren();
    return false;
  }

  private boolean tryCamelCaseProperty(
    DeserializationContext ctxt,
    JsonParser jp,
    BeanDeserializerBase deserializer,
    Object beanOrClass,
    String propertyName
  ) throws IOException {
    String fixedName = StringUtil.fixCamelCase(propertyName);
    SettableBeanProperty beanProp = deserializer.findProperty(fixedName);
    if (beanProp != null) {
      try {
        beanProp.deserializeAndSet(jp, ctxt, beanOrClass);
      } catch (Exception e) {
        deserializer.wrapAndThrow(e, beanOrClass, fixedName, ctxt);
      }
      return true;
    } else if ((beanOrClass != null) && (beanOrClass instanceof UnknownFieldCollector)) {
      try {
        ((UnknownFieldCollector) beanOrClass).handleUnknownField(
          propertyName,
          ctxt.findRootValueDeserializer(ctxt.constructType(Object.class)).deserialize(jp, ctxt)
        );
        return true;
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Issue handling unknown field [%s] for UnknownFieldCollector [%s].", propertyName, beanOrClass.getClass());
      }
    }
    return false;
  }

}
