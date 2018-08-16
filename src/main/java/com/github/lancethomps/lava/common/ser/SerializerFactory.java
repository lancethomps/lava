package com.github.lancethomps.lava.common.ser;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static com.github.lancethomps.lava.common.logging.Logs.logWarn;
import static com.github.lancethomps.lava.common.ser.Serializer.CSV_MAPPER;
import static com.github.lancethomps.lava.common.ser.jackson.filter.FieldsFilter.FIELDS_FILTER_ID;
import static com.fasterxml.jackson.core.JsonParser.NumberType.DOUBLE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.collections.FastHashMap;
import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.logging.SplunkMessageIdCommon;
import com.github.lancethomps.lava.common.math.Numbers;
import com.github.lancethomps.lava.common.ser.csv.CsvAnnotationIntrospector;
import com.github.lancethomps.lava.common.ser.csv.CsvPropertyFilter;
import com.github.lancethomps.lava.common.ser.jackson.CustomBeanDeserializerModifier;
import com.github.lancethomps.lava.common.ser.jackson.CustomDeserializationProblemHandler;
import com.github.lancethomps.lava.common.ser.jackson.CustomHandlerInstantiator;
import com.github.lancethomps.lava.common.ser.jackson.CustomTreeMapCaseInsensitive;
import com.github.lancethomps.lava.common.ser.jackson.LimitedObjectMapper;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.BooleanDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.ClassDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.ClassKeyDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.DateDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.PatternDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.TemporalAccessorDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.TemporalAccessorKeyDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.deserialize.ZoneOffsetDeserializer;
import com.github.lancethomps.lava.common.ser.jackson.filter.FieldInclusionPredicateSerializerModifier;
import com.github.lancethomps.lava.common.ser.jackson.filter.FieldsFilter;
import com.github.lancethomps.lava.common.ser.jackson.filter.JacksonAnnotationIntrospectorSingleFilter;
import com.github.lancethomps.lava.common.ser.jackson.mixins.QnameMixIn;
import com.github.lancethomps.lava.common.ser.jackson.mixins.StringBufferMixIn;
import com.github.lancethomps.lava.common.ser.jackson.modules.CustomHealthCheckModule;
import com.github.lancethomps.lava.common.ser.jackson.modules.PairModule;
import com.github.lancethomps.lava.common.ser.jackson.serialize.BigDecimalSerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.CustomBeanSerializerModifier;
import com.github.lancethomps.lava.common.ser.jackson.serialize.CustomDoubleSerializerWithFiniteCheck;
import com.github.lancethomps.lava.common.ser.jackson.serialize.CustomFunctionalSerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.DateSerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.GenericKeySerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.NumberWithLimitSerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.StringSerializerWithHtmlEncoding;
import com.github.lancethomps.lava.common.ser.jackson.serialize.TemporalAccessorKeySerializer;
import com.github.lancethomps.lava.common.ser.jackson.serialize.TemporalAccessorSerializer;
import com.github.lancethomps.lava.common.ser.jackson.types.CustomTypeResolver;
import com.github.lancethomps.lava.common.ser.jackson.types.NoopTypeAnnotationIntrospector;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.SmileParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.dropwizard.metrics5.json.MetricsModule;

/**
 * A factory for creating Serializer objects.
 */
public class SerializerFactory {

	/** The Constant CSV_FILTER. */
	public static final String CSV_FILTER = "csvFilter";

	/** The Constant DATE_MODULE_ID. */
	public static final String DATE_MODULE_ID = "WTPDateModule";

	/** The json char cache limit. */
	private static long jsonCharCacheLimit = NumberUtils.toLong(System.getProperty("wtp.jsonCharCacheLimit"), 20 * 1024 * 1024);

