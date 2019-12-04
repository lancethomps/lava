package com.lancethomps.lava.common.ser.jackson.filter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import com.google.common.collect.Sets;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.web.requests.MissingRequestParameter;

public class FieldsFilter extends SimpleBeanPropertyFilter implements Serializable {

  public static final String FIELDS_FILTER_ID = "fieldsFilter";

  private static final Set<String> EMPTY_FIELD_LIST = Collections.unmodifiableSet(new HashSet<String>());

  private static final Logger LOG = Logger.getLogger(FieldsFilter.class);
  private static final long serialVersionUID = 1L;
  private static Set<String> mandatoryFields = Sets.newHashSet(
    "allDataReturned",
    "debugMessage",
    "errorCodes",
    "failureReason",
    "identifiersWithoutFullData",
    "messages",
    "missingParameters",
    "success",
    "uniqueId"
  );
  private static Set<Class<?>> mandatoryFieldsViaDeclaringTypes = Sets.newHashSet(MissingRequestParameter.class);
  private Predicate<PropertyWriter> includePredicate;

  private SimpleBeanPropertyFilter serializeAsFieldFilter;

  public FieldsFilter() {
    super();
  }

  public FieldsFilter(@Nullable final Collection<Pattern> whiteList, @Nullable final Collection<Pattern> blackList) {
    super();
    addFilter(whiteList, blackList);
  }

  public FieldsFilter(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
    super();
    addFilter(propertiesToExcludeByType);
  }

  public FieldsFilter(final Predicate<PropertyWriter> includePredicate, SimpleBeanPropertyFilter serializeAsFieldFilter) {
    super();
    this.includePredicate = includePredicate;
    this.serializeAsFieldFilter = serializeAsFieldFilter;
  }

  public FieldsFilter(final Set<String> properties) {
    super();
    addFilter(properties);
  }

  public FieldsFilter(final String graphFilter) {
    super();
    addGraphFilter(graphFilter);
  }

  public static boolean checkForMandatoryFieldInclusion(@Nonnull PropertyWriter writer) {
    Class<?> declaringType;
    if ((mandatoryFields != null) && mandatoryFields.contains(writer.getName())) {
      return true;
    } else {
      return (mandatoryFieldsViaDeclaringTypes != null) && (writer.getMember() != null) &&
        ((declaringType = writer.getMember().getDeclaringClass()) != null)
        && mandatoryFieldsViaDeclaringTypes.stream().anyMatch(check -> check.isAssignableFrom(declaringType));
    }
  }

  public static FieldsFilter fromOnlyFields(final Set<String> properties) {
    return new FieldsFilter().addOnlyFilter(properties);
  }

  public static FieldsFilter fromSkipFields(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
    return new FieldsFilter().addFilter(propertiesToExcludeByType);
  }

  public static FieldsFilter fromSkipFields(final Set<String> properties) {
    return new FieldsFilter().addFilter(properties);
  }

  public static Set<String> getMandatoryFields() {
    return mandatoryFields;
  }

  public static void setMandatoryFields(Set<String> mandatoryFields) {
    FieldsFilter.mandatoryFields = mandatoryFields;
  }

  public static Set<Class<?>> getMandatoryFieldsViaDeclaringTypes() {
    return mandatoryFieldsViaDeclaringTypes;
  }

  public static void setMandatoryFieldsViaDeclaringTypes(Set<Class<?>> mandatoryFieldsViaDeclaringTypes) {
    FieldsFilter.mandatoryFieldsViaDeclaringTypes = mandatoryFieldsViaDeclaringTypes;
  }

  public FieldsFilter addFieldInclusionPredicateFilter(final Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
    serializeAsFieldFilter = new FieldInclusionPredicateFilter(predicateByType);
    return this;
  }

  public FieldsFilter addFilter(@Nullable final Collection<Pattern> whiteList, @Nullable final Collection<Pattern> blackList) {
    addOrSetPredicate(writer -> Checks.passesWhiteAndBlackListCheck(writer.getName(), whiteList, blackList).getLeft());
    return this;
  }

  public FieldsFilter addFilter(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
    addOrSetPredicate(writer -> {
      Class<?> type = writer instanceof BeanPropertyWriter ? writer.getMember().getMember().getDeclaringClass() :
        writer.getType().getRawClass();
      return !propertiesToExcludeByType.getOrDefault(type, EMPTY_FIELD_LIST).contains(writer.getName());
    });
    return this;
  }

  public FieldsFilter addFilter(final Set<String> properties) {
    addOrSetPredicate(writer -> !properties.contains(writer.getName()));
    return this;
  }

  public FieldsFilter addGraphFilter(final String graphFilter) {
    serializeAsFieldFilter = new SquigglyPropertyFilter(new SimpleSquigglyContextProvider(new SquigglyParser(), graphFilter));
    return this;
  }

  public FieldsFilter addOnlyFilter(final Set<String> properties) {
    addOrSetPredicate(writer -> properties.contains(writer.getName()));
    return this;
  }

  public FieldsFilter addOrSetPredicate(Predicate<PropertyWriter> includePredicate) {
    if (includePredicate != null) {
      if (this.includePredicate == null) {
        this.includePredicate = includePredicate;
      } else {
        final Predicate<PropertyWriter> current = this.includePredicate;
        this.includePredicate = writer -> current.test(writer) && includePredicate.test(writer);
      }
    }
    return this;
  }

  public FieldsFilter copy() {
    return new FieldsFilter(includePredicate, serializeAsFieldFilter);
  }

  public Predicate<PropertyWriter> getIncludePredicate() {
    return includePredicate;
  }

  public SimpleBeanPropertyFilter getSerializeAsFieldFilter() {
    return serializeAsFieldFilter;
  }

  public FieldsFilter setSerializeAsFieldFilter(SimpleBeanPropertyFilter serializeAsFieldFilter) {
    this.serializeAsFieldFilter = serializeAsFieldFilter;
    return this;
  }

  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
    if (serializeAsFieldFilter != null) {
      if (checkForMandatoryFieldInclusion(writer)) {
        super.serializeAsField(pojo, jgen, provider, writer);
      } else if ((includePredicate == null) || includePredicate.test(writer)) {
        serializeAsFieldFilter.serializeAsField(pojo, jgen, provider, writer);
      } else if (!jgen.canOmitFields()) {
        writer.serializeAsOmittedField(pojo, jgen, provider);
      }
    } else {
      super.serializeAsField(pojo, jgen, provider, writer);
    }
  }

  @Override
  protected boolean include(BeanPropertyWriter writer) {
    // TODO: decide whether to check for mandatory field inclusion here
    return (includePredicate == null) || includePredicate.test(writer);
  }

  @Override
  protected boolean include(PropertyWriter writer) {
    // TODO: decide whether to check for mandatory field inclusion here
    return (includePredicate == null) || includePredicate.test(writer);
  }

}
