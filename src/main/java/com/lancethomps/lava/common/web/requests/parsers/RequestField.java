package com.lancethomps.lava.common.web.requests.parsers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, TYPE})
@Retention(RUNTIME)
public @interface RequestField {

  String DEFAULT = "#default";

  String[] additionalParameterNames() default "";

  boolean adminOnly() default false;

  boolean enabled() default true;

  boolean includeTypeFields() default false;

  boolean internalOnly() default false;

  boolean postProcess() default false;

  String prefix() default "";

  boolean prefixOnly() default false;

  boolean prefixOrJson() default false;

  @SuppressWarnings("rawtypes")
  Class<? extends RequestParameterParser> processUsing() default DefaultRequestParameterParser.class;

  boolean validateField() default false;

  Class<? extends RequestParameterValidator> validateUsing() default NoOpRequestParameterValidator.class;

  String value() default DEFAULT;

}
