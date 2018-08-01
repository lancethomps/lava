package com.github.lancethomps.lava.common.ser.jackson.filter;

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

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.web.requests.MissingRequestParameter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import com.google.common.collect.Sets;

/**
 * The Class FieldsFilter.
 *
 * @author lancethomps
 */
public class FieldsFilter extends SimpleBeanPropertyFilter implements Serializable {

	/** The Constant FIELDS_FILTER_ID. */
	public static final String FIELDS_FILTER_ID = "fieldsFilter";

	/** The Constant EMPTY_FIELD_LIST. */
	private static final Set<String> EMPTY_FIELD_LIST = Collections.unmodifiableSet(new HashSet<String>());

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(FieldsFilter.class);

	/** The mandatory fields. */
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

	/** The mandatory fields via declaring types. */
	private static Set<Class<?>> mandatoryFieldsViaDeclaringTypes = Sets.newHashSet(MissingRequestParameter.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The include predicate. */
	private Predicate<PropertyWriter> includePredicate;

	/** The serialize as field filter. */
	private SimpleBeanPropertyFilter serializeAsFieldFilter;

	/**
	 * Instantiates a new jackson exclude fields filter.
	 */
	public FieldsFilter() {
		super();
	}

	/**
	 * Instantiates a new fields filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 */
	public FieldsFilter(@Nullable final Collection<Pattern> whiteList, @Nullable final Collection<Pattern> blackList) {
		super();
		addFilter(whiteList, blackList);
	}

	/**
	 * Instantiates a new jackson exclude fields filter.
	 *
	 * @param propertiesToExcludeByType the properties to exclude by type
	 */
	public FieldsFilter(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
		super();
		addFilter(propertiesToExcludeByType);
	}

	/**
	 * Instantiates a new jackson exclude fields filter.
	 *
	 * @param includePredicate the include predicate
	 * @param serializeAsFieldFilter the serialize as field filter
	 */
	public FieldsFilter(final Predicate<PropertyWriter> includePredicate, SimpleBeanPropertyFilter serializeAsFieldFilter) {
		super();
		this.includePredicate = includePredicate;
		this.serializeAsFieldFilter = serializeAsFieldFilter;
	}

	/**
	 * Instantiates a new jackson exclude fields filter.
	 *
	 * @param properties the properties
	 */
	public FieldsFilter(final Set<String> properties) {
		super();
		addFilter(properties);
	}

	/**
	 * Instantiates a new fields filter.
	 *
	 * @param graphFilter the graph filter
	 */
	public FieldsFilter(final String graphFilter) {
		super();
		addGraphFilter(graphFilter);
	}

	/**
	 * Check for mandatory field inclusion.
	 *
	 * @param writer the writer
	 * @return true, if successful
	 */
	public static boolean checkForMandatoryFieldInclusion(@Nonnull PropertyWriter writer) {
		Class<?> declaringType;
		if ((mandatoryFields != null) && mandatoryFields.contains(writer.getName())) {
			return true;
		} else if ((mandatoryFieldsViaDeclaringTypes != null) && (writer.getMember() != null) && ((declaringType = writer.getMember().getDeclaringClass()) != null)
			&& mandatoryFieldsViaDeclaringTypes.stream().anyMatch(check -> check.isAssignableFrom(declaringType))) {
			return true;
		}
		return false;
	}

	/**
	 * From only fields.
	 *
	 * @param properties the properties
	 * @return the jackson exclude fields filter
	 */
	public static FieldsFilter fromOnlyFields(final Set<String> properties) {
		return new FieldsFilter().addOnlyFilter(properties);
	}

	/**
	 * From skip fields.
	 *
	 * @param propertiesToExcludeByType the properties to exclude by type
	 * @return the jackson exclude fields filter
	 */
	public static FieldsFilter fromSkipFields(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
		return new FieldsFilter().addFilter(propertiesToExcludeByType);
	}

	/**
	 * From skip fields.
	 *
	 * @param properties the properties
	 * @return the jackson exclude fields filter
	 */
	public static FieldsFilter fromSkipFields(final Set<String> properties) {
		return new FieldsFilter().addFilter(properties);
	}

	/**
	 * @return the mandatoryFields
	 */
	public static Set<String> getMandatoryFields() {
		return mandatoryFields;
	}

	/**
	 * @return the mandatoryFieldsViaDeclaringTypes
	 */
	public static Set<Class<?>> getMandatoryFieldsViaDeclaringTypes() {
		return mandatoryFieldsViaDeclaringTypes;
	}

	/**
	 * @param mandatoryFields the mandatoryFields to set
	 */
	public static void setMandatoryFields(Set<String> mandatoryFields) {
		FieldsFilter.mandatoryFields = mandatoryFields;
	}

	/**
	 * @param mandatoryFieldsViaDeclaringTypes the mandatoryFieldsViaDeclaringTypes to set
	 */
	public static void setMandatoryFieldsViaDeclaringTypes(Set<Class<?>> mandatoryFieldsViaDeclaringTypes) {
		FieldsFilter.mandatoryFieldsViaDeclaringTypes = mandatoryFieldsViaDeclaringTypes;
	}

	/**
	 * Adds the function by type filter.
	 *
	 * @param predicateByType the predicate by type
	 * @return the fields filter
	 */
	public FieldsFilter addFieldInclusionPredicateFilter(final Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
		serializeAsFieldFilter = new FieldInclusionPredicateFilter(predicateByType);
		return this;
	}

	/**
	 * Adds the filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return the fields filter
	 */
	public FieldsFilter addFilter(@Nullable final Collection<Pattern> whiteList, @Nullable final Collection<Pattern> blackList) {
		addOrSetPredicate(writer -> Checks.passesWhiteAndBlackListCheck(writer.getName(), whiteList, blackList).getLeft());
		return this;
	}

	/**
	 * Adds the filter.
	 *
	 * @param propertiesToExcludeByType the properties to exclude by type
	 * @return the jackson exclude fields filter
	 */
	public FieldsFilter addFilter(final Map<Class<?>, Set<String>> propertiesToExcludeByType) {
		addOrSetPredicate(writer -> {
			Class<?> type = writer instanceof BeanPropertyWriter ? ((BeanPropertyWriter) writer).getMember().getMember().getDeclaringClass() : writer.getType().getRawClass();
			return !propertiesToExcludeByType.getOrDefault(type, EMPTY_FIELD_LIST).contains(writer.getName());
		});
		return this;
	}

	/**
	 * Adds the filter.
	 *
	 * @param properties the properties
	 * @return the jackson exclude fields filter
	 */
	public FieldsFilter addFilter(final Set<String> properties) {
		addOrSetPredicate(writer -> !properties.contains(writer.getName()));
		return this;
	}

	/**
	 * Adds the graph filter.
	 *
	 * @param graphFilter the graph filter
	 * @return the fields filter
	 */
	public FieldsFilter addGraphFilter(final String graphFilter) {
		serializeAsFieldFilter = new SquigglyPropertyFilter(new SimpleSquigglyContextProvider(new SquigglyParser(), graphFilter));
		return this;
	}

	/**
	 * Adds the only filter.
	 *
	 * @param properties the properties
	 * @return the jackson exclude fields filter
	 */
	public FieldsFilter addOnlyFilter(final Set<String> properties) {
		addOrSetPredicate(writer -> properties.contains(writer.getName()));
		return this;
	}

	/**
	 * Adds the or set predicate.
	 *
	 * @param includePredicate the include predicate
	 * @return the jackson exclude fields filter
	 */
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

	/**
	 * Copy.
	 *
	 * @return the jackson exclude fields filter
	 */
	public FieldsFilter copy() {
		return new FieldsFilter(includePredicate, serializeAsFieldFilter);
	}

	/**
	 * @return the includePredicate
	 */
	public Predicate<PropertyWriter> getIncludePredicate() {
		return includePredicate;
	}

	/**
	 * @return the serializeAsFieldFilter
	 */
	public SimpleBeanPropertyFilter getSerializeAsFieldFilter() {
		return serializeAsFieldFilter;
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

	/**
	 * Sets the serialize as field filter.
	 *
	 * @param serializeAsFieldFilter the serializeAsFieldFilter to set
	 * @return the fields filter
	 */
	public FieldsFilter setSerializeAsFieldFilter(SimpleBeanPropertyFilter serializeAsFieldFilter) {
		this.serializeAsFieldFilter = serializeAsFieldFilter;
		return this;
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