	/** The Constant JSON_CHAR_LIMIT. */
	private static int jsonCharLimit = NumberUtils.toInt(System.getProperty("wtp.jsonCharLimit"), 100 * 1024 * 1024);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SerializerFactory.class);

	/** The Constant OBJECT_MAPPER_CACHE. */
	private static final FastHashMap<String, ObjectMapper> OBJECT_MAPPER_CACHE = new FastHashMap<>();

	/** The Constant REGISTERED_MAPPERS. */
	private static final List<ObjectMapper> REGISTERED_MAPPERS = new ArrayList<>();

	/** The Constant REGISTERED_MODIFIERS. */
	private static final List<Consumer<ObjectMapper>> REGISTERED_MODIFIERS = new ArrayList<>();

	/** The Constant REGISTERED_MODULES. */
	private static final List<Module> REGISTERED_MODULES = new ArrayList<>();

	/** The Constant UNTYPE_SCHEMAS. */
	private static final LRUMap<JavaType, CsvSchema> UNTYPE_SCHEMAS = new LRUMap<>(8, 32);

	/** The use default schemas. */
	private static boolean useDefaultSchemas;

	/** The use object mapper cache. */
	private static boolean useObjectMapperCache = BooleanUtils.toBoolean(System.getProperty("wtp.useObjectMapperCache", "true"));

	/**
	 * Adds the dates as strings.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the t
	 */
	public static <T extends ObjectMapper> T addDatesAsStrings(@Nonnull T mapper) {
		return addDatesAsStrings(mapper, null);
	}

	/**
	 * Adds the dates as strings.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param dateFormat the date format
	 * @return the t
	 */
	public static <T extends ObjectMapper> T addDatesAsStrings(@Nonnull T mapper, @Nullable String dateFormat) {
		SimpleModule dateModule = getDateModule(mapper, true, dateFormat);
		mapper.registerModule(dateModule);
		return mapper;
	}

	/**
	 * Adds the function by type filter.
	 *
	 * @param mapper the mapper
	 * @param predicateByType the predicate by type
	 * @return the object mapper
	 */
	public static ObjectMapper addFieldInclusionPredicateFilter(ObjectMapper mapper, final Map<Class<?>, BiPredicate<Object, Object>> predicateByType) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addFieldInclusionPredicateFilter(predicateByType)
			: new FieldsFilter().addFieldInclusionPredicateFilter(predicateByType);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		mapper.registerModule(new SimpleModule("WTPFieldInclusionPredicateModule").setSerializerModifier(new FieldInclusionPredicateSerializerModifier(predicateByType)));
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the fields white and black list.
	 *
	 * @param mapper the mapper
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return the object mapper
	 */
	public static ObjectMapper addFieldsWhiteAndBlackList(ObjectMapper mapper, @Nullable final Collection<Pattern> whiteList, @Nullable final Collection<Pattern> blackList) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addFilter(whiteList, blackList) : new FieldsFilter(whiteList, blackList);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the graph filter.
	 *
	 * @param mapper the mapper
	 * @param graphFilter the graph filter
	 * @return the object mapper
	 */
	public static ObjectMapper addGraphFilter(ObjectMapper mapper, String graphFilter) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addGraphFilter(graphFilter) : new FieldsFilter(graphFilter);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the html encoding string serializer.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the t
	 */
	public static <T extends ObjectMapper> T addHtmlEncodingStringSerializer(T mapper) {
		SimpleModule module = new SimpleModule("WTPStringHtmlEncoderModule");
		module.addSerializer(String.class, new StringSerializerWithHtmlEncoding());
		mapper.registerModule(module);
		return mapper;
	}

	/**
	 * Adds the user modules.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static <T extends ObjectMapper> T addModules(T mapper) {
		SimpleModule module = new SimpleModule("WTPMiscModule");
		module.addDeserializer(Boolean.class, new BooleanDeserializer(Boolean.class, Boolean.FALSE));
		module.addDeserializer(Boolean.TYPE, new BooleanDeserializer(Boolean.TYPE, null));
		module.addDeserializer(Class.class, new ClassDeserializer()).addKeyDeserializer(Class.class, new ClassKeyDeserializer());
		module.addDeserializer(Pattern.class, new PatternDeserializer());
		module.addSerializer(Double.class, new CustomDoubleSerializerWithFiniteCheck(Double.class));
		module.addSerializer(Double.TYPE, new CustomDoubleSerializerWithFiniteCheck(Double.TYPE));
		mapper.registerModule(module);
		mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false).addFilter("translation", SimpleBeanPropertyFilter.serializeAllExcept()));

		mapper.registerModule(new SimpleModule("WTPKeyModule").addKeySerializer(Object.class, new GenericKeySerializer()));

		mapper.registerModule(
			new SimpleModule("WTPSingleValueModule").setDeserializerModifier(new CustomBeanDeserializerModifier()).setSerializerModifier(new CustomBeanSerializerModifier())
		);

		SimpleModule dateModule = getDateModule(mapper, false);
		mapper.registerModule(dateModule);

		mapper.registerModule(new PairModule());
		mapper.registerModule(new AfterburnerModule());
		mapper.registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false));
		mapper.registerModule(new CustomHealthCheckModule());

		mapper.addMixIn(StringBuffer.class, StringBufferMixIn.class);
		mapper.addMixIn(QName.class, QnameMixIn.class);

		return mapper;
	}

	/**
	 * Adds the noop typing.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the t
	 */
	public static <T extends ObjectMapper> T addNoopTyping(T mapper) {
		mapper.setAnnotationIntrospector(new NoopTypeAnnotationIntrospector());
		return mapper;
	}

	/**
	 * Adds the one time only fields.
	 *
	 * @param mapper the mapper
	 * @param onlyFields the ignore fields
	 * @return the object mapper
	 */
	public static ObjectMapper addOneTimeOnlyFields(ObjectMapper mapper, Set<String> onlyFields) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addOnlyFilter(onlyFields) : FieldsFilter.fromOnlyFields(onlyFields);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the one time skip fields.
	 *
	 * @param mapper the mapper
	 * @param ignoreFieldsByType the ignore fields by type
	 * @return the object mapper
	 */
	public static ObjectMapper addOneTimeSkipFields(ObjectMapper mapper, Map<Class<?>, Set<String>> ignoreFieldsByType) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addFilter(ignoreFieldsByType) : new FieldsFilter(ignoreFieldsByType);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the one time skip fields.
	 *
	 * @param mapper the mapper
	 * @param ignoreFields the ignore fields
	 * @return the object mapper
	 */
	public static ObjectMapper addOneTimeSkipFields(ObjectMapper mapper, Set<String> ignoreFields) {
		SimpleFilterProvider filter = new SimpleFilterProvider().setFailOnUnknownId(false);
		FieldsFilter currFilter = mapper.getSerializationConfig().getFilterProvider() == null ? null
			: (FieldsFilter) ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider()).setFailOnUnknownId(false).findPropertyFilter(FIELDS_FILTER_ID, null);
		FieldsFilter oneTimeFilter = currFilter != null ? currFilter.copy().addFilter(ignoreFields) : new FieldsFilter(ignoreFields);
		filter.addFilter(FIELDS_FILTER_ID, oneTimeFilter);
		mapper.setFilterProvider(filter);
		return configureMapperWithFilterId(mapper, FIELDS_FILTER_ID);
	}

	/**
	 * Adds the ordered map config.
	 *
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static ObjectMapper addOrderedMapConfig(ObjectMapper mapper) {
		return mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}

	/**
	 * Adds the pretty printer.
	 *
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static ObjectMapper addPrettyPrinter(ObjectMapper mapper) {
		return mapper.setDefaultPrettyPrinter(getPrettyPrinter()).configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	/**
	 * Adds the sig figs limit.
	 *
	 * @param mapper the mapper
	 * @param limit the limit
	 * @return the object mapper
	 */
	public static ObjectMapper addSigFigsLimit(ObjectMapper mapper, int limit) {
		SimpleModule module = new SimpleModule("WTPBigDecimalModule");
		module.addSerializer(BigDecimal.class, new BigDecimalSerializer(limit));
		NumberWithLimitSerializer<Double> doubleSer = new NumberWithLimitSerializer<>(Double.class, DOUBLE, (jgen, sigFigs, val) -> jgen.writeNumber(Numbers.round(val, sigFigs)), limit);
		module.addSerializer(Double.class, doubleSer).addSerializer(Double.TYPE, doubleSer);
		NumberWithLimitSerializer<DecimalNode> decimalNodeSer = new NumberWithLimitSerializer<>(DecimalNode.class, DOUBLE, (jgen, sigFigs, node) -> {
			BigDecimal val = node.decimalValue();
			jgen.writeNumber((val != null) && (val.scale() > sigFigs) ? val.setScale(sigFigs, RoundingMode.HALF_UP) : val);
		}, limit);
		module.addSerializer(DecimalNode.class, decimalNodeSer);
		mapper.registerModule(module);
		return mapper;
	}

	/**
	 * Adds the temporal config.
	 *
	 * @param <T> the generic type
	 * @param module the module
	 * @param type the type
	 * @param in the in
	 * @param defaultAsInt the default as int
	 * @param out the out
	 * @param keyIn the key in
	 * @param keyOut the key out
	 * @return the simple module
	 */
	public static <T extends TemporalAccessor> SimpleModule addTemporalConfig(
		SimpleModule module,
		Class<T> type,
		Function<LocalDateTime, T> in,
		boolean defaultAsInt,
		Function<T, ?> out,
		Function<String, T> keyIn,
		Function<T, String> keyOut
	) {
		Lambdas.actionIfTrue(keyIn != null, () -> module.addKeyDeserializer(type, new TemporalAccessorKeyDeserializer<>(type, keyIn)));
		Lambdas.actionIfTrue(keyOut != null, () -> module.addKeySerializer(type, new TemporalAccessorKeySerializer<>(type, keyOut)));
		return module.addDeserializer(type, new TemporalAccessorDeserializer<>(type, in, defaultAsInt)).addSerializer(type, new TemporalAccessorSerializer<>(type, out));
	}

	/**
	 * Adds the temporal config.
	 *
	 * @param <T> the generic type
	 * @param module the module
	 * @param type the type
	 * @param in the in
	 * @param defaultAsInt the default as int
	 * @param out the out
	 * @return the simple module
	 */
	public static <T extends TemporalAccessor> SimpleModule addTemporalConfig(
		SimpleModule module,
		Class<T> type,
		Function<LocalDateTime, T> in,
		boolean defaultAsInt,
		Function<T, Long> out
	) {
		return addTemporalConfig(module, type, in, defaultAsInt, out, null, null);
	}

	/**
	 * Adds the tree map module.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the t
	 */
	public static <T extends ObjectMapper> T addTreeMapModule(T mapper) {
		SimpleModule mod = new SimpleModule("WTPTreeMap");
		mod.addAbstractTypeMapping(Map.class, CustomTreeMapCaseInsensitive.class);
		mapper.registerModule(mod);
		return mapper;
	}

	/**
	 * Adds the unquoted field names.
	 *
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static ObjectMapper addUnquotedFieldNames(ObjectMapper mapper) {
		return mapper.configure(Feature.QUOTE_FIELD_NAMES, false);
	}

	/**
	 * Check limit.
	 *
	 * @param size the size
	 * @throws SerializationLimitException the serialization limit exception
	 */
	public static void checkLimit(int size) throws SerializationLimitException {
		checkLimit(jsonCharLimit, size);
	}

	/**
	 * Check limit.
	 *
	 * @param limit the limit
	 * @param size the size
	 * @throws SerializationLimitException the serialization limit exception
	 */
	public static void checkLimit(long limit, long size) throws SerializationLimitException {
		if ((limit > 0) && (size > limit)) {
			throw new SerializationLimitException(limit, "Data length (bytes) is larger than max: limit=%,d size=%,d", limit, size);
		}
	}

	/**
	 * Clear object mapper cache.
	 *
	 * @return the int
	 */
	public static int clearObjectMapperCache() {
		int currentSize = OBJECT_MAPPER_CACHE.size();
		OBJECT_MAPPER_CACHE.clear();
		return currentSize;
	}

	/**
	 * Configure mapper with filter id.
	 *
	 * @param mapper the mapper
	 * @param id the id
	 * @return the object mapper
	 */
	public static ObjectMapper configureMapperWithFilterId(ObjectMapper mapper, String id) {
		return mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospectorSingleFilter(id));
	}

	/**
	 * Configure object mapper.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param useBespokeTyping the use bespoke typing
	 * @return the t
	 */
	public static <T extends ObjectMapper> T configureObjectMapper(T mapper, boolean useBespokeTyping) {
		return configureObjectMapper(mapper, useBespokeTyping, false);
	}

	/**
	 * Configure object mapper.
	 *
	 * @param <T> the generic type
	 * @param mapper the object mapper
	 * @param useBespokeTyping the use bespoke typing
	 * @param shortenedTypeOverride the shortened type override
	 * @return the object mapper
	 */
	public static <T extends ObjectMapper> T configureObjectMapper(T mapper, boolean useBespokeTyping, boolean shortenedTypeOverride) {
		if (useBespokeTyping) {
			mapper = setTyping(mapper, shortenedTypeOverride);
		}
		// mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		// mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);

		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
		mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, false);
		mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);

		// mapper.configure(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE, false);
		mapper.setDefaultMergeable(false);
		mapper.configOverride(Object.class).setMergeable(false);

		// Don't serialize null values
		mapper.setSerializationInclusion(Include.NON_NULL);

		// Serialize only fields not getters. Default behaviour is to serialize getters even if
		// property doesn't exist
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		// Custom handler to ignore unknown properties but log a warning message
		mapper.addHandler(new CustomDeserializationProblemHandler());
		mapper.setHandlerInstantiator(new CustomHandlerInstantiator());
		mapper.getSerializerProvider().setNullKeySerializer(new GenericKeySerializer());
		return registerMapper(mapper);
	}

	/**
	 * Deregister mapper.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return true, if successful
	 */
	public static <T extends ObjectMapper> boolean deregisterMapper(T mapper) {
		if (mapper != null) {
			synchronized (REGISTERED_MAPPERS) {
				return REGISTERED_MAPPERS.removeIf(m -> m == mapper);
			}
		}
		return false;
	}

	/**
	 * Gets the csv mapper.
	 *
	 * @return the csv mapper
	 */
	public static CsvMapper getCsvMapper() {
		try {
			CsvMapper mapper = configureObjectMapper(new CsvMapper(), false);
			mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
			// mapper.addMixIn(Returns.class, ReturnsFormat.class);
			if (!useDefaultSchemas) {
				mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false).addFilter(CSV_FILTER, new CsvPropertyFilter()));
				mapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
			}
			return mapper;
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue while getting Jackson CsvMapper!");
			return null;
		}
	}

	/**
	 * Gets the date module.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param datesAsStrings the dates as strings
	 * @return the date module
	 */
	public static <T extends ObjectMapper> SimpleModule getDateModule(@Nonnull T mapper, boolean datesAsStrings) {
		return getDateModule(mapper, datesAsStrings, null);
	}

	/**
	 * Gets the date module.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param datesAsStrings the dates as strings
	 * @param dateFormat the date format
	 * @return the date module
	 */
	public static <T extends ObjectMapper> SimpleModule getDateModule(@Nonnull T mapper, boolean datesAsStrings, @Nullable String dateFormat) {
		SimpleModule dateModule = new SimpleModule(DATE_MODULE_ID);
		Function<LocalDate, Object> outDate;
		Function<LocalDate, String> outDateKey;
		Function<LocalDateTime, Object> outDateTime;
		Function<LocalDateTime, String> outDateTimeKey;
		Function<ZonedDateTime, Object> outZonedDateTime;
		Function<ZonedDateTime, String> outZonedDateTimeKey;
		if (Checks.isNotBlank(dateFormat)) {
			DateTimeFormatter formatter = Dates.formatterFromPattern(dateFormat);
			outDate = formatter::format;
			outDateKey = formatter::format;
			outDateTime = formatter::format;
			outDateTimeKey = formatter::format;
			outZonedDateTime = formatter::format;
			outZonedDateTimeKey = formatter::format;
		} else if (datesAsStrings) {
			outDate = Dates::toJsonStandardFormat;
			outDateKey = Dates::toJsonStandardFormat;
			outDateTime = Dates::toJsonStandardFormat;
			outDateTimeKey = Dates::toJsonStandardFormat;
			outZonedDateTime = Dates::toJsonStandardFormat;
			outZonedDateTimeKey = Dates::toJsonStandardFormat;
		} else {
			outDate = d -> Dates.toInt(d).longValue();
			outDateKey = Dates::toIntString;
			outDateTime = Dates::toMillis;
			outDateTimeKey = Dates::toMillisString;
			outZonedDateTime = Dates::toMillis;
			outZonedDateTimeKey = Dates::toMillisString;
		}
		addTemporalConfig(dateModule, LocalDateTime.class, LocalDateTime::from, false, outDateTime, Dates::parseDateTime, outDateTimeKey);
		addTemporalConfig(dateModule, LocalDate.class, LocalDateTime::toLocalDate, true, outDate, Dates::parseDate, outDateKey);
		addTemporalConfig(dateModule, ZonedDateTime.class, Dates::toZonedDateTime, false, outZonedDateTime, Dates::parseZonedDateTime, outZonedDateTimeKey);
		dateModule.addDeserializer(ZoneOffset.class, new ZoneOffsetDeserializer());
		dateModule.addSerializer(ZoneOffset.class, new CustomFunctionalSerializer<>(ZoneOffset.class, zo -> zo.getId()));

		if (!(mapper instanceof XmlMapper)) {
			dateModule.addDeserializer(Date.class, new DateDeserializer());
			DateTimeFormatter formatter = Checks.isNotBlank(dateFormat) ? Dates.formatterFromPattern(dateFormat) : datesAsStrings ? Dates.SOLR_DATE_FORMAT : null;
			dateModule.addSerializer(Date.class, new DateSerializer(formatter));
		}
		return dateModule;
	}

	/**
	 * Gets the full property name.
	 *
	 * @param writer the writer
	 * @return the full property name
	 */
	public static String getFullPropertyName(PropertyWriter writer) {
		return (writer instanceof BeanPropertyWriter ? ((BeanPropertyWriter) writer).getMember().getDeclaringClass().getName() + '.' : "") + writer.getName();
	}

	/**
	 * Gets the json char cache limit.
	 *
	 * @return the jsonCharCacheLimit
	 */
	public static long getJsonCharCacheLimit() {
		return jsonCharCacheLimit;
	}

	/**
	 * Gets the json char limit.
	 *
	 * @return the jsonCharLimit
	 */
	public static int getJsonCharLimit() {
		return jsonCharLimit;
	}

	/**
	 * Gets the json mapper.
	 *
	 * @return the json mapper
	 */
	public static ObjectMapper getJsonMapper() {
		return getJsonMapper(true);
	}

	/**
	 * Gets the json mapper.
	 *
	 * @param useBespokeTyping the use bespoke typing
	 * @return the json mapper
	 */
	public static ObjectMapper getJsonMapper(boolean useBespokeTyping) {
		return getJsonMapper(useBespokeTyping, false);
	}

	/**
	 * Gets the object mapper.
	 *
	 * @param useBespokeTyping the use bespoke typing
	 * @param shortenedTypeOverride the shortened type override
	 * @return the object mapper
	 */
	public static ObjectMapper getJsonMapper(boolean useBespokeTyping, boolean shortenedTypeOverride) {
		return addModules(configureObjectMapper(new LimitedObjectMapper().setLimit(jsonCharLimit), useBespokeTyping, shortenedTypeOverride));
	}

	/**
	 * Gets the pretty printer.
	 *
	 * @return the pretty printer
	 */
	public static PrettyPrinter getPrettyPrinter() {
		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		DefaultIndenter indenter = new DefaultIndenter("\t", DefaultIndenter.SYS_LF);
		return pp.withObjectIndenter(indenter).withArrayIndenter(indenter);
	}

	/**
	 * Gets the registered mappers count.
	 *
	 * @return the registered mappers count
	 */
	public static int getRegisteredMappersCount() {
		synchronized (REGISTERED_MAPPERS) {
			return REGISTERED_MAPPERS.size();
		}
	}

	/**
	 * Gets the schema.
	 *
	 * @param obj the obj
	 * @return the schema
	 */
	public static CsvSchema getSchema(Object obj) {
		Class<?> clazz = obj.getClass();
		if (Collection.class.isAssignableFrom(clazz)) {
			clazz = ((Collection<?>) obj).iterator().next().getClass();
		} else if (Map.class.isAssignableFrom(clazz)) {
			clazz = ((Map<?, ?>) obj).values().iterator().next().getClass();
		}
		if (useDefaultSchemas) {
			return CSV_MAPPER.schemaFor(clazz).withHeader();
		}
		return getBespokeSchema(obj, clazz);
	}

	/**
	 * Gets the smile mapper.
	 *
	 * @return the smile mapper
	 */
	public static ObjectMapper getSmileMapper() {
		return getSmileMapper(true, true, false, false, false);
	}

	/**
	 * Gets the smile mapper.
	 *
	 * @param useBespokeTyping the use bespoke typing
	 * @param shortenedTypeOverride the shortened type override
	 * @param requireHeader the require header
	 * @param writeHeader the write header
	 * @param writeEndMarker the write end marker
	 * @return the smile mapper
	 */
	public static ObjectMapper getSmileMapper(boolean useBespokeTyping, boolean shortenedTypeOverride, boolean requireHeader, boolean writeHeader, boolean writeEndMarker) {
		return addModules(
			configureObjectMapper(
				new LimitedObjectMapper(
					new SmileFactory()
						.configure(SmileParser.Feature.REQUIRE_HEADER, requireHeader)
						.configure(SmileGenerator.Feature.WRITE_HEADER, writeHeader)
						.configure(SmileGenerator.Feature.WRITE_END_MARKER, writeEndMarker)
				).setLimit(jsonCharLimit),
				useBespokeTyping,
				shortenedTypeOverride
			)
		);
	}

	/**
	 * Gets the xml mapper.
	 *
	 * @return the xml mapper
	 */
	public static XmlMapper getXmlMapper() {
		XmlMapper mapper = configureObjectMapper(new XmlMapper(), false);
		return addModules(mapper);
	}

	/**
	 * Gets the yaml mapper.
	 *
	 * @param useBespokeTyping the use bespoke typing
	 * @param shortenedTypeOverride the shortened type override
	 * @return the yaml mapper
	 */
	public static ObjectMapper getYamlMapper(boolean useBespokeTyping, boolean shortenedTypeOverride) {
		YAMLMapper mapper = new YAMLMapper();
		mapper.configure(com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.LITERAL_BLOCK_STYLE, true);
		mapper.configure(com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES, false);
		mapper.configure(com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES, false);
		return addModules(configureObjectMapper(new LimitedObjectMapper(mapper).setLimit(jsonCharLimit), useBespokeTyping, shortenedTypeOverride));
	}

	/**
	 * Ignore annotations.
	 *
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static ObjectMapper ignoreAnnotations(ObjectMapper mapper) {
		return mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
	}

	/**
	 * @return the useObjectMapperCache
	 */
	public static boolean isUseObjectMapperCache() {
		return useObjectMapperCache;
	}

	/**
	 * Register mapper.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public static <T extends ObjectMapper> T registerMapper(T mapper) {
		if (mapper != null) {
			synchronized (REGISTERED_MAPPERS) {
				REGISTERED_MAPPERS.add(mapper);
				synchronized (REGISTERED_MODULES) {
					if (!REGISTERED_MODULES.isEmpty()) {
						mapper.registerModules(REGISTERED_MODULES);
					}
				}
				synchronized (REGISTERED_MODIFIERS) {
					if (!REGISTERED_MODIFIERS.isEmpty()) {
						REGISTERED_MODIFIERS.forEach(mod -> mod.accept(mapper));
					}
				}
			}
		}
		return mapper;
	}

	/**
	 * Register modifier.
	 *
	 * @param modifier the modifier
	 */
	public static void registerModifier(Consumer<ObjectMapper> modifier) {
		assert modifier != null;
		registerModifiers(Arrays.asList(modifier));
	}

	/**
	 * Register modifiers.
	 *
	 * @param modifiers the modifiers
	 */
	public static void registerModifiers(Collection<Consumer<ObjectMapper>> modifiers) {
		assert Checks.isNotEmpty(modifiers);
		synchronized (REGISTERED_MAPPERS) {
			for (ObjectMapper mapper : REGISTERED_MAPPERS) {
				modifiers.forEach(mod -> mod.accept(mapper));
			}
			synchronized (REGISTERED_MODIFIERS) {
				REGISTERED_MODIFIERS.addAll(modifiers);
			}
			clearObjectMapperCache();
		}
	}

	/**
	 * Register modules.
	 *
	 * @param modules the modules
	 */
	public static void registerModules(Collection<Module> modules) {
		assert Checks.isNotEmpty(modules);
		synchronized (REGISTERED_MAPPERS) {
			for (ObjectMapper mapper : REGISTERED_MAPPERS) {
				mapper.registerModules(modules);
			}
			synchronized (REGISTERED_MODULES) {
				REGISTERED_MODULES.addAll(modules);
			}
			clearObjectMapperCache();
		}
	}

	/**
	 * Register modules.
	 *
	 * @param modules the modules
	 */
	public static void registerModules(Module... modules) {
		assert Checks.isNotEmpty(modules);
		registerModules(Arrays.asList(modules));
	}

	/**
	 * Reload jackson caches.
	 */
	public static void reloadJacksonCaches() {
		try {
			synchronized (REGISTERED_MAPPERS) {
				REGISTERED_MAPPERS.forEach(mapper -> {
					try {
						Logs.logInfo(LOG, "Flushing Jackson root deserializer cache");
						final Field rootDeserializersField = ReflectionUtils.findField(mapper.getClass(), "_rootDeserializers");
						ReflectionUtils.makeAccessible(rootDeserializersField);
						((Map<?, ?>) ReflectionUtils.getField(rootDeserializersField, mapper)).clear();

						Logs.logInfo(LOG, "Flushing Jackson serializer cache");
						SerializerProvider serializerProvider = mapper.getSerializerProvider();
						Field serializerCacheField = serializerProvider.getClass().getSuperclass().getSuperclass().getDeclaredField("_serializerCache");
						ReflectionUtils.makeAccessible(serializerCacheField);
						SerializerCache serializerCache = (SerializerCache) serializerCacheField.get(serializerProvider);
						Method serializerCacheFlushMethod = SerializerCache.class.getDeclaredMethod("flush");
						serializerCacheFlushMethod.invoke(serializerCache);

						Logs.logInfo(LOG, "Flushing Jackson deserializer cache");
						DeserializationContext deserializationContext = mapper.getDeserializationContext();
						Field deSerializerCacheField = deserializationContext.getClass().getSuperclass().getSuperclass().getDeclaredField("_cache");
						ReflectionUtils.makeAccessible(deSerializerCacheField);
						DeserializerCache deSerializerCache = (DeserializerCache) deSerializerCacheField.get(deserializationContext);
						Method deSerializerCacheFlushMethod = DeserializerCache.class.getDeclaredMethod("flushCachedDeserializers");
						deSerializerCacheFlushMethod.invoke(deSerializerCache);
					} catch (Throwable e) {
						logError(LOG, e, "Issue clearing cache for mapper [%s]", mapper);
					}
				});
			}
		} catch (Throwable e) {
			logWarn(LOG, e, "Could not hot reload Jackson class!");
		}
	}

	/**
	 * Resolve object mapper.
	 *
	 * @param params the params
	 * @return the object mapper
	 */
	public static ObjectMapper resolveObjectMapper(@Nonnull OutputParams params) {
		return resolveObjectMapper(params, true);
	}

	/**
	 * Resolve object mapper.
	 *
	 * @param params the params
	 * @param useCache the use cache
	 * @return the object mapper
	 */
	public static ObjectMapper resolveObjectMapper(@Nonnull OutputParams params, boolean useCache) {
		ObjectMapper mapper;
		final boolean canCache = useObjectMapperCache && useCache && params.canCacheResolvedObjectMapper();
		final String cacheKey = canCache ? params.toObjectMapperCacheKey() : null;
		if (canCache && ((mapper = OBJECT_MAPPER_CACHE.get(cacheKey)) != null)) {
			return mapper;
		}
		final Stopwatch watch = canCache ? Stopwatch.createAndStart() : null;
		mapper = params.getObjectMapper() != null ? params.getObjectMapper() : resolveObjectMapperBase(params);
		if (mapper != null) {
			boolean copied = false;
			if ((params.getObjectMapper() != null) && (params.testPrettifyJson() || !params.testJsonIncludeType())) {
				mapper = mapper.copy();
				copied = true;
				if (!params.testJsonIncludeType()) {
					mapper.disableDefaultTyping();
				}
				if (params.testPrettifyJson()) {
					addPrettyPrinter(mapper);
				}
			}
			if ((params.getQuoteFieldNames() != null) && !params.getQuoteFieldNames().booleanValue()) {
				mapper = SerializerFactory.addUnquotedFieldNames(copied ? mapper : mapper.copy());
				copied = true;
			}
			if (params.testOrderKeys()) {
				mapper = SerializerFactory.addOrderedMapConfig(copied ? mapper : mapper.copy());
				copied = true;
			}
			if (params.testEncodeStringsForHtml()) {
				mapper = SerializerFactory.addHtmlEncodingStringSerializer(copied ? mapper : mapper.copy());
				copied = true;
			}
			if (params.testDatesAsStrings() || Checks.isNotBlank(params.getDatesAsStringsFormat())) {
				mapper = SerializerFactory.addDatesAsStrings(copied ? mapper : mapper.copy(), params.getDatesAsStringsFormat());
				copied = true;
			}
			if (params.getJsonSigFigs() != null) {
				mapper = SerializerFactory.addSigFigsLimit(copied ? mapper : mapper.copy(), params.getJsonSigFigs());
				copied = true;
			}
			if (isNotEmpty(params.getFieldInclusionPredicateFilter())) {
				mapper = SerializerFactory.addFieldInclusionPredicateFilter(copied ? mapper : mapper.copy(), params.getFieldInclusionPredicateFilter());
				copied = true;
			}
			if (isNotEmpty(params.getFieldsWhiteList()) || isNotEmpty(params.getFieldsBlackList())) {
				mapper = SerializerFactory.addFieldsWhiteAndBlackList(copied ? mapper : mapper.copy(), params.getFieldsWhiteList(), params.getFieldsBlackList());
				copied = true;
			}
			if (isNotEmpty(params.getOnlyFields())) {
				mapper = SerializerFactory.addOneTimeOnlyFields(copied ? mapper : mapper.copy(), params.getOnlyFields());
				copied = true;
			} else {
				if (isNotEmpty(params.getSkipFields())) {
					mapper = SerializerFactory.addOneTimeSkipFields(copied ? mapper : mapper.copy(), params.getSkipFields());
					copied = true;
				}
				if (isNotEmpty(params.getSkipFieldsByType())) {
					mapper = SerializerFactory.addOneTimeSkipFields(copied ? mapper : mapper.copy(), params.getSkipFieldsByType());
					copied = true;
				}
			}
			if (isNotBlank(params.getGraph())) {
				mapper = SerializerFactory.addGraphFilter(copied ? mapper : mapper.copy(), params.getGraph());
				copied = true;
			}
			if (canCache) {
				OBJECT_MAPPER_CACHE.put(cacheKey, mapper);
			}
			if (watch != null) {
				Logs.logTimer(LOG, watch, "resolve_object_mapper", cacheKey);
			}
		}
		return mapper;
	}

	/**
	 * Sets the json char cache limit.
	 *
	 * @param jsonCharCacheLimit the jsonCharCacheLimit to set
	 */
	public static void setJsonCharCacheLimit(long jsonCharCacheLimit) {
		SerializerFactory.jsonCharCacheLimit = jsonCharCacheLimit;
	}

	/**
	 * Sets the object mapper char limit.
	 *
	 * @param jsonCharLimit the new json char limit
	 */
	public static void setJsonCharLimit(int jsonCharLimit) {
		SerializerFactory.jsonCharLimit = jsonCharLimit;
		synchronized (REGISTERED_MAPPERS) {
			REGISTERED_MAPPERS
				.stream()
				.filter(LimitedObjectMapper.class::isInstance)
				.map(LimitedObjectMapper.class::cast)
				.forEach(mapper -> mapper.setLimit(jsonCharLimit));
		}
	}

	/**
	 * Sets the typing.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param shortenedTypeOverride the shortened type override
	 * @return the object mapper
	 */
	public static <T extends ObjectMapper> T setTyping(T mapper, boolean shortenedTypeOverride) {
		mapper.setDefaultTyping(new CustomTypeResolver(shortenedTypeOverride).init(Id.CLASS, null).inclusion(JsonTypeInfo.As.PROPERTY).typeProperty(Id.NAME.getDefaultPropertyName()));
		return mapper;
	}

	/**
	 * @param useObjectMapperCache the useObjectMapperCache to set
	 */
	public static void setUseObjectMapperCache(boolean useObjectMapperCache) {
		SerializerFactory.useObjectMapperCache = useObjectMapperCache;
	}

	/**
	 * Adds the schema properties.
	 *
	 * @param builder the builder
	 * @param intr the intr
	 * @param pojoType the pojo type
	 * @param unwrapper the unwrapper
	 */
	private static void addSchemaProperties(CsvSchema.Builder builder, AnnotationIntrospector intr, JavaType pojoType, NameTransformer unwrapper) {
		BeanDescription beanDesc = CSV_MAPPER.getSerializationConfig().introspect(pojoType);
		for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
			if (prop.couldSerialize()) {
				AnnotatedMember m = prop.getPrimaryMember();
				if (m != null) {
					NameTransformer nextUnwrapper = intr.findUnwrappingNameTransformer(prop.getPrimaryMember());
					if (nextUnwrapper != null) {
						if (unwrapper != null) {
							nextUnwrapper = NameTransformer.chainedTransformer(unwrapper, nextUnwrapper);
						}
						JavaType nextType = m.getType();
						if (nextType.getRawClass() != pojoType.getRawClass()) {
							addSchemaProperties(builder, intr, nextType, nextUnwrapper);
							continue;
						}
					}
				}
				String name = prop.getName();
				if (unwrapper != null) {
					name = unwrapper.transform(name);
				}
				builder.addColumn(name);
			}
		}
	}

	/**
	 * Gets the bespoke schema.
	 *
	 * @param obj the obj
	 * @param clazz the clazz
	 * @return the bespoke schema
	 */
	@SuppressWarnings("unchecked")
	private static CsvSchema getBespokeSchema(Object obj, Class<?> clazz) {
		JavaType pojoType = CSV_MAPPER.constructType(clazz);
		if (pojoType.isMapLikeType()) {
			return getSchemaForListOfMaps((List<Map<String, Object>>) obj);
		}
		synchronized (UNTYPE_SCHEMAS) {
			CsvSchema s = UNTYPE_SCHEMAS.get(pojoType);
			if (s != null) {
				return s;
			}
		}
		AnnotationIntrospector intr = CSV_MAPPER.getDeserializationConfig().getAnnotationIntrospector();
		CsvSchema.Builder builder = CsvSchema.builder().setUseHeader(true);
		addSchemaProperties(builder, intr, pojoType, null);
		CsvSchema result = builder.build();
		synchronized (UNTYPE_SCHEMAS) {
			UNTYPE_SCHEMAS.put(pojoType, result);
		}
		return result;
	}

	/**
	 * Gets the schema for list of maps.
	 *
	 * @param maps the maps
	 * @return the schema for list of maps
	 */
	private static CsvSchema getSchemaForListOfMaps(List<Map<String, Object>> maps) {
		CsvSchema.Builder builder = CsvSchema.builder().setUseHeader(true);
		Set<String> allKeys = new TreeSet<>();
		maps.stream().map(Map::keySet).forEach(k -> allKeys.addAll(k));
		allKeys.forEach(k -> builder.addColumn(k));
		return builder.build();
	}

	/**
	 * Resolve object mapper base.
	 *
	 * @param params the params
	 * @return the object mapper
	 */
	private static ObjectMapper resolveObjectMapperBase(@Nonnull OutputParams params) {
		final OutputFormat format = params.getOutputFormat() == null ? OutputFormat.json : params.getOutputFormat();
		switch (format) {
		case csv:
		case html:
		case xls:
		case xlsx:
		case json:
		case jsonCompressed:
		case jsonp:
			return !params.testJsonIncludeType() ? (params.testPrettifyJson() ? addPrettyPrinter(Serializer.JSON_OUTPUT_MAPPER.copy()) : Serializer.JSON_OUTPUT_MAPPER)
				: params.testPrettifyJson() ? Serializer.PRETTY_WRITER : Serializer.JSON_MAPPER;
		case smile:
			return params.testPrettifyJson() ? addPrettyPrinter(Serializer.SMILE_MAPPER.copy()) : Serializer.SMILE_MAPPER;
		case yaml:
			return !params.testJsonIncludeType() ? (params.testPrettifyJson() ? addPrettyPrinter(Serializer.YAML_NO_TYPE_MAPPER.copy()) : Serializer.YAML_NO_TYPE_MAPPER)
				: params.testPrettifyJson() ? addPrettyPrinter(Serializer.YAML_MAPPER.copy()) : Serializer.YAML_MAPPER;
		case xml:
			// NB: the XML_MAPPER already does not include the @type values, which is why 'params.testJsonIncludeType()' is not checked
			return params.testPrettifyJson() ? addPrettyPrinter(Serializer.XML_MAPPER.copy()) : Serializer.XML_MAPPER;
		default:
			String error = String.format("OutputFormat is not supported: format=%s", format);
			Logs.logForSplunk(LOG, SplunkMessageIdCommon.OUTPUT_FORMAT_NOT_SUPPORTED, error);
			throw new IllegalArgumentException(error);
		}
	}
}
