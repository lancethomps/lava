// CHECKSTYLE.OFF: FileLength|OpenCSV
package com.github.lancethomps.lava.common.ser;

import static com.github.lancethomps.lava.common.Checks.isEmpty;
import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.expr.ExprFactory.evalOgnl;
import static com.github.lancethomps.lava.common.expr.ExprFactory.getValueFromPath;
import static com.github.lancethomps.lava.common.logging.Logs.logDebug;
import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static com.github.lancethomps.lava.common.logging.Logs.logTrace;
import static com.github.lancethomps.lava.common.logging.Logs.logWarn;
import static com.github.lancethomps.lava.common.ser.OutputFormat.csv;
import static com.github.lancethomps.lava.common.ser.OutputFormat.jsonCompressed;
import static com.github.lancethomps.lava.common.ser.OutputFormat.jsonp;
import static com.github.lancethomps.lava.common.ser.OutputFormat.xls;
import static com.github.lancethomps.lava.common.ser.OutputFormat.xlsx;
import static com.github.lancethomps.lava.common.ser.SerializerFactory.addPrettyPrinter;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.XML;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.expression.spel.standard.SpelExpression;
import org.yaml.snakeyaml.Yaml;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.CommonConstants;
import com.github.lancethomps.lava.common.CompressionUtil;
import com.github.lancethomps.lava.common.Enums;
import com.github.lancethomps.lava.common.Exceptions;
import com.github.lancethomps.lava.common.Reflections;
import com.github.lancethomps.lava.common.env.EnvSpecificConfig;
import com.github.lancethomps.lava.common.env.EnvSpecificConfigWrapper;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.file.Content;
import com.github.lancethomps.lava.common.file.FileParser;
import com.github.lancethomps.lava.common.file.FileParserOptions;
import com.github.lancethomps.lava.common.file.FileParsingException;
import com.github.lancethomps.lava.common.file.FileUtil;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.merge.MergeConfig;
import com.github.lancethomps.lava.common.merge.Merges;
import com.github.lancethomps.lava.common.ser.excel.ExcelFactory;
import com.github.lancethomps.lava.common.ser.jackson.CustomDeserializationProblemHandler;
import com.github.lancethomps.lava.common.ser.jackson.SerializationException;
import com.github.lancethomps.lava.common.ser.jackson.schema.DefinitionsSchemaFactory;
import com.github.lancethomps.lava.common.ser.jackson.schema.SchemaWithDefinitions;
import com.github.lancethomps.lava.common.ser.jackson.types.CustomTypeIdResolver;
import com.github.lancethomps.lava.common.ser.snakeyaml.PropertyParsingConstructor;
import com.github.lancethomps.lava.common.sorting.CaseInsensitiveStringSort;
import com.github.lancethomps.lava.common.sorting.SortClause;
import com.github.lancethomps.lava.common.sorting.Sorting;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.web.WebRequests;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.json.JsonSanitizer;
import com.opencsv.CSVParser;
import com.opencsv.CSVWriter;

import ognl.Node;

/**
 * The Class Serializer.
 */
@SuppressWarnings("PMD.LooseCoupling")
public class Serializer {

	/** The Constant CONVERT_MAPPER. */
	public static final ObjectMapper CONVERT_MAPPER = SerializerFactory.getJsonMapper(false, false).configure(MapperFeature.USE_ANNOTATIONS, false);

	/** The Constant CSV_MAPPER. */
	public static final CsvMapper CSV_MAPPER = SerializerFactory.getCsvMapper();

	/** The Constant INTEGER_TYPES. */
	public static final Set<Class<?>> INTEGER_TYPES = Sets.newHashSet(Integer.class, Integer.TYPE, Long.class, Long.TYPE, BigInteger.class);

	/** The Constant objectMapper. */
	public static final ObjectMapper JSON_MAPPER = SerializerFactory.getJsonMapper();

	/** The Constant JSON_OUTPUT_MAPPER. */
	public static final ObjectMapper JSON_OUTPUT_MAPPER = SerializerFactory.addNoopTyping(SerializerFactory.getJsonMapper(false));

	/** The Constant LIST_KEY_REGEX. */
	public static final Pattern LIST_KEY_REGEX = Pattern.compile("^(\\w+)\\[(\\d+|\\*)(?:\\**)]$");

	/** The Constant LOG. */
	public static final Logger LOG = Logger.getLogger(Serializer.class);

	/** The Constant MAPPER. */
	@SuppressWarnings("PMD.LooseCoupling")
	public static final JavaType MAP_TYPE = JSON_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);

	/** The Constant PATH_KEY_ARRAY_POS_REGEX. */
	public static final Pattern PATH_KEY_ARRAY_POS_REGEX = Pattern.compile("\\[\\d+\\]");

	/** The Constant PRETTY_MAPPER. */
	public static final ObjectMapper PRETTY_WRITER = addPrettyPrinter(SerializerFactory.getJsonMapper());

	/** The Constant JSON_FAILURE. */
	public static final String RESULTS_FAILURE = "{\"success\":false}";

	/** The Constant JSON_SUCCESS. */
	public static final String RESULTS_SUCCESS = "{\"success\":true}";

	/** The Constant SIMPLE_FIELD_CLASSES. */
	public static final List<Class<?>> SIMPLE_FIELD_CLASSES = Lists.newArrayList(String.class, Number.class, Boolean.class, Class.class);

	/** The Constant SMILE_MAPPER. */
	public static final ObjectMapper SMILE_MAPPER = SerializerFactory.getSmileMapper();

	/** The Constant TYPE_PROPERTY. */
	public static final String TYPE_PROPERTY = "@type";

	/** The Constant XML_DECLARATION. */
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>";

	/** The Constant XML_MAPPER. */
	public static final XmlMapper XML_MAPPER = SerializerFactory.getXmlMapper();

	/** The read serializer. */
	public static final org.simpleframework.xml.Serializer XML_READ_SERIALIZER = new Persister(new AnnotationStrategy(), new EnumToStringMatcher());

	/** The Constant XML_WRITE_SERIALIZER. */
	public static final org.simpleframework.xml.Serializer XML_WRITE_SERIALIZER = new Persister(new Format(XML_DECLARATION));

	/** The Constant YAML_MAPPER. */
	public static final ObjectMapper YAML_MAPPER = SerializerFactory.getYamlMapper(true, true);

	/** The Constant YAML_NO_TYPE_MAPPER. */
	public static final ObjectMapper YAML_NO_TYPE_MAPPER = SerializerFactory.addNoopTyping(SerializerFactory.getYamlMapper(false, false));

	/** The log missing properties. */
	private static boolean logMissingProperties = true;

	/** The Constant SIMPLE_OUTPUT_CLASSES. */
	private static final Set<Class<?>> SIMPLE_OUTPUT_CLASSES = Sets.newHashSet(Boolean.class, String.class);

	static {
		CustomDeserializationProblemHandler.addIgnoreProperty("_anchors");
	}

	/**
	 * Adds the params to csv serializer.
	 *
	 * @param csvSer the csv
	 * @param outputParams the output params
	 * @return the csv serializer
	 */
	public static CsvSerializer addParamsToCsvSerializer(CsvSerializer csvSer, OutputParams outputParams) {
		return csvSer
			.setOutputFormat(outputParams.getOutputFormat() == null ? csv : outputParams.getOutputFormat())
			.setSkipProperties(
				isEmpty(outputParams.getSkipFields()) ? CsvSerializer.DEFAULT_SKIP_PROPS : outputParams.getSkipFields()
			)
			.setSkipRowsExpression(outputParams.getCsvSkipRowsExpression())
			.setIncludeProperties(outputParams.getCsvIncludeProperties())
			.setCsvDelimiter(outputParams.getCsvDelimiter())
			.setExpandJsonFields((outputParams.getCsvExpandJsonFields() == null) ? csvSer.isExpandJsonFields() : outputParams.getCsvExpandJsonFields())
			.setExpandJsonFieldsInner(
				(outputParams.getCsvExpandJsonFieldsInner() == null) ? csvSer.isExpandJsonFieldsInner() : outputParams.getCsvExpandJsonFieldsInner()
			)
			.setHeadersOrder(outputParams.getCsvHeaders())
			.setTransposeData(outputParams.testTranspose())
			.setAsFlattenedObjects(outputParams.testCsvAsFlatObjs())
			.setChainingSep(
				defaultIfBlank(outputParams.getCsvChainingSep(), csvSer.getChainingSep())
			)
			.setUseChainingListSep((outputParams.getCsvUseChainingListSep() == null) || outputParams.getCsvUseChainingListSep())
			.setSort(
				defaultIfBlank(outputParams.getCsvSort(), outputParams.getCsvSortByField())
			);
	}

	/**
	 * Adds the path key to map.
	 *
	 * @param result the result
	 * @param key the key
	 * @param val the val
	 * @return the map
	 */
	public static Map<String, Object> addPathKeyToMap(@Nonnull Map<String, Object> result, @Nonnull String key, Object val) {
		return addPathKeyToMap(result, key, val, null);
	}

	/**
	 * Adds the path key to map.
	 *
	 * @param result the result
	 * @param key the key
	 * @param val the val
	 * @param beanClass the bean class
	 * @return the map
	 */
	// CHECKSTYLE.OFF: NestedIfDepth
	@SuppressWarnings("unchecked")
	public static Map<String, Object> addPathKeyToMap(@Nonnull Map<String, Object> result, @Nonnull String key, Object val, Class<?> beanClass) {
		String mapKey = key;
		Object mapVal = null;
		if (key.startsWith("\"") && key.endsWith("\"")) {
			mapKey = StringUtils.removeStart(StringUtils.removeEnd(key, "\""), "\"");
			mapVal = val;
		} else if (contains(key, ".") || (contains(key, "["))) {
			Map<String, Object> previous = result;
			Iterator<String> keys = Collect.splitCsvAsList(key, '.').iterator();
			List<List<Object>> listsWithNulls = new ArrayList<>(1);
			while (keys.hasNext()) {
				String keyPart = keys.next();
				Matcher listMatcher = LIST_KEY_REGEX.matcher(keyPart);
				if (listMatcher.find()) {
					String listKey = listMatcher.group(1);
					@SuppressWarnings("rawtypes")
					Class<? super Collection> listType = Optional
						.ofNullable(beanClass == null ? null : Reflections.getField(beanClass, listKey))
						.map(field -> (Class) field.getType())
						.filter(
							type -> Collection.class.isAssignableFrom(type)
						)
						.map(
							type -> {
								if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
									return type;
								}
								if (Set.class.isAssignableFrom(type)) {
									return LinkedHashSet.class;
								} else if (List.class.isAssignableFrom(type)) {
									return ArrayList.class;
								}
								return null;
							}
						)
						.orElse(ArrayList.class);
					String listPos = listMatcher.group(2);
					Collection<Object> innerList = (Collection<Object>) previous.computeIfAbsent(
						listKey,
						k -> {
							try {
								return ClassUtil.createInstance(listType, false);
							} catch (Throwable e) {
								Logs.logError(LOG, e, "Could not create new instance of type [%s]", listType);
								return new ArrayList<>();
							}
						}
					);
					if (mapVal == null) {
						mapKey = listKey;
						mapVal = innerList;
					}
					if (keys.hasNext()) {
						int pos = "*".equals(listPos) ? innerList.size() : NumberUtils.toInt(listPos, 0);
						Map<String, Object> innerListMap = null;
						while (pos >= innerList.size()) {
							innerList.add(Maps.newTreeMap());
						}
						if (pos < innerList.size()) {
							if (innerList instanceof List) {
								innerListMap = (Map<String, Object>) ((List<Object>) innerList).get(pos);
							} else {
								innerListMap = (Map<String, Object>) IteratorUtils.get(innerList.iterator(), pos);
							}
						}
						previous = innerListMap;
					} else if (innerList instanceof List) {
						if ("*".equals(listPos)) {
							if (listsWithNulls.contains(innerList)) {
								int firstNullIndex = ((List) innerList).indexOf(Void.TYPE);
								if (firstNullIndex > -1) {
									((List) innerList).set(firstNullIndex, val);
								} else {
									innerList.add(val);
								}
							} else {
								innerList.add(val);
							}
						} else {
							int pos = NumberUtils.toInt(listPos, 0);
							if ((pos >= innerList.size()) && !listsWithNulls.contains(innerList)) {
								listsWithNulls.add((List) innerList);
							}
							while (pos >= innerList.size()) {
								innerList.add(Void.TYPE);
							}
							((List) innerList).set(pos, val);
						}
					} else {
						innerList.add(val);
					}
				} else if (keys.hasNext()) {
					Map<String, Object> innerMap = (Map<String, Object>) previous.computeIfAbsent(keyPart, k -> new TreeMap<String, Object>());
					if (mapVal == null) {
						mapKey = keyPart;
						mapVal = innerMap;
					}
					previous = innerMap;
				} else {
					previous.put(keyPart, val);
				}
			}
			for (List<Object> listWithMaybeNull : listsWithNulls) {
				for (int pos = 0; pos < listWithMaybeNull.size(); pos++) {
					if (listWithMaybeNull.get(pos) == Void.TYPE) {
						listWithMaybeNull.set(pos, null);
					}
				}
			}
		} else {
			mapVal = val;
		}
		result.put(mapKey, mapVal);
		return result;
	}
	// CHECKSTYLE.ON: NestedIfDepth

	/**
	 * Adds the simple output classes.
	 *
	 * @param classes the classes
	 */
	public static void addSimpleOutputClasses(@Nonnull Class<?>... classes) {
		SIMPLE_OUTPUT_CLASSES.addAll(Arrays.asList(classes));
	}

	/**
	 * Adds the bean map to map.
	 *
	 * @param map the map
	 * @param beanToAdd the bean to add
	 * @param keyPrefix the key prefix
	 * @return the map
	 */
	public static Map<String, Object> addToMap(@Nonnull Map<String, Object> map, @Nonnull Object beanToAdd, @Nullable String keyPrefix) {
		Map<String, Object> beanMap = Serializer.toMap(beanToAdd, false);
		Iterator<Entry<String, Object>> iterator = beanMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			String key = (keyPrefix != null ? keyPrefix : "") + entry.getKey();
			Object obj = entry.getValue();
			if ((obj instanceof String) || (obj instanceof Number)) {
				map.put(key, obj);
			}
		}
		return map;
	}

	/**
	 * Clone.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param val the val
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(ObjectMapper mapper, T val) {
		if (val == null) {
			return val;
		}
		try {
			return (T) mapper.readValue(mapper.writeValueAsBytes(val), val.getClass());
		} catch (Throwable e) {
			logError(LOG, e, "Issue cloning value [%s]", val);
		}
		return null;
	}

	/**
	 * Clone.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @return the t
	 */
	public static <T> T clone(T val) {
		return clone(Merges.MERGE_MAPPER, val);
	}

	/**
	 * Clone list via json.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> cloneListViaJson(List<T> src) {
		return (List<T>) readJsonAsList(toJson(src), src.get(0).getClass());
	}

	/**
	 * Clone via json.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param type the type
	 * @return the t
	 */
	public static <T> T cloneViaJson(Object src, Class<T> type) {
		T target = null;
		if (src != null) {
			if (src.getClass() != type) {
				Map<String, Object> objectMap = Serializer.toMap(src);
				objectMap.remove("@type");
				return Serializer.fromMap(objectMap, type);
			}
			String json = Serializer.toJson(src);
			target = Serializer.fromJson(json, type);
		}
		return target;
	}

	/**
	 * Clone via json.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneViaJson(T src) {
		return (T) cloneViaJson(src, src.getClass());
	}

	/**
	 * Construct collection type.
	 *
	 * @param collectionClass the collection class
	 * @param elementClass the element class
	 * @return the java type
	 */
	@SuppressWarnings("rawtypes")
	public static JavaType constructCollectionType(Class<? extends Collection> collectionClass, Type elementClass) {
		return getTypeFactory().constructCollectionType(collectionClass, constructType(elementClass));
	}

	/**
	 * Construct map type.
	 *
	 * @param mapClass the map class
	 * @param keyClass the key class
	 * @param valueClass the value class
	 * @return the java type
	 */
	@SuppressWarnings("rawtypes")
	public static JavaType constructMapType(Class<? extends Map> mapClass, Type keyClass, Type valueClass) {
		return getTypeFactory().constructMapType(mapClass, constructType(keyClass), constructType(valueClass));
	}

	/**
	 * Construct type.
	 *
	 * @param type the type
	 * @return the java type
	 */
	public static JavaType constructType(Type type) {
		return type instanceof JavaType ? (JavaType) type : getTypeFactory().constructType(type);
	}

	/**
	 * Convert.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param val the val
	 * @param toType the to type
	 * @return the u
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> U convert(T val, Class<U> toType) {
		if ((val != null) && (val.getClass() == toType)) {
			return (U) val;
		}
		return convert(val, toType == null ? null : constructType(toType));
	}

	/**
	 * Convert.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param val the val
	 * @param toType the to type
	 * @return the u
	 */
	public static <T, U> U convert(T val, JavaType toType) {
		if ((val == null) || (toType == null)) {
			return null;
		}
		try {
			return CONVERT_MAPPER.readValue(CONVERT_MAPPER.writeValueAsBytes(val), toType);
		} catch (Throwable e) {
			logError(LOG, e, "Issue converting value to type [%s]. Value: [%s]", toType, val);
		}
		return null;
	}

	/**
	 * Convert path key maps to type.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> convertPathKeyMapsToType(List<Map<String, Object>> data, Class<T> type) {
		return data
			.stream()
			.map(
				dataMap -> {
					return Serializer.convertPathKeyMapToPojo(dataMap, null, type);
				}
			)
			.collect(Collectors.toList());
	}

	/**
	 * Convert path key map to pojo.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param ignoreKeys the ignore keys
	 * @param beanClass the bean class
	 * @return the t
	 */
	public static <T> T convertPathKeyMapToPojo(Map<String, Object> map, Set<String> ignoreKeys, Class<T> beanClass) {
		return fromMap(convertPathKeyMapToPojoMap(map, ignoreKeys, beanClass), beanClass);
	}

	/**
	 * Convert path key map to pojo map.
	 *
	 * @param map the map
	 * @param ignoreKeys the ignore keys
	 * @param beanClass the bean class
	 * @return the map
	 */
	public static Map<String, Object> convertPathKeyMapToPojoMap(Map<String, Object> map, Set<String> ignoreKeys, Class<?> beanClass) {
		Map<String, Object> result = new HashMap<>();
		map
			.entrySet()
			.stream()
			.filter(e -> e.getValue() != null)
			.filter(e -> !"_version_".equalsIgnoreCase(e.getKey()) && ((ignoreKeys == null) || !ignoreKeys.contains(e.getKey())))
			.forEach(
				e -> Serializer.addPathKeyToMap(result, e.getKey(), e.getValue(), beanClass)
			);
		return result;
	}

	/**
	 * Copy.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @return the t
	 */
	public static <T> T copy(final T src) {
		return copy(src, null);
	}

	/**
	 * Copy.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param ignoreFields the ignore fields
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copy(final T src, final Set<String> ignoreFields) {
		if (src == null) {
			return src;
		}
		return (T) fromJson(toJson(JSON_OUTPUT_MAPPER, src, ignoreFields), src.getClass());
	}

	/**
	 * Copy fields.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @param fields the fields
	 * @return the t
	 */
	public static <T> T copyFields(final Object src, final T target, final Set<String> fields) {
		if ((src != null) && (target != null)) {
			try {
				JSON_MAPPER.readerForUpdating(target).readValue(toTree(SerializerFactory.addOneTimeOnlyFields(JSON_OUTPUT_MAPPER.copy(), fields), src, null));
			} catch (Throwable e) {
				logError(LOG, e, "Issue copying properties using JSON for source class [%s] and target class [%s] with values [%s] and [%s]!", src.getClass(), target.getClass(), src, target);
			}
		}
		return target;
	}

	/**
	 * Copy properties.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copyProperties(Object src, final T target) {
		if ((src != null) && (target != null)) {
			Map<String, Object> srcMap = src instanceof Map ? (Map<String, Object>) src : Serializer.toMap(src);
			Set<String> targetFields = Stream.of(BeanUtils.getPropertyDescriptors(target.getClass())).map(pd -> pd.getName()).collect(Collectors.toSet());
			Set<String> srcMapKeys = Sets.newHashSet(srcMap.keySet());
			srcMapKeys.stream().filter(key -> !targetFields.contains(key)).forEach(srcMap::remove);
			src = srcMap;
		}
		return copyProperties(src, target, null);
	}

	/**
	 * Copy properties.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @param ignoreFields the ignore fields
	 * @return the t
	 */
	public static <T> T copyProperties(final Object src, final T target, final Set<String> ignoreFields) {
		if ((src != null) && (target != null)) {
			try {
				JSON_MAPPER.readerForUpdating(target).readValue(toTree(JSON_OUTPUT_MAPPER, src, ignoreFields));
			} catch (Throwable e) {
				logError(LOG, e, "Issue copying properties using JSON for source class [%s] and target class [%s] with values [%s] and [%s]!", src.getClass(), target.getClass(), src, target);
			}
		}
		return target;
	}

	/**
	 * Copy properties ignore invalid.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param type the type
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copyPropertiesIgnoreInvalid(Object src, Class<T> type) {
		Map<String, Object> srcMap = src instanceof Map ? (Map<String, Object>) src : Serializer.toMap(src);
		Set<String> targetFields = Stream.of(BeanUtils.getPropertyDescriptors(type)).map(pd -> pd.getName()).collect(Collectors.toSet());
		Set<String> srcMapKeys = Sets.newHashSet(srcMap.keySet());
		srcMapKeys.stream().filter(key -> !targetFields.contains(key)).forEach(srcMap::remove);
		return Serializer.fromMap(srcMap, type);
	}

	/**
	 * Copy properties ignore null.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @return the object
	 */
	public static <T> T copyPropertiesIgnoreNull(Object src, T target) {
		BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
		return target;
	}

	/**
	 * Copy properties ignore null and existing.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @return the object
	 */
	public static <T> T copyPropertiesIgnoreNullAndExisting(Object src, T target) {
		BeanUtils.copyProperties(src, target, getNullAndExistingPropertyNames(src, target));
		return target;
	}

	/**
	 * Copy specific properties.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @param fields the fields
	 * @return the t
	 */
	public static <T> T copySpecificProperties(Object src, T target, Collection<String> fields) {
		return copySpecificProperties(src, target, Stream.of(BeanUtils.getPropertyDescriptors(src.getClass())).filter(pd -> fields.contains(pd.getName())).collect(Collectors.toList()));
	}

	/**
	 * Copy specific properties.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @param pds the pds
	 * @return the t
	 */
	public static <T> T copySpecificProperties(Object src, T target, List<PropertyDescriptor> pds) {
		pds.stream().filter(pd -> (pd.getReadMethod() != null) && (pd.getWriteMethod() != null)).forEach(
			pd -> {
				Object val = Reflections.invokeSafely(pd.getReadMethod(), src);
				if (val != null) {
					Reflections.invokeSafely(pd.getWriteMethod(), target, val);
				}
			}
		);
		return target;
	}

	/**
	 * Copy valid fields.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @return the t
	 */
	public static <T> T copyValidFields(@Nonnull Object src, @Nonnull T target) {
		return copyValidFields(src, target, null);
	}

	/**
	 * Copy valid fields.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param target the target
	 * @param ignoreFields the ignore fields
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copyValidFields(@Nonnull Object src, @Nonnull T target, @Nullable Set<String> ignoreFields) {
		Map<String, Object> srcMap = src instanceof Map ? (Map<String, Object>) src : Serializer.toMap(src);
		Set<String> targetFields = Stream.of(BeanUtils.getPropertyDescriptors(target.getClass())).map(pd -> pd.getName()).collect(Collectors.toSet());
		Set<String> srcMapKeys = Sets.newHashSet(srcMap.keySet());
		srcMapKeys.stream().filter(key -> !targetFields.contains(key)).forEach(srcMap::remove);
		return Serializer.copyProperties(srcMap, target, ignoreFields);
	}

	/**
	 * Creates the json map.
	 *
	 * @return the map
	 */
	public static Map<String, Object> createJsonMap() {
		return createJsonMap(null);
	}

	/**
	 * Creates the json map.
	 *
	 * @param success the success
	 * @return the map
	 */
	public static Map<String, Object> createJsonMap(Boolean success) {
		Map<String, Object> map = createOrderedMapCaseInsensitive();
		if (success != null) {
			map.put("success", success);
		}
		return map;
	}

	/**
	 * Creates the ordered map case insensitive.
	 *
	 * @param <T> the generic type
	 * @return the map
	 */
	public static <T> Map<String, T> createOrderedMapCaseInsensitive() {
		return new TreeMap<>(CaseInsensitiveStringSort.INSTANCE);
	}

	/**
	 * Deserialize env specific.
	 *
	 * @param <I> the generic type
	 * @param <R> the generic type
	 * @param input the input
	 * @param deserializer the deserializer
	 * @param valueType the value type
	 * @return the r
	 */
	public static <I, R> R deserializeEnvSpecific(I input, BiFunction<I, JavaType, Object> deserializer, Class<R> valueType) {
		return deserializeEnvSpecific(input, deserializer, Serializer.constructType(valueType));
	}

	/**
	 * Deserialize env specific.
	 *
	 * @param <I> the generic type
	 * @param <R> the generic type
	 * @param input the input
	 * @param deserializer the deserializer
	 * @param valueType the value type
	 * @return the r
	 */
	public static <I, R> R deserializeEnvSpecific(I input, BiFunction<I, JavaType, Object> deserializer, JavaType valueType) {
		JavaType configType = getTypeFactory().constructParametricType(EnvSpecificConfigWrapper.class, MAP_TYPE);
		@SuppressWarnings("unchecked")
		EnvSpecificConfigWrapper<Map<String, Object>> configWrapper = (EnvSpecificConfigWrapper<Map<String, Object>>) deserializer.apply(input, configType);
		if ((configWrapper == null) || (configWrapper.getConfigs() == null)) {
			return null;
		}
		final MergeConfig mergeConfig = Checks.defaultIfNull(configWrapper.getMergeConfig(), Merges.OVERWRITE_MERGE_CONFIG);
		Map<String, Object> base = configWrapper.getConfigs().stream().filter(EnvSpecificConfig::checkIsDefault).map(EnvSpecificConfig::getValue).findFirst().orElse(null);
		for (EnvSpecificConfig<Map<String, Object>> config : configWrapper.getConfigs()) {
			if (!config.checkEnvConfigsMatch()) {
				continue;
			}
			if (base == null) {
				base = config.getValue();
			} else {
				Merges.deepMerge(config.getValue(), base, mergeConfig);
			}
		}
		return Serializer.fromMap(base, valueType);
	}

	/**
	 * Deserialize env specific yaml.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T deserializeEnvSpecificYaml(File file, Class<T> type) {
		return deserializeEnvSpecificYaml(file, constructType(type));
	}

	/**
	 * Deserialize env specific yaml.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T deserializeEnvSpecificYaml(File file, JavaType type) {
		return deserializeEnvSpecific(file, Serializer::fromYaml, type);
	}

	/**
	 * Finalize object.
	 *
	 * @param <T> the generic type
	 * @param object the object
	 * @return the t
	 */
	public static <T> T finalizeObject(T object) {
		// NB: handling this via a Jackson convert now
		// if ((object != null) && (object instanceof ExternalizableBean)) {
		// ((ExternalizableBean) object).afterDeserialization();
		// }
		return object;
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @return the list
	 */
	public static <T> List<T> fromCsv(File file) {
		return fromCsv(file, null, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromCsv(File file, Class<T> type) {
		return fromCsv(file, type, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param opts the opts
	 * @return the list
	 */
	public static <T> List<T> fromCsv(File file, Class<T> type, FileParserOptions opts) {
		try {
			return fromCsvWithException(file, type, opts);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read CSV file: file=%s type=%s opts=%s", Logs.getSplunkValueString(FileUtil.fullPath(file)), type, opts);
		}
		return null;
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param opts the opts
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromCsv(File file, FileParserOptions opts) {
		return fromCsv(file, opts == null ? null : (Class<T>) opts.getType(), opts);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @return the list
	 */
	public static <T> List<T> fromCsv(@Nonnull InputStream stream) {
		return fromCsv(stream, null, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromCsv(@Nonnull InputStream stream, Class<T> type) {
		return fromCsv(stream, type, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @param opts the opts
	 * @return the list
	 */
	public static <T> List<T> fromCsv(InputStream stream, Class<T> type, FileParserOptions opts) {
		List<T> object = null;
		try {
			object = new FileParser<>(stream, type, opts).parseFile().getResultList();
		} catch (Throwable e) {
			logError(LOG, e, "Could not read CSV stream: type=%s opts=%s", type, opts);
		}
		return finalizeObject(object);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the list
	 */
	public static <T> List<T> fromCsv(String data) {
		return fromCsv(data, null, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromCsv(String data, Class<T> type) {
		return fromCsv(data, type, null);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param type the type
	 * @param opts the opts
	 * @return the list
	 */
	public static <T> List<T> fromCsv(String data, Class<T> type, FileParserOptions opts) {
		List<T> object = null;
		try {
			object = new FileParser<>(data, type, opts).parseFile().getResultList();
		} catch (Throwable e) {
			logError(LOG, e, "Could not read CSV [%s] of type [%s] with opts [%s].", data, type, opts);
		}
		return finalizeObject(object);
	}

	/**
	 * From csv.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param opts the opts
	 * @return the list
	 */
	public static <T> List<T> fromCsv(String data, FileParserOptions opts) {
		return fromCsv(data, null, opts);
	}

	/**
	 * From csv with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param opts the opts
	 * @return the list
	 * @throws FileParsingException the file parsing exception
	 */
	public static <T> List<T> fromCsvWithException(File file, Class<T> type, FileParserOptions opts) throws FileParsingException {
		List<T> object = new FileParser<>(file, type, opts).parseFile().getResultList();
		return finalizeObject(object);
	}

	/**
	 * From csv with path keys.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromCsvWithPathKeys(File file, Class<T> type) {
		return fromCsvWithPathKeys(file, type, new FileParserOptions());
	}

	/**
	 * From csv with path keys.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param opts the opts
	 * @return the list
	 */
	public static <T> List<T> fromCsvWithPathKeys(File file, Class<T> type, FileParserOptions opts) {
		if (opts == null) {
			opts = new FileParserOptions();
		}
		opts.setUsePathKeys(true);
		return fromCsv(file, type, opts);
	}

	/**
	 * From file using simple.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromFileUsingSimple(File file, JavaType type) {
		String ext = StringUtils.lowerCase(FilenameUtils.getExtension(file.getPath()));
		return "json".equalsIgnoreCase(ext) ? fromJson(file, type) : fromXmlUsingSimple(file, type);
	}

	/**
	 * From file.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(File file, Class<T> type) {
		try {
			return fromJsonWithException(FileUtil.openInputStreamSafe(file), type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read file [%s] for class [%s] to object.", file, type);
			logDebug(LOG, "Data string that could not be read is:\n%s.", (file == null) || !file.isFile() ? null : FileUtil.readFile(file));
			return null;
		}
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(File file, JavaType type) {
		try {
			return fromJsonWithException(FileUtil.openInputStreamSafe(file), type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read file [%s] for class [%s] to object.", file, type);
			logDebug(LOG, "Data string that could not be read is:\n%s.", (file == null) || !file.isFile() ? null : FileUtil.readFile(file));
			return null;
		}
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(InputStream stream, Class<T> type) {
		return fromJson(JSON_MAPPER, stream, type);
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(InputStream stream, JavaType type) {
		return fromJson(JSON_MAPPER, stream, type);
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(ObjectMapper mapper, InputStream stream, Class<T> type) {
		return fromJson(mapper, stream, constructType(type));
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(ObjectMapper mapper, InputStream stream, JavaType type) {
		return readValue(mapper, stream, type);
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		return fromJson(json, JSON_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromJson(String json, JavaType type) {
		return readValue(JSON_MAPPER, json, type);
	}

	/**
	 * From json into list.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromJsonIntoList(File file, Class<T> type) {
		if ((file == null) || !file.isFile()) {
			return null;
		}
		String json = FileUtil.readFile(file);
		return fromJsonIntoList(json, type);
	}

	/**
	 * From json into list.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> fromJsonIntoList(String json, Class<T> type) {
		if (isBlank(json)) {
			return null;
		}
		return isJsonList(json) ? readJsonAsList(json, type) : new ArrayList<>(Arrays.asList(fromJson(json, type)));
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJsonUnknown(String json) {
		return (T) fromJson(json, Object.class);
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(File file, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(FileUtil.openInputStreamSafe(file), type);
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(File file, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(FileUtil.openInputStreamSafe(file), type);
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(InputStream stream, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(JSON_MAPPER, stream, type);
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(InputStream stream, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(JSON_MAPPER, stream, type);
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(ObjectMapper mapper, InputStream stream, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(mapper, stream, constructType(type));
	}

	/**
	 * From json with exception.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(ObjectMapper mapper, InputStream stream, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		return readValueWithException(mapper, stream, type);
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(String json, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		return fromJsonWithException(json, JSON_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonWithException(String json, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		return readValueWithException(JSON_MAPPER, json, type);
	}

	/**
	 * From json with type guess.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromJsonWithTypeGuess(String data) {
		String typeString = null;
		T object = null;
		try {
			if (data == null) {
				return null;
			}
			final JsonNode jsonNode = JSON_MAPPER.readTree(data);
			Class<T> type;
			if (jsonNode.isObject() && Checks.isNotBlank(typeString = jsonNode.get(TYPE_PROPERTY).asText())) {
				type = (Class<T>) Class.forName(CustomTypeIdResolver.getCorrectClassName(typeString));
				object = JSON_MAPPER.convertValue(jsonNode, type);
			} else {
				object = (T) JSON_MAPPER.convertValue(jsonNode, Object.class);
			}
		} catch (Throwable e) {
			logError(LOG, e, "Could not read data string for class [%s] to object.", typeString);
			logDebug(LOG, "Data string that could not be read is:\n%s", data);
		}
		return finalizeObject(object);
	}

	/**
	 * From map.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromMap(Map<String, Object> map, Class<T> type) {
		return fromMap(map, constructType(type));
	}

	/**
	 * From map.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromMap(Map<String, Object> map, JavaType type) {
		return fromMap(JSON_MAPPER, map, type);
	}

	/**
	 * From map.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param map the map
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromMap(ObjectMapper mapper, Map<String, Object> map, Class<T> type) {
		return fromMap(mapper, map, constructType(type));
	}

	/**
	 * From map.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param map the map
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromMap(ObjectMapper mapper, Map<String, Object> map, JavaType type) {
		T object = null;
		try {
			if (map != null) {
				map.remove(TYPE_PROPERTY);
			}
			object = mapper.convertValue(map, type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read map [%s] to object of type [%s].", map, type);
		}
		return object;
	}

	/**
	 * From map with reflection.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param current the current
	 * @return the t
	 */
	public static <T> T fromMapWithReflection(Map<String, Object> map, T current) {
		if (isNotEmpty(map) && (current != null)) {
			map.entrySet().stream().filter(e -> (Reflections.getSetterForField(current.getClass(), e.getKey()) != null) && (e.getValue() != null)).forEach(
				entry -> {
					Object val = entry.getValue();
					Method setter = Reflections.getSetterForField(current.getClass(), entry.getKey());
					Class<?> setterType = setter.getParameters()[0].getType();
					if (!setterType.isAssignableFrom(val.getClass())) {
						val = Serializer.fromJson(Serializer.toJson(val), setterType);
					}
					Reflections.invokeSafely(setter, current, val);
				}
			);
		}
		return current;
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(byte[] data, Class<T> type) {
		return fromSmile(data, SMILE_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(byte[] data, JavaType type) {
		return readValue(SMILE_MAPPER, data, type);
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(File file, Class<T> type) {
		return fromSmile(file, SMILE_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(File file, JavaType type) {
		try {
			return readValueWithException(SMILE_MAPPER, FileUtil.openInputStreamSafe(file), type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read file to object: file=%s type=%s", file, type);
			return null;
		}
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(InputStream stream, Class<T> type) {
		return fromSmile(stream, SMILE_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(InputStream stream, JavaType type) {
		return readValue(SMILE_MAPPER, stream, type);
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param base64Data the base 64 data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(String base64Data, Class<T> type) {
		return fromSmile(base64Data, SMILE_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From smile.
	 *
	 * @param <T> the generic type
	 * @param base64Data the base 64 data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromSmile(String base64Data, JavaType type) {
		return readValue(SMILE_MAPPER, Base64.decodeBase64(base64Data), type);
	}

	/**
	 * From tree.
	 *
	 * @param <T> the generic type
	 * @param tree the tree
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromTree(JsonNode tree, Class<T> type) {
		return fromTree(JSON_MAPPER, tree, type);
	}

	/**
	 * From tree.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param tree the tree
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromTree(ObjectMapper mapper, JsonNode tree, Class<T> type) {
		try {
			return mapper.treeToValue(tree, type);
		} catch (Throwable e) {
			logError(LOG, e, "Issue reading JsonNode to value of type [%s]", type);
			logDebug(LOG, "JsonNode value is [%s]", toJson(tree));
		}
		return null;
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromUnknown(File file, Class<T> type) {
		return fromUnknown(file, type, null);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 */
	public static <T> T fromUnknown(File file, Class<T> type, OutputFormat defaultFormat) {
		return fromUnknown(file, constructType(type), defaultFormat);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromUnknown(File file, JavaType type) {
		return fromUnknown(file, type, null);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 */
	public static <T> T fromUnknown(File file, JavaType type, OutputFormat defaultFormat) {
		try {
			return fromUnknownWithException(file, type, defaultFormat);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read file [%s] for class [%s] to object.", file, type);
			logDebug(LOG, "Data string that could not be read is:\n%s.", (file == null) || !file.isFile() ? null : FileUtil.readFile(file));
			return null;
		}
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param content the content
	 * @param fileName the file name
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromUnknown(String content, String fileName) {
		return (T) fromUnknown(content, fileName, Object.class, null);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param content the content
	 * @param fileName the file name
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 */
	public static <T> T fromUnknown(String content, String fileName, Class<T> type, OutputFormat defaultFormat) {
		return fromUnknown(content, fileName, constructType(type), defaultFormat);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param content the content
	 * @param fileName the file name
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 */
	public static <T> T fromUnknown(String content, String fileName, JavaType type, OutputFormat defaultFormat) {
		try {
			return fromUnknownWithException(content, fileName, type, defaultFormat);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read file [%s] for class [%s] to object.", fileName, type);
			logTrace(LOG, "Data string that could not be read is:\n%s.", content);
			return null;
		}
	}

	/**
	 * From unknown with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromUnknownWithException(File file, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
		return fromUnknownWithException(file, type, null);
	}

	/**
	 * From unknown with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromUnknownWithException(File file, Class<T> type, OutputFormat defaultFormat) throws JsonParseException, JsonMappingException, IOException {
		return fromUnknownWithException(file, constructType(type), defaultFormat);
	}

	/**
	 * From unknown with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromUnknownWithException(File file, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		return fromUnknownWithException(file, type, null);
	}

	/**
	 * From unknown with exception.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromUnknownWithException(File file, JavaType type, OutputFormat defaultFormat) throws JsonParseException, JsonMappingException, IOException {
		return fromUnknownWithException(FileUtil.readFile(file), file.getName(), type, defaultFormat);
	}

	/**
	 * From unknown.
	 *
	 * @param <T> the generic type
	 * @param content the content
	 * @param fileName the file name
	 * @param type the type
	 * @param defaultFormat the default format
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromUnknownWithException(String content, String fileName, JavaType type, OutputFormat defaultFormat) throws JsonParseException, JsonMappingException, IOException {
		OutputFormat format = Enums.fromString(OutputFormat.class, FilenameUtils.getExtension(fileName), defaultFormat);
		String data = StringUtils.trim(content);
		if (format == null) {
			if (data == null) {
				throw new IllegalArgumentException(String.format("Could not determine format from file [%s].", fileName));
			} else if (isJsonObject(data)) {
				format = OutputFormat.json;
			} else if (isXml(data)) {
				format = OutputFormat.xml;
			} else if (Character.isLetter(data.charAt(0))) {
				format = OutputFormat.yaml;
			} else {
				throw new IllegalArgumentException(String.format("Could not determine format from file [%s].", fileName));
			}
		}
		switch (format) {
		case csv:
			return (T) fromCsv(data, type.getRawClass());
		case json:
			return fromJsonWithException(data, type);
		case xml:
			return fromXml(data, type);
		case yaml:
			return fromYaml(data, type);
		default:
			throw new IllegalArgumentException(String.format("Format [%s] not supported!", format));
		}
	}

	/**
	 * From xls.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @return the map
	 */
	public static <T> Map<String, List<T>> fromXls(File file) {
		return fromXls(file, null, null);
	}

	/**
	 * From xls.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the map
	 */
	public static <T> Map<String, List<T>> fromXls(File file, Class<T> type) {
		return fromXls(file, type, null);
	}

	/**
	 * Convert excel to maps.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param opts the options
	 * @return the map
	 */
	public static <T> Map<String, List<T>> fromXls(File file, Class<T> type, FileParserOptions opts) {
		Map<String, List<T>> result = Maps.newLinkedHashMap();
		try (Workbook book = file.getName().endsWith(".xlsx") ? ExcelFactory.readXlsx(file) : ExcelFactory.readXls(file)) {
			if (book == null) {
				return null;
			}
			if (opts == null) {
				opts = new FileParserOptions();
			}
			boolean useExcelFormats = opts.testUseExcelFormats();
			opts.setSepChar('\t');
			int sheetCount = book.getNumberOfSheets();
			HSSFDataFormatter formatter = new HSSFDataFormatter();
			Map<String, Integer> firstRowNumBySheet = opts.getFirstRowNumBySheet();
			for (int sheetPos = 0; sheetPos < sheetCount; sheetPos++) {
				Sheet sheet = book.getSheetAt(sheetPos);
				String sheetPosStr = String.valueOf(sheetPos);
				String sheetName = sheet.getSheetName();
				if (Checks.isNotEmpty(opts.getSkipSheets()) && (opts.getSkipSheets().contains(sheetName) || opts.getSkipSheets().contains(sheetPosStr))) {
					continue;
				} else if (Checks.isNotEmpty(opts.getOnlySheets()) && !(opts.getOnlySheets().contains(sheetName) || opts.getOnlySheets().contains(sheetPosStr))) {
					continue;
				}
				List<String[]> allRows = new ArrayList<>();
				int firstRow = opts.getFirstRowNum() != null ? opts.getFirstRowNum() : sheet.getFirstRowNum();
				if (firstRowNumBySheet != null) {
					firstRow = Stream.of(sheetName, sheetPosStr).map(key -> firstRowNumBySheet.get(key)).findFirst().orElse(firstRow);
				}
				int lastRow = sheet.getLastRowNum();
				for (int j = firstRow; j <= lastRow; j++) {
					Row row = sheet.getRow(j);
					if (row == null) {
						continue;
					}
					int firstCell = 0;
					int lastCell = row.getLastCellNum();
					String[] text = lastCell <= 0 ? null : new String[lastCell];
					for (int k = firstCell; k < lastCell; k++) {
						Object cellVal = null;
						Cell cell = row.getCell(k);
						if (cell != null) {
							switch (cell.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								if (useExcelFormats) {
									cellVal = cell.getRichStringCellValue().getString();
								} else {
									cellVal = cell.getStringCellValue();
								}
								break;
							case Cell.CELL_TYPE_NUMERIC:
								if (useExcelFormats) {
									cellVal = formatter.formatCellValue(cell);
								} else {
									cellVal = cell.getNumericCellValue();
								}
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								cellVal = cell.getBooleanCellValue();
								break;
							case Cell.CELL_TYPE_ERROR:
								cellVal = ErrorEval.getText(cell.getErrorCellValue());
								break;
							case Cell.CELL_TYPE_FORMULA:
								cellVal = cell.getCellFormula();
								switch (cell.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									cellVal = cell.getNumericCellValue();
									break;
								case Cell.CELL_TYPE_STRING:
									cellVal = cell.getRichStringCellValue();
									break;
								default:
									break;
								}
								break;
							case Cell.CELL_TYPE_BLANK:
								if ((k == 0) && (j == 0)) {
									cellVal = "key";
								}
								break;
							default:
								throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ')');
							}
						}
						text[k] = cellVal == null ? StringUtils.EMPTY : cellVal.toString();
					}
					if (text != null) {
						allRows.add(text);
					}
				}
				StringWriter csvString = new StringWriter();
				try (CSVWriter csvWriter = new CSVWriter(csvString, opts.getSepChar())) {
					csvWriter.writeAll(allRows, false);
				}
				FileParser<T> parser = new FileParser<>(csvString.toString(), type, opts);
				result.put(sheetName, parser.parseFile().getResultList());
			}
		} catch (AbstractMethodError e) {
			logWarn(LOG, e, "Issue reading Excel file [%s]", file);
		} catch (IOException | FileParsingException e) {
			return Exceptions.sneakyThrow(e);
		}
		return result;
	}

	/**
	 * From xls.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param opts the opts
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, List<T>> fromXls(File file, FileParserOptions opts) {
		return fromXls(file, opts == null ? null : (Class<T>) opts.getType(), opts);
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromXml(File xml) {
		return (T) fromXml(xml, Object.class);
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(File xml, Class<T> type) {
		return fromXml(FileUtil.openInputStreamSafe(xml), constructType(type));
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(File xml, JavaType type) {
		return fromXml(FileUtil.openInputStreamSafe(xml), type);
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(InputStream xml, Class<T> type) {
		return readValue(XML_MAPPER, xml, constructType(type));
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(InputStream xml, JavaType type) {
		return readValue(XML_MAPPER, xml, type);
	}

	/**
	 * Read xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T fromXml(String xml) {
		return (T) fromXml(xml, Object.class);
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(String xml, Class<T> type) {
		return fromXml(xml, constructType(type));
	}

	/**
	 * From xml.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXml(String xml, JavaType type) {
		return readValue(XML_MAPPER, xml, type);
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(File file, Class<T> type) {
		return fromXmlUsingSimple(file, type, false);
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param strict the strict
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(File file, Class<T> type, boolean strict) {
		return fromXmlUsingSimple(file, constructType(type), strict);
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(File file, JavaType type) {
		return fromXmlUsingSimple(file, type, false);
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param strict the strict
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(File file, JavaType type, boolean strict) {
		T content = null;
		logDebug(LOG, "deserialiseFile(%s, %s)", file, type);
		try (InputStream stream = new FileInputStream(file)) {
			content = fromXmlUsingSimple(stream, type, strict);
			if (content instanceof Content) {
				((Content) content).setPath(file.getPath());
				((Content) content).setLastModified(file.lastModified());
			}
		} catch (Throwable e) {
			logError(LOG, e, "Deserialisation of file [%s] into bean of type [%s] failed", file, type);
		}
		return content;
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(InputStream stream, Class<T> type) {
		return fromXmlUsingSimple(stream, constructType(type));
	}

	/**
	 * From xml using simple.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlUsingSimple(InputStream stream, JavaType type) {
		return fromXmlUsingSimple(stream, type, false);
	}

	/**
	 * Deserialise file.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @param strict the strict
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromXmlUsingSimple(InputStream stream, JavaType type, boolean strict) {
		T content = null;
		try (InputStream tmp = stream) {
			content = (T) XML_READ_SERIALIZER.read(type.getRawClass(), stream, strict);
		} catch (Throwable e) {
			logError(LOG, e, "Deserialisation of stream into bean of type [%s] failed", type);
		}
		return content;
	}

	/**
	 * From xml with list support.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromXmlWithListSupport(File xml) {
		return (T) fromXmlWithListSupport(xml, Object.class);
	}

	/**
	 * From xml with list support.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlWithListSupport(File xml, Class<T> type) {
		return fromJson(XML.toJSONObject(FileUtil.readFile(xml)).toString(), type);
	}

	/**
	 * From xml with list support.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromXmlWithListSupport(String xml) {
		return (T) fromXmlWithListSupport(xml, Object.class);
	}

	/**
	 * From xml with list support.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromXmlWithListSupport(String xml, Class<T> type) {
		return fromJson(XML.toJSONObject(xml).toString(), type);
	}

	/**
	 * From file.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(File file, Class<T> type) {
		return fromYaml(FileUtil.openInputStreamSafe(file), type);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(File file, JavaType type) {
		return fromYaml(FileUtil.openInputStreamSafe(file), type);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(InputStream stream, Class<T> type) {
		return fromYaml(YAML_MAPPER, stream, type);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(InputStream stream, JavaType type) {
		return fromYaml(YAML_MAPPER, stream, type);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(ObjectMapper mapper, InputStream stream, Class<T> type) {
		return fromYaml(mapper, stream, constructType(type));
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(ObjectMapper mapper, InputStream stream, JavaType type) {
		T object = null;
		try (InputStream tmp = stream) {
			if (stream != null) {
				object = mapper.convertValue(fromYamlUsingSnakeYaml(stream), type);
			}
		} catch (Throwable e) {
			logError(LOG, e, "Could not read YAML stream for class [%s] to object.", type);
			logDebug(LOG, "YAML string that could not be read is:\n%s.", stream == null ? null : readStream(stream));
		}
		return finalizeObject(object);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param yaml the yaml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(String yaml, Class<T> type) {
		return fromYaml(yaml, YAML_MAPPER.getTypeFactory().constructType(type));
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param yaml the yaml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYaml(String yaml, JavaType type) {
		T object = null;
		try {
			object = yaml == null ? null : YAML_MAPPER.convertValue(fromYamlUsingSnakeYaml(yaml), type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read data string for class [%s] to object.", type);
			logDebug(LOG, "Data string that could not be read is:\n%s", yaml);
		}
		return finalizeObject(object);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param yaml the yaml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromYamlUnknown(File yaml) {
		return (T) fromYamlUsingSnakeYaml(yaml);
	}

	/**
	 * From yaml.
	 *
	 * @param <T> the generic type
	 * @param yaml the yaml
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromYamlUnknown(String yaml) {
		return (T) fromYamlUsingSnakeYaml(yaml);
	}

	/**
	 * From yaml using snake yaml.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the t
	 */
	public static <T> T fromYamlUsingSnakeYaml(File data) {
		return fromYamlUsingSnakeYaml(FileUtil.openInputStreamSafe(data));
	}

	/**
	 * From yaml using snake yaml.
	 *
	 * @param <T> the generic type
	 * @param stream the stream
	 * @return the t
	 */
	public static <T> T fromYamlUsingSnakeYaml(InputStream stream) {
		Yaml yaml = new Yaml(new PropertyParsingConstructor());
		//yaml.addImplicitResolver(PropertyParsingConstructor.CUSTOM_TAG, PropertyParsingConstructor.CUSTOM_PATTERN, "$");
		try (InputStream tmp = stream) {
			T val = yaml.load(stream);
			if (val instanceof Map) {
				((Map<?, ?>) val).remove("_anchors");
			}
			return val;
		} catch (IOException e) {
			String text = null;
			try {
				text = stream == null ? null : IOUtils.toString(stream, UTF_8);
			} catch (IOException e1) {
				;
			}
			Logs.logError(LOG, e, "Issue deserializing YAML stream: text=%s", text);
			return null;
		}
	}

	/**
	 * From yaml using snake yaml.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @return the t
	 */
	public static <T> T fromYamlUsingSnakeYaml(String data) {
		Yaml yaml = new Yaml(new PropertyParsingConstructor());
		//yaml.addImplicitResolver(PropertyParsingConstructor.CUSTOM_TAG, PropertyParsingConstructor.CUSTOM_PATTERN, "$");
		T val = yaml.load(data);
		if (val instanceof Map) {
			((Map<?, ?>) val).remove("_anchors");
		}
		return val;
	}

	/**
	 * From yaml with exception.
	 *
	 * @param <T> the generic type
	 * @param yaml the yaml
	 * @param type the type
	 * @return the t
	 */
	public static <T> T fromYamlWithException(String yaml, JavaType type) {
		Object parsed = fromYamlUsingSnakeYaml(yaml);
		return YAML_MAPPER.convertValue(parsed, type);
	}

	/**
	 * Gets the converted csv data.
	 *
	 * @param orig the orig
	 * @param outputParams the output params
	 * @param exprContext the expr context
	 * @return the converted csv data
	 */
	@SuppressWarnings("unchecked")
	public static Object getConvertedCsvData(Object orig, OutputParams outputParams, @Nullable final Map<String, Object> exprContext) {
		if ((outputParams == null) || (orig == null) || (orig instanceof String)) {
			return orig;
		}
		if (isNotEmpty(outputParams.getPostProcessExpressions())) {
			ExprFactory.evaluateOutputExpressions(outputParams.getPostProcessExpressions(), orig, true, exprContext);
		}
		if (isNotEmpty(outputParams.getCreateExpressions())) {
			Map<String, Object> created = ExprFactory.evaluateOutputExpressions(outputParams.getCreateExpressions(), orig, true, exprContext);
			orig = created;
		}
		Collection<Object> objs = Collection.class.isAssignableFrom(orig.getClass()) ? (Collection<Object>) orig : Lists.newArrayList(orig);
		if ((outputParams.getCsvType() != null) && (orig.getClass().getSimpleName().endsWith("ResultBeans"))) {
			String resultMap = orig.getClass().getSimpleName().equals("GenericResultBeans") ? "genericResultMap" : "resultMap";
			List<Object> resultBeans = ExprFactory.evalSpel(orig, resultMap + "['" + outputParams.getCsvType() + "']");
			if (isNotEmpty(resultBeans)) {
				objs = Lists.newArrayList(resultBeans);
			}
		}
		String dataExpression = outputParams.getOutputDataExpression();
		if (isNotBlank(dataExpression) && isNotEmpty(objs)) {
			SpelExpression spel = outputParams.testOgnl() ? null : ExprFactory.getSpelExpression(dataExpression);
			Node ognl = !outputParams.testOgnl() ? null : ExprFactory.createOgnlExpression(dataExpression);
			objs = objs
				.stream()
				.map(
					objsInner -> {
						Object mappedObj = ognl != null ? evalOgnl(objsInner, ognl) : ExprFactory.evalSpel(objsInner, spel);
						return mappedObj instanceof Collection ? (Collection<Object>) mappedObj : mappedObj != null ? Lists.newArrayList(mappedObj) : null;
					}
				)
				.filter(o -> isNotEmpty(o))
				.flatMap(
					o -> {
						if (o.iterator().next() instanceof Collection) {
							return o.stream().flatMap(
								objColl -> {
									return ((Collection<Object>) objColl).stream();
								}
							);
						}
						return o.stream();
					}
				)
				.collect(Collectors.toList());
		}
		return objs;
	}

	/**
	 * Gets the csv serializer.
	 *
	 * @param obj the obj
	 * @return the csv serializer
	 */
	public static CsvSerializer getCsvSerializer(Object obj) {
		return getCsvSerializer(obj, null);
	}

	/**
	 * Gets the csv serializer.
	 *
	 * @param orig the orig
	 * @param outputParams the output params
	 * @return the csv serializer
	 */
	public static CsvSerializer getCsvSerializer(Object orig, OutputParams outputParams) {
		if (outputParams == null) {
			return new CsvSerializer(orig);
		}
		CsvSerializer csv = new CsvSerializer(getConvertedCsvData(orig, outputParams, null), outputParams);
		return addParamsToCsvSerializer(csv, outputParams);
	}

	/**
	 * Gets the existing property names as set.
	 *
	 * @param target the target
	 * @return the existing property names as set
	 */
	public static Set<String> getExistingPropertyNamesAsSet(Object target) {
		final BeanWrapper src = new BeanWrapperImpl(target);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> existingNames = new HashSet<>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue != null) {
				existingNames.add(pd.getName());
			}
		}
		return existingNames;
	}

	/**
	 * Gets the failure map.
	 *
	 * @return the failure map
	 */
	public static Map<String, Object> getFailureMap() {
		return getFailureMap(null);
	}

	/**
	 * Gets the failure map.
	 *
	 * @param failureReason the failure reason
	 * @return the failure map
	 */
	public static Map<String, Object> getFailureMap(@Nullable String failureReason) {
		Map<String, Object> map = new HashMap<>();
		map.put("success", false);
		if (failureReason != null) {
			map.put("failureReason", failureReason);
		}
		return map;
	}

	/**
	 * Gets the incremental output separators.
	 *
	 * @param outputFormat the output format
	 * @return the incremental output separators
	 */
	public static Triple<String, String, String> getIncrementalOutputSeparators(OutputFormat outputFormat) {
		if (outputFormat == null) {
			outputFormat = OutputFormat.json;
		}
		switch (outputFormat) {
		case json:
			return Triple.of("[", ",", "]");
		default:
			return Triple.of("", "", "");
		}
	}

	/**
	 * Gets the mapper and execute expressions.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the mapper and execute expressions
	 */
	public static Pair<ObjectMapper, Object> getMapperAndExecuteExpressions(Object obj, @Nonnull OutputParams outputParams) {
		return getMapperAndExecuteExpressions(obj, outputParams, null);
	}

	/**
	 * Gets the mapper and execute expressions.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @param exprContext the expr context
	 * @return the mapper and execute expressions
	 */
	public static Pair<ObjectMapper, Object> getMapperAndExecuteExpressions(
		Object obj,
		@Nonnull OutputParams outputParams,
		@Nullable final Map<String, Object> exprContext
	) {
		ObjectMapper mapper = SerializerFactory.resolveObjectMapper(outputParams);
		if (obj != null) {
			if ((outputParams.getOutputFormat() == csv) || (outputParams.getOutputFormat() == xls) || (outputParams.getOutputFormat() == xlsx)) {
				obj = getConvertedCsvData(obj, outputParams, exprContext);
			} else {
				if ((outputParams.getCsvType() != null) && (obj.getClass().getSimpleName().endsWith("ResultBeans"))) {
					String resultMap = obj.getClass().getSimpleName().equals("GenericResultBeans") ? "genericResultMap" : "resultMap";
					obj = ExprFactory.evalSpel(obj, resultMap + "['" + outputParams.getCsvType() + "']");
				}
				if (isNotBlank(outputParams.getOutputDataExpression())) {
					String expr = (obj.getClass().getSimpleName().equals("GenericResultBeans")) && outputParams.getOutputDataExpression().startsWith(
						"resultMap"
					) ? "genericResultMap" + removeStart(outputParams.getOutputDataExpression(), "resultMap") : outputParams.getOutputDataExpression();
					obj = outputParams.testOgnl() ? evalOgnl(obj, expr, true, false) : getValueFromPath(obj, expr, true, false);
				}
				if (isNotEmpty(outputParams.getPostProcessExpressions())) {
					ExprFactory.evaluateOutputExpressions(outputParams.getPostProcessExpressions(), obj, true, exprContext);
				}
				if (isNotEmpty(outputParams.getCreateExpressions())) {
					Stopwatch watch = Stopwatch.createAndStart();
					try {
						Object created = ExprFactory.evaluateOutputExpressions(outputParams.getCreateExpressions(), obj, true, exprContext);
						obj = created;
					} finally {
						Logs.logTimer(LOG, watch, "output_create_expressions");
					}
				}
				if ((obj != null) && (outputParams.getCsvSort() != null) && (obj instanceof List)) {
					Sorting.sort((List<?>) obj, SortClause.fromMultiString(outputParams.getCsvSort()));
				}
			}
		}
		return Pair.of(mapper, obj);
	}

	/**
	 * Gets the node from path.
	 *
	 * @param rootNode the root node
	 * @param path the path
	 * @return the node from path
	 */
	public static JsonNode getNodeFromPath(final ObjectNode rootNode, final String path) {
		return getNodeFromPath(rootNode, path, ".");
	}

	/**
	 * Gets the node from path.
	 *
	 * @param rootNode the root node
	 * @param path the path
	 * @param sep the sep
	 * @return the node from path
	 */
	// TODO: support lists, leveraging same logic as DataFactory
	public static JsonNode getNodeFromPath(final ObjectNode rootNode, final String path, final String sep) {
		JsonNode value = null;
		if ((rootNode != null) && (path != null)) {
			String[] keys = StringUtils.split(path, sep);
			ObjectNode current = rootNode;
			for (int index = 0; index < keys.length; index++) {
				value = current.get(keys[index]);
				if (!(value instanceof ObjectNode)) {
					if (!(index == (keys.length - 1))) {
						value = null;
					}
					break;
				}
				current = (ObjectNode) value;
			}
		}
		return value;
	}

	/**
	 * Gets the null and existing property names.
	 *
	 * @param source the source
	 * @param target the target
	 * @return the null and existing property names
	 */
	public static String[] getNullAndExistingPropertyNames(Object source, Object target) {
		Set<String> emptyNames = getNullPropertyNamesAsSet(source);
		Set<String> existingNames = getExistingPropertyNamesAsSet(target);
		emptyNames.addAll(existingNames);
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	/**
	 * Gets the null property names.
	 *
	 * @param source the source
	 * @return the null property names
	 */
	public static String[] getNullPropertyNames(Object source) {
		Set<String> emptyNames = getNullPropertyNamesAsSet(source);
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	/**
	 * Gets the null property names as set.
	 *
	 * @param source the source
	 * @return the null property names as set
	 */
	public static Set<String> getNullPropertyNamesAsSet(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null) {
				emptyNames.add(pd.getName());
			}
		}
		return emptyNames;
	}

	/**
	 * Gets the success map.
	 *
	 * @return the success map
	 */
	public static Map<String, Object> getSuccessMap() {
		return getSuccessMap(null);
	}

	/**
	 * Gets the success map.
	 *
	 * @param results the results
	 * @return the success map
	 */
	public static Map<String, Object> getSuccessMap(Object results) {
		Map<String, Object> map = createJsonMap(true);
		if (results != null) {
			map.put("data", results);
		}
		return map;
	}

	/**
	 * Gets the type factory.
	 *
	 * @return the type factory
	 */
	public static TypeFactory getTypeFactory() {
		return JSON_MAPPER.getTypeFactory();
	}

	/**
	 * Checks if is json list.
	 *
	 * @param json the json
	 * @return true, if is json list
	 */
	public static boolean isJsonList(String json) {
		if (json == null) {
			return false;
		}
		json = StringUtils.trim(json);
		return json.startsWith("[") && json.endsWith("]");
	}

	/**
	 * Checks if is json map.
	 *
	 * @param json the json
	 * @return true, if is json map
	 */
	public static boolean isJsonMap(String json) {
		if (json == null) {
			return false;
		}
		json = json.trim();
		return json.startsWith("{") && json.endsWith("}");
	}

	/**
	 * Checks if is param in JSON format.
	 *
	 * @param json the param
	 * @return true, if param is in JSON format.
	 */
	public static boolean isJsonObject(String json) {
		if (json == null) {
			return false;
		}
		json = StringUtils.trim(json);
		return isJsonMap(json) || isJsonList(json);
	}

	/**
	 * Checks if is log missing properties.
	 *
	 * @return the logMissingProperties
	 */
	public static boolean isLogMissingProperties() {
		return logMissingProperties;
	}

	/**
	 * Checks if is probably json list.
	 *
	 * @param json the json
	 * @return true, if is probably json list
	 */
	public static boolean isProbablyJsonList(String json) {
		if (json == null) {
			return false;
		}
		json = StringUtils.trim(json);
		return json.startsWith("[") || json.endsWith("]");
	}

	/**
	 * Checks if is probably json map.
	 *
	 * @param json the json
	 * @return true, if is probably json map
	 */
	public static boolean isProbablyJsonMap(String json) {
		if (json == null) {
			return false;
		}
		json = json.trim();
		return json.startsWith("{") || json.endsWith("}");
	}

	/**
	 * Checks if is probably json object.
	 *
	 * @param json the json
	 * @return true, if is probably json object
	 */
	public static boolean isProbablyJsonObject(String json) {
		if (json == null) {
			return false;
		}
		json = StringUtils.trim(json);
		return isProbablyJsonMap(json) || isProbablyJsonList(json);
	}

	/**
	 * Checks if is simple output class.
	 *
	 * @param clazz the clazz
	 * @return true, if is simple output class
	 */
	public static boolean isSimpleOutputClass(Class<?> clazz) {
		return SIMPLE_OUTPUT_CLASSES.contains(clazz) || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz) || Enum.class
			.isAssignableFrom(clazz);
	}

	/**
	 * Checks if is xml.
	 *
	 * @param data the data
	 * @return true, if is xml
	 */
	public static boolean isXml(String data) {
		return Lambdas.functionIfNonNull(StringUtils.trim(data), d -> d.startsWith("<") && d.endsWith(">")).orElse(false);
	}

	/**
	 * Json node to object.
	 *
	 * @param node the node
	 * @return the object
	 */
	public static Object jsonNodeToObject(JsonNode node) {
		if (node == null) {
			return null;
		}
		switch (node.getNodeType()) {
		case ARRAY:
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(((ArrayNode) node).elements(), Spliterator.ORDERED), false).map(Serializer::jsonNodeToObject).collect(
				Collectors.toList()
			);
		case BINARY:
			try {
				return node.binaryValue();
			} catch (IOException e) {
				Logs.logError(LOG, e, "Error getting node as binary value [%s]", node);
				return null;
			}
		case BOOLEAN:
			return node.asBoolean();
		case MISSING:
			return null;
		case NULL:
			return null;
		case NUMBER:
			return node.numberValue();
		case OBJECT:
			return toMap(node);
		case POJO:
			return ((POJONode) node).getPojo();
		case STRING:
		default:
			return node.asText();
		}
	}

	/**
	 * Output.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @return the string
	 */
	public static String output(final Object obj, final OutputParams params) {
		return output(obj, params, null);
	}

	/**
	 * Output.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param exprContext the expr context
	 * @return the string
	 */
	public static String output(final Object obj, final OutputParams params, @Nullable final Map<String, Object> exprContext) {
		if (params.getOutputFormat() == null) {
			params.updateOutputFormatIfAllowed(OutputFormat.json);
		}
		final OutputFormat format = params.getOutputFormat() == null ? OutputFormat.json : params.getOutputFormat();
		BiFunction<ObjectMapper, Object, String> firstFunction = null;
		Function<String, String> wrapperFunction = null;
		switch (format) {
		case csv:
		case xls:
		case xlsx:
			firstFunction = (mapper, data) -> addParamsToCsvSerializer(new CsvSerializer(data, params), params).toCsv();
			break;
		case html:
			firstFunction = (m, d) -> d.toString();
			break;
		case json:
		case jsonCompressed:
		case jsonp:
			firstFunction = Serializer::toJson;
			if (format == jsonCompressed) {
				wrapperFunction = CompressionUtil::deflate;
			} else if (format == jsonp) {
				wrapperFunction = (output) -> defaultIfBlank(params.getCallback(), "parseResponse") + '(' + output + ");";
			} else if (params.testSanitizeJson()) {
				wrapperFunction = (output) -> JsonSanitizer.sanitize(output);
			}
			break;
		case smile:
			firstFunction = (mapper, data) -> CompressionUtil.bytesToBase64String(toSmile(mapper, data));
			break;
		case yaml:
			firstFunction = Serializer::toYaml;
			break;
		case xml:
			firstFunction = Serializer::toXml;
			break;
		default:
			throw new IllegalArgumentException(String.format("OutputFormat is not supported: format=%s", format));
		}
		final BiFunction<ObjectMapper, Object, String> resolvedFunc = firstFunction;
		final Function<String, String> resolvedWrapperFunc = wrapperFunction;
		BiFunction<ObjectMapper, Object, String> outputFunction = (mapper, data) -> {
			String output = (data instanceof String) && params.testSkipStrings() ? (String) data : resolvedFunc.apply(mapper, data);
			return resolvedWrapperFunc != null ? resolvedWrapperFunc.apply(output) : output;
		};
		return output(obj, params, exprContext, outputFunction);
	}

	/**
	 * To json.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @param exprContext the expr context
	 * @param outputFunction the output function
	 * @return the string
	 */
	public static String output(
		Object obj,
		OutputParams outputParams,
		@Nullable Map<String, Object> exprContext,
		BiFunction<ObjectMapper, Object, String> outputFunction
	) {
		Pair<ObjectMapper, Object> pair = getMapperAndExecuteExpressions(obj, outputParams, exprContext);
		return outputFunction.apply(pair.getLeft(), pair.getRight());
	}

	/**
	 * Output bytes.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @return the byte[]
	 * @throws JsonProcessingException the json processing exception
	 */
	public static byte[] outputBytes(final Object obj, final OutputParams params) throws JsonProcessingException {
		return outputBytes(obj, params, null);
	}

	/**
	 * Output bytes.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param exprContext the expr context
	 * @return the byte[]
	 * @throws JsonProcessingException the json processing exception
	 */
	public static byte[] outputBytes(
		Object obj,
		@Nonnull OutputParams params,
		@Nullable Map<String, Object> exprContext
	) throws JsonProcessingException {
		if (params.getOutputFormat() == null) {
			params.updateOutputFormatIfAllowed(OutputFormat.json);
		}
		Pair<ObjectMapper, Object> pair = getMapperAndExecuteExpressions(obj, params, exprContext);
		return pair.getLeft().writeValueAsBytes(pair.getRight());
	}

	/**
	 * Parses the boolean.
	 *
	 * @param text the text
	 * @return true, if successful
	 */
	public static Boolean parseBoolean(String text) {
		return parseBoolean(text, false);
	}

	/**
	 * Parses the boolean.
	 *
	 * @param text the text
	 * @param defaultValue the default value
	 * @return true, if successful
	 */
	public static Boolean parseBoolean(String text, Boolean defaultValue) {
		return Checks.defaultIfNull(BooleanUtils.toBooleanObject(text), defaultValue);
	}

	/**
	 * Parses the csv lines.
	 *
	 * @param options the options
	 * @param br the br
	 * @param parser the parser
	 * @param currentLine the current line
	 * @param lineConsumer the line consumer
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int parseCsvLines(FileParserOptions options, LineNumberReader br, CSVParser parser, String currentLine, ThrowingConsumer<String[]> lineConsumer) throws IOException {
		int count = 0;
		String[] pendingData = null;
		Pattern onlySepsRegex = Pattern.compile("^" + Pattern.quote(String.valueOf(parser.getSeparator())) + "+$");
		String line = currentLine == null ? br.readLine() : currentLine;
		while ((line != null) && (parser.isPending() || isNotBlank(line)) && ((options.getMaxLines() == null) || (count < options.getMaxLines()))) {
			if (line.startsWith("#") || (!options.testAllowBlanks() && onlySepsRegex.matcher(line).matches())) {
				line = br.readLine();
				continue;
			}
			count++;
			String[] data = parser.parseLineMulti(line);
			if (parser.isPending()) {
				if ((pendingData == null) || (pendingData.length == 0)) {
					pendingData = data;
				} else if ((data != null) && (data.length > 0)) {
					pendingData = ArrayUtils.addAll(pendingData, data);
				}
				line = br.readLine();
				continue;
			} else if (pendingData != null) {
				if ((pendingData.length > 0) && (data != null) && (data.length > 0)) {
					data = ArrayUtils.addAll(pendingData, data);
				}
				pendingData = null;
			}
			lineConsumer.acceptWithSneakyThrow(data);
			line = br.readLine();
		}
		return count;
	}

	/**
	 * Parses the string.
	 *
	 * @param <T> the generic type
	 * @param content the content
	 * @param valueType the value type
	 * @return the t
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parseString(String content, Class<T> valueType) {
		if (valueType == String.class) {
			return (T) content;
		}
		T val = null;
		if (content != null) {
			try {
				if (valueType.isEnum()) {
					val = (T) Enum.valueOf((Class<Enum>) valueType, content);
				} else {
					val = JSON_MAPPER.readValue(content, valueType);
				}
			} catch (Throwable e) {
				logError(LOG, e, "Unable to parse string [%s] to value of type [%s].", content, valueType);
			}
		}
		return val;
	}

	/**
	 * Parses the string as best fit.
	 *
	 * @param val the val
	 * @return the object
	 */
	public static Object parseStringAsBestFit(String val) {
		if (NumberUtils.isCreatable(val)) {
			return NumberUtils.toDouble(val);
		} else if (BooleanUtils.toBooleanObject(val) != null) {
			return BooleanUtils.toBoolean(val);
		}
		return val;
	}

	/**
	 * Read external.
	 *
	 * @param in the in
	 * @param obj the obj
	 */
	public static void readExternal(ObjectInput in, Object obj) {
		try {
			JSON_MAPPER.readerForUpdating(obj).readValue(in);
		} catch (Throwable e) {
			logError(LOG, e, "Issue reading object input using JSON: type=%s current=%s", obj == null ? null : obj.getClass().getName(), toJson(obj));
		}
	}

	/**
	 * Use reflection to read the object.
	 *
	 * @param in the object input
	 * @param obj the object
	 */
	public static void readExternalReflection(ObjectInput in, Externalizable obj) {
		Class<?> clazz = obj.getClass();
		PropertyDescriptor d = null;
		try {
			for (PropertyDescriptor current : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
				d = current;
				if ((d.getReadMethod() != null) && (d.getWriteMethod() != null)) {
					Reflections.invokeSafely(d.getWriteMethod(), obj, in.readObject());
				}
			}
		} catch (Throwable e) {
			logError(LOG, e, "Could not deserialize bean of class [%s]. Failed on descriptor [%s].", clazz.getName(), d == null ? null : d.getShortDescription());
		}
	}

	/**
	 * Read json.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T readJson(String json) {
		if (isBlank(json)) {
			return null;
		}
		try {
			return (T) JSON_MAPPER.readValue(json, Object.class);
		} catch (Throwable e) {
			logError(LOG, e, "Issue reading JSON string [%s] to object", json);
		}
		return null;
	}

	/**
	 * Read json as list.
	 *
	 * @param json the json
	 * @return the list
	 */
	public static List<Map<String, Object>> readJsonAsList(File json) {
		try {
			return readJsonAsListOfMaps(FileUtil.readFile(json), Object.class);
		} catch (Throwable e) {
			logError(LOG, e, "Issue reading file [%s] to JSON.", json);
		}
		return null;
	}

	/**
	 * Read json as list.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> readJsonAsList(File file, Class<T> type) {
		if ((file == null) || !file.exists()) {
			logWarn(LOG, "JSON file to convert to List did not exist.");
			return null;
		}
		try {
			return JSON_MAPPER.readValue(file, JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, type));
		} catch (Throwable e) {
			logError(LOG, e, "Error reading JSON as list for file [%s]!", file);
		}
		return null;
	}

	/**
	 * Read json as list.
	 *
	 * @param json the json
	 * @return the list
	 */
	public static List<Map<String, Object>> readJsonAsList(InputStream json) {
		return readJsonAsListOfMaps(json, Object.class);
	}

	/**
	 * Read json as list.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> readJsonAsList(InputStream json, Class<T> type) {
		if (json == null) {
			logWarn(LOG, "JSON to convert to List was blank.");
			return null;
		}
		try {
			return JSON_MAPPER.readValue(json, JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, type));
		} catch (Throwable e) {
			logError(LOG, e, "Error reading JSON as List!");
		}
		return null;
	}

	/**
	 * Read json as list.
	 *
	 * @param json the json
	 * @return the list
	 */
	public static List<Map<String, Object>> readJsonAsList(String json) {
		return readJsonAsListOfMaps(json, Object.class);
	}

	/**
	 * Read json as list.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> readJsonAsList(String json, Class<T> type) {
		return readJsonAsList(json, constructType(type));
	}

	/**
	 * Read json as list.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> readJsonAsList(String json, JavaType type) {
		if (isBlank(json)) {
			logWarn(LOG, "JSON to convert to List was blank.");
			return null;
		}
		try {
			return JSON_MAPPER.readValue(json, JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, type));
		} catch (Throwable e) {
			logError(LOG, e, "Error reading JSON as List!");
		}
		return null;
	}

	/**
	 * Read json as list of maps.
	 *
	 * @param <V> the value type
	 * @param json the json
	 * @param valueType the value type
	 * @return the list
	 */
	public static <V> List<Map<String, V>> readJsonAsListOfMaps(InputStream json, Class<V> valueType) {
		try {
			return JSON_MAPPER.readValue(
				json,
				JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, JSON_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, valueType))
			);
		} catch (Throwable e) {
			logError(LOG, e, "Error reading JSON as List!");
		}
		return null;
	}

	/**
	 * Read json as list of maps.
	 *
	 * @param <V> the value type
	 * @param json the json
	 * @param valueType the value type
	 * @return the list
	 */
	public static <V> List<Map<String, V>> readJsonAsListOfMaps(String json, Class<V> valueType) {
		if (isBlank(json)) {
			logWarn(LOG, "JSON to convert to List was blank.");
			return null;
		}
		try {
			return JSON_MAPPER.readValue(
				json,
				JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, JSON_MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, valueType))
			);
		} catch (Throwable e) {
			logError(LOG, e, "Error reading JSON as List!");
		}
		return null;
	}

	/**
	 * Read json as map.
	 *
	 * @param file the file
	 * @return the hash map
	 */
	public static Map<String, Object> readJsonAsMap(File file) {
		return fromJson(file, MAP_TYPE);
	}

	/**
	 * Read json as map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param file the file
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the hash map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> readJsonAsMap(File file, Class<K> keyType, Class<V> valueType) {
		return readJsonAsMap(file, LinkedHashMap.class, keyType, valueType);
	}

	/**
	 * Read json as map.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @return the hash map
	 */
	public static <T> Map<String, T> readJsonAsMap(File file, Class<T> type) {
		return readJsonAsMap(file, String.class, type);
	}

	/**
	 * Read json as map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the generic type
	 * @param file the file
	 * @param mapClass the map class
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the map
	 */
	public static <K, V, T extends Map<K, V>> T readJsonAsMap(File file, Class<T> mapClass, Class<K> keyType, Class<V> valueType) {
		return fromJson(file, JSON_MAPPER.getTypeFactory().constructMapType(mapClass, keyType, valueType));
	}

	/**
	 * Read json as map.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the map
	 */
	public static <T> Map<String, T> readJsonAsMap(File json, JavaType type) {
		return fromJson(json, getTypeFactory().constructMapType(LinkedHashMap.class, constructType(String.class), type));
	}

	/**
	 * Read json as map.
	 *
	 * @param stream the stream
	 * @return the hash map
	 */
	public static Map<String, Object> readJsonAsMap(InputStream stream) {
		return fromJson(stream, MAP_TYPE);
	}

	/**
	 * Read json as map.
	 *
	 * @param json the json
	 * @return the hash map
	 */
	public static Map<String, Object> readJsonAsMap(String json) {
		return fromJson(json, MAP_TYPE);
	}

	/**
	 * Read json as map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param json the json
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the hash map
	 */
	public static <K, V> Map<K, V> readJsonAsMap(String json, Class<K> keyType, Class<V> valueType) {
		return readJsonAsMap(json, LinkedHashMap.class, keyType, valueType);
	}

	/**
	 * Read json as map.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the hash map
	 */
	public static <T> Map<String, T> readJsonAsMap(String json, Class<T> type) {
		return readJsonAsMap(json, String.class, type);
	}

	/**
	 * Read json as map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param <T> the generic type
	 * @param json the json
	 * @param mapClass the map class
	 * @param keyType the key type
	 * @param valueType the value type
	 * @return the hash map
	 */
	@SuppressWarnings("rawtypes")
	public static <K, V, T extends Map> Map<K, V> readJsonAsMap(String json, Class<T> mapClass, Class<K> keyType, Class<V> valueType) {
		return fromJson(json, JSON_MAPPER.getTypeFactory().constructMapType(mapClass, keyType, valueType));
	}

	/**
	 * Read json as map.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param type the type
	 * @return the map
	 */
	public static <T> Map<String, T> readJsonAsMap(String json, JavaType type) {
		return fromJson(json, constructMapType(LinkedHashMap.class, constructType(String.class), type));
	}

	/**
	 * Read json as ordered map.
	 *
	 * @param json the json
	 * @return the tree map
	 */
	public static TreeMap<String, Object> readJsonAsOrderedMap(String json) {
		return (TreeMap<String, Object>) readJsonAsMap(json, TreeMap.class, String.class, Object.class);
	}

	/**
	 * Read json as tree.
	 *
	 * @param data the data
	 * @return the object node
	 */
	public static ObjectNode readJsonAsTree(File data) {
		return fromJson(data, ObjectNode.class);
	}

	/**
	 * Read json as tree.
	 *
	 * @param data the data
	 * @return the object node
	 */
	public static ObjectNode readJsonAsTree(String data) {
		return fromJson(data, ObjectNode.class);
	}

	/**
	 * Read stream.
	 *
	 * @param stream the stream
	 * @return the string
	 */
	public static String readStream(InputStream stream) {
		try {
			return stream == null ? null : IOUtils.toString(stream, UTF_8);
		} catch (Throwable e) {
			logError(LOG, e, "Issue reading stream [%s] to string.", stream);
		}
		return null;
	}

	/**
	 * Read value.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param data the data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T readValue(@Nonnull ObjectMapper mapper, byte[] data, @Nonnull JavaType type) {
		T object = null;
		try {
			object = data == null ? null : mapper.readValue(data, type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read data bytes to object: class=%s", type);
		}
		return finalizeObject(object);
	}

	/**
	 * Read value.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 */
	public static <T> T readValue(ObjectMapper mapper, InputStream stream, JavaType type) {
		T object = null;
		try (InputStream tmp = stream) {
			object = stream == null ? null : mapper.readValue(stream, type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read data stream to object: class=%s", type);
			logDebug(LOG, "Data string that could not be read is:\n%s.", stream == null ? null : readStream(stream));
		}
		return finalizeObject(object);
	}

	/**
	 * Read value.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param data the data
	 * @param type the type
	 * @return the t
	 */
	public static <T> T readValue(@Nonnull ObjectMapper mapper, String data, @Nonnull JavaType type) {
		T object = null;
		try {
			object = data == null ? null : mapper.readValue(data, type);
		} catch (Throwable e) {
			logError(LOG, e, "Could not read data string to object: class=%s", type);
			logDebug(LOG, "Data string that could not be read is:\n%s", data);
		}
		return finalizeObject(object);
	}

	/**
	 * Read value.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param format the format
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readValue(String data, OutputFormat format) {
		if ((data == null) || (format == null)) {
			return null;
		}
		switch (format) {
		case csv:
			return (T) Serializer.fromCsv(data);
		case json:
			return Serializer.fromJsonUnknown(data);
		case xml:
			return Serializer.fromXml(data);
		case yaml:
			return Serializer.fromYamlUnknown(data);
		default:
			throw new IllegalArgumentException(String.format("Could not parse data from [%s] format!", format));
		}
	}

	/**
	 * Read value with exception.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param stream the stream
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T readValueWithException(ObjectMapper mapper, InputStream stream, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		T object = null;
		try (InputStream tmp = stream) {
			object = stream == null ? null : mapper.readValue(stream, type);
		}
		return finalizeObject(object);
	}

	/**
	 * Read value with exception.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param data the data
	 * @param type the type
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T readValueWithException(ObjectMapper mapper, String data, JavaType type) throws JsonParseException, JsonMappingException, IOException {
		T object = mapper.readValue(data, type);
		return finalizeObject(object);
	}

	/**
	 * Read xml as map.
	 *
	 * @param xml the xml
	 * @return the map
	 */
	public static Map<String, Object> readXmlAsMap(String xml) {
		if (isBlank(xml)) {
			logWarn(LOG, "XML to convert to Map was blank.");
			return null;
		}
		try {
			return XML_MAPPER.readValue(xml, XML_MAPPER.getTypeFactory().constructMapType(TreeMap.class, String.class, Object.class));
		} catch (Throwable e) {
			logError(LOG, e, "Error reading XML as map!");
		}
		return null;
	}

	/**
	 * Removes the commented values.
	 *
	 * @param values the values
	 */
	public static void removeCommentedValues(List<String> values) {
		if ((values != null) && !values.isEmpty()) {
			List<Integer> removePositions = new ArrayList<>();
			int removedCount = 0;
			for (int pos = 0; pos < values.size(); pos++) {
				if (values.get(pos).startsWith("#")) {
					removePositions.add(pos - removedCount);
					removedCount++;
				}
			}
			if (!removePositions.isEmpty()) {
				for (Integer pos : removePositions) {
					values.remove(pos.intValue());
				}
			}
		}
	}

	/**
	 * Removes the commented values.
	 *
	 * @param <T> the generic type
	 * @param values the values
	 * @param retrieveString the retrieve string
	 */
	public static <T> void removeCommentedValues(List<T> values, Function<T, String> retrieveString) {
		if ((values != null) && !values.isEmpty()) {
			List<Integer> removePositions = new ArrayList<>();
			int removedCount = 0;
			for (int pos = 0; pos < values.size(); pos++) {
				if (StringUtils.defaultString(retrieveString.apply(values.get(pos))).startsWith("#")) {
					removePositions.add(pos - removedCount);
					removedCount++;
				}
			}
			if (!removePositions.isEmpty()) {
				for (Integer pos : removePositions) {
					values.remove(pos.intValue());
				}
			}
		}
	}

	/**
	 * Removes the field values.
	 *
	 * @param <T> the generic type
	 * @param src the src
	 * @param fields the fields
	 * @return the t
	 */
	public static <T> T removeFieldValues(T src, Set<String> fields) {
		if ((src != null) && isNotEmpty(fields)) {
			Object[] nullVals = { null };
			Stream.of(BeanUtils.getPropertyDescriptors(src.getClass())).filter(pd -> (pd.getWriteMethod() != null) && fields.contains(pd.getName())).forEach(
				pd -> {
					try {
						pd.getWriteMethod().invoke(src, nullVals);
					} catch (Throwable e) {
						Logs.logError(LOG, e, "Could not invoke write method with a null value for field [%s] on object [%s]", pd.getName(), src);
					}
				}
			);
		}
		return src;
	}

	/**
	 * Sets the log missing properties.
	 *
	 * @param logMissingProperties the logMissingProperties to set
	 */
	public static void setLogMissingProperties(boolean logMissingProperties) {
		Serializer.logMissingProperties = logMissingProperties;
	}

	/**
	 * Split csv as list and parse elements.
	 *
	 * @param <T> the generic type
	 * @param csv the csv
	 * @param type the type
	 * @return the list
	 */
	public static <T> List<T> splitCsvAsListAndParseElements(final String csv, @Nonnull Class<T> type) {
		return splitCsvAsListAndParseElements(csv, constructType(type));
	}

	/**
	 * Split csv as list and parse elements.
	 *
	 * @param <T> the generic type
	 * @param csv the csv
	 * @param type the type
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> splitCsvAsListAndParseElements(final String csv, @Nonnull JavaType type) {
		if (type.getRawClass() == String.class) {
			return (List<T>) Collect.splitCsvAsList(csv);
		}
		if (Checks.isBlank(csv)) {
			return new ArrayList<>();
		}
		return (List<T>) Collect.splitCsvAsList(csv).stream().map(val -> fromJson(val, type)).collect(Collectors.toList());
	}

	/**
	 * To bytes.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the byte[]
	 */
	public static byte[] toBytes(ObjectMapper mapper, Object obj, Set<String> ignoreFields) {
		byte[] data = null;
		try {
			if (isNotEmpty(ignoreFields)) {
				mapper = SerializerFactory.addOneTimeSkipFields(mapper.copy(), ignoreFields);
			}
			data = mapper.writeValueAsBytes(obj);
		} catch (SerializationException e) {
			throw e;
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object of class [%s] as JSON.", obj == null ? null : obj.getClass());
		}
		return data;
	}

	/**
	 * To csv.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toCsv(Object obj) {
		return obj == null ? null : new CsvSerializer(obj).toCsv();
	}

	/**
	 * To csv.
	 *
	 * @param obj the obj
	 * @param schema the schema
	 * @return the string
	 */
	public static String toCsv(Object obj, CsvSchema schema) {
		String csv = StringUtils.EMPTY;
		Collection<?> list = obj == null ? null
			: Collection.class.isAssignableFrom(obj.getClass()) ? (Collection<?>) obj : Map.class.isAssignableFrom(obj.getClass()) ? ((Map<?, ?>) obj).values() : Arrays.asList(obj);
		if (CollectionUtils.isNotEmpty(list)) {
			try {
				csv = CSV_MAPPER.writer(schema).writeValueAsString(list);
			} catch (Throwable e) {
				logError(LOG, e, "Error writing object as CSV.");
			}
		}
		return csv;
	}

	/**
	 * To csv.
	 *
	 * @param orig the orig
	 * @param outputParams the output params
	 * @return the string
	 */
	public static String toCsv(Object orig, OutputParams outputParams) {
		try {
			CsvSerializer csv = getCsvSerializer(orig, outputParams);
			return csv.toCsv();
		} catch (Throwable e) {
			logError(LOG, e, "Issue writing object of class [%s] as CSV.", orig == null ? null : orig.getClass());
		}
		return null;
	}

	/**
	 * To csv.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static String toCsv(ObjectMapper mapper, Object obj) {
		return obj == null ? null : new CsvSerializer(obj).setObjectMapper(mapper).toCsv();
	}

	/**
	 * To html table.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toHtmlTable(Object obj) {
		return toHtmlTable(obj, null);
	}

	/**
	 * To html table.
	 *
	 * @param obj the obj
	 * @param headersOrder the headers order
	 * @return the string
	 */
	public static String toHtmlTable(Object obj, List<String> headersOrder) {
		return toHtmlTable(obj, headersOrder, true, null, null, null, null, null);
	}

	/**
	 * To html table.
	 *
	 * @param obj the obj
	 * @param headersOrder the headers order
	 * @param asHtmlEmail the as html email
	 * @param tableClass the table class
	 * @param headerRowClass the header row class
	 * @param headerClass the header class
	 * @param rowClass the row class
	 * @param cellClass the cell class
	 * @return the string
	 */
	public static String toHtmlTable(
		Object obj,
		List<String> headersOrder,
		boolean asHtmlEmail,
		String tableClass,
		String headerRowClass,
		String headerClass,
		String rowClass,
		String cellClass
	) {
		if (obj != null) {
			CsvSerializer csv = new CsvSerializer(obj, true);
			csv.setAsHtmlEmail(true);
			csv.setHeadersOrder(headersOrder);
			csv.setHtmlTableClass(tableClass);
			csv.setHtmlRowClass(rowClass);
			csv.setHtmlHeaderRowClass(headerRowClass);
			csv.setHtmlHeaderClass(headerClass);
			csv.setHtmlCellClass(cellClass);
			return csv.toCsv();
		}
		return null;
	}

	/**
	 * To json.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toJson(Object obj) {
		return toJson(obj, true);
	}

	/**
	 * To json.
	 *
	 * @param obj the obj
	 * @param includeType the include type
	 * @return the string
	 */
	public static String toJson(Object obj, boolean includeType) {
		return toJson(includeType ? JSON_MAPPER : JSON_OUTPUT_MAPPER, obj);
	}

	/**
	 * To json.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the string
	 */
	public static String toJson(Object obj, OutputParams outputParams) {
		return output(obj, outputParams.updateOutputFormatIfAllowed(OutputFormat.json));
	}

	/**
	 * To json.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static String toJson(ObjectMapper mapper, Object obj) {
		return toJson(mapper, obj, null);
	}

	/**
	 * To json.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the string
	 */
	public static String toJson(ObjectMapper mapper, Object obj, Set<String> ignoreFields) {
		String json = StringUtils.EMPTY;
		try {
			if (isNotEmpty(ignoreFields)) {
				mapper = SerializerFactory.addOneTimeSkipFields(mapper.copy(), ignoreFields);
			}
			json = mapper.writeValueAsString(obj);
		} catch (SerializationException e) {
			throw e;
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object of class [%s] as JSON.", obj == null ? null : obj.getClass());
		}
		return json;
	}

	/**
	 * To json for html.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toJsonForHtml(Object obj) {
		return WebRequests.sanitizeJavaScriptForHtml(toJson(obj));
	}

	/**
	 * To json schema.
	 *
	 * @param type the type
	 * @return the json schema
	 */
	public static SchemaWithDefinitions toJsonSchema(Class<?> type) {
		try {
			DefinitionsSchemaFactory visitor = new DefinitionsSchemaFactory();
			JSON_MAPPER.acceptJsonFormatVisitor(JSON_MAPPER.constructType(type), visitor);
			return new SchemaWithDefinitions((ObjectSchema) visitor.finalSchema(), visitor.getDefinitions());
			// return (ObjectSchema) JSON_MAPPER_SCHEMA_GEN.generateSchema(type);
		} catch (JsonMappingException e) {
			logError(LOG, e, "Issue generating JSON schema for type [%s]", type);
			return null;
		}
	}

	/**
	 * To log string.
	 *
	 * @param val the val
	 * @return the string
	 */
	public static String toLogString(Object val) {
		return val == null ? null : ToStringBuilder.reflectionToString(val, CommonConstants.TO_STRING_STYLE_WITHOUT_NULL);
	}

	/**
	 * To map.
	 *
	 * @param obj the obj
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object obj) {
		return toMap(obj, true);
	}

	/**
	 * To map.
	 *
	 * @param obj the obj
	 * @param includeType the include type
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object obj, boolean includeType) {
		return toMap(obj, includeType, null);
	}

	/**
	 * To map.
	 *
	 * @param obj the obj
	 * @param includeType the include type
	 * @param ignoreFields the ignore fields
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object obj, boolean includeType, Set<String> ignoreFields) {
		ObjectMapper mapper = includeType ? JSON_MAPPER : JSON_OUTPUT_MAPPER;
		return toMap(mapper, obj, HashMap.class, ignoreFields);
	}

	/**
	 * To map.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object obj, OutputParams params) {
		return toMap(SerializerFactory.resolveObjectMapper(params), obj, HashMap.class, null);
	}

	/**
	 * To map.
	 *
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object obj, Set<String> ignoreFields) {
		return toMap(obj, true, ignoreFields);
	}

	/**
	 * To map.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param mapType the map type
	 * @return the map
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends Map> Map<String, Object> toMap(ObjectMapper mapper, Object obj, Class<T> mapType) {
		return toMap(mapper, obj, mapType, null);
	}

	/**
	 * To map.
	 *
	 * @param <T> the generic type
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param mapType the map type
	 * @param ignoreFields the ignore fields
	 * @return the map
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends Map> Map<String, Object> toMap(ObjectMapper mapper, Object obj, Class<T> mapType, Set<String> ignoreFields) {
		try {
			if (isNotEmpty(ignoreFields)) {
				mapper = SerializerFactory.addOneTimeSkipFields(mapper.copy(), ignoreFields);
			}
			return mapper.convertValue(obj, mapper.getTypeFactory().constructMapType(mapType, String.class, Object.class));
		} catch (Throwable e) {
			logError(LOG, e, "Error converting object to map.");
			if (LOG.isTraceEnabled()) {
				logTrace(LOG, "Object to convert to map: %s", toJson(obj));
			}
		}
		return null;
	}

	/**
	 * To map only simple fields.
	 *
	 * @param obj the obj
	 * @return the map
	 */
	public static Map<String, Object> toMapOnlySimpleFields(Object obj) {
		return toMapOnlySimpleFields(obj, Sets.newHashSet(), 1, 10);
	}

	/**
	 * To map only simple fields.
	 *
	 * @param obj the obj
	 * @param foundHashCodes the found hash codes
	 * @param currentLevel the current level
	 * @param maxLevels the max levels
	 * @return the map
	 */
	public static Map<String, Object> toMapOnlySimpleFields(Object obj, Set<Integer> foundHashCodes, int currentLevel, int maxLevels) {
		if (obj == null) {
			return null;
		}
		try {
			Map<String, Object> map = Maps.newTreeMap();
			BeanMap objectMap = new BeanMap(obj);
			String objClass = obj.getClass().getName();
			String parentPackage = substringBefore(objClass, ".") + '.' + substringBefore(substringAfter(objClass, "."), ".");
			if (isNotEmpty(objectMap)) {
				Iterator<String> allFieldNames = objectMap.keyIterator();
				while (allFieldNames.hasNext()) {
					String fieldName = allFieldNames.next();
					Object fieldVal = objectMap.get(fieldName);
					if (fieldVal == null) {
						continue;
					}
					Class<?> fieldType = fieldVal.getClass();
					if (fieldType == Class.class) {
						map.put(fieldName, fieldVal);
						continue;
					}
					Integer hashCode = System.identityHashCode(fieldVal);
					if (foundHashCodes.contains(hashCode)) {
						continue;
					}
					foundHashCodes.add(hashCode);
					if (SIMPLE_FIELD_CLASSES.stream().anyMatch(cl -> cl.isAssignableFrom(fieldType))) {
						map.put(fieldName, fieldVal);
					} else if (Map.class.isAssignableFrom(fieldType)) {
						Map<?, ?> fieldValMap = (Map<?, ?>) fieldVal;
						Class<?> keyType = isNotEmpty(fieldValMap) ? fieldValMap.keySet().iterator().next().getClass() : null;
						Class<?> valueType = isNotEmpty(fieldValMap) ? fieldValMap.values().iterator().next().getClass() : null;
						if (isEmpty(fieldValMap)) {
							map.put(fieldName, fieldValMap);
						} else if ((keyType != null) && (valueType != null) && SIMPLE_FIELD_CLASSES.stream().anyMatch(cl -> cl.isAssignableFrom(keyType)) && SIMPLE_FIELD_CLASSES.stream().anyMatch(
							cl -> cl.isAssignableFrom(valueType)
						)) {
							map.put(fieldName, fieldVal);
						} else {
							map.put("SKIPPED@" + fieldName, fieldType);
						}
					} else if (Collection.class.isAssignableFrom(fieldType)) {
						Collection<?> fieldValList = (Collection<?>) fieldVal;
						Class<?> genericType = isNotEmpty(fieldValList) ? fieldValList.iterator().next().getClass() : null;
						if (isEmpty(fieldValList)) {
							map.put(fieldName, fieldValList);
						} else if ((genericType != null) && SIMPLE_FIELD_CLASSES.stream().anyMatch(cl -> cl.isAssignableFrom(genericType))) {
							map.put(fieldName, fieldVal);
						} else {
							map.put("SKIPPED@" + fieldName, fieldType);
						}
					} else if (fieldVal instanceof Enumeration) {
						Enumeration<?> fieldValEnum = (Enumeration<?>) fieldVal;
						List<?> fieldValList = Collections.list(fieldValEnum);
						Class<?> genericType = isNotEmpty(fieldValList) ? fieldValList.iterator().next().getClass() : null;
						if (isEmpty(fieldValList)) {
							map.put(fieldName, fieldValList);
						} else if ((genericType != null) && SIMPLE_FIELD_CLASSES.stream().anyMatch(cl -> cl.isAssignableFrom(genericType))) {
							map.put(fieldName, fieldValList);
						} else {
							map.put("SKIPPED@" + fieldName, fieldType);
						}
					} else if (fieldVal instanceof Object[]) {
						List<?> fieldValList = Lists.newArrayList((Object[]) fieldVal);
						Class<?> genericType = isNotEmpty(fieldValList) ? fieldValList.iterator().next().getClass() : null;
						if (isEmpty(fieldValList)) {
							map.put(fieldName, fieldValList);
						} else if ((genericType != null) && SIMPLE_FIELD_CLASSES.stream().anyMatch(cl -> cl.isAssignableFrom(genericType))) {
							map.put(fieldName, fieldValList);
						} else {
							map.put("SKIPPED@" + fieldName, fieldType);
						}
					} else if (fieldType.getName().startsWith(parentPackage) && !(currentLevel > maxLevels)) {
						Map<String, Object> fieldMap = toMapOnlySimpleFields(fieldVal, foundHashCodes, currentLevel++, maxLevels);
						if (fieldMap != null) {
							map.put(fieldName, fieldMap);
						}
					} else {
						map.put("SKIPPED@" + fieldName, fieldType);
					}
				}
			}
			return map;
		} catch (Throwable e) {
			logError(LOG, e, "Error converting object [%s] to map with only simple fields.", obj.toString());
		}
		return null;
	}

	/**
	 * To map.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the map
	 */
	public static Map<String, Object> toMapViaJson(ObjectMapper mapper, Object obj) {
		try {
			return readJsonAsMap(toJson(mapper, obj));
		} catch (Throwable e) {
			logError(LOG, e, "Error converting object to map.");
		}
		return null;
	}

	/**
	 * To ordered map.
	 *
	 * @param obj the obj
	 * @return the tree map
	 */
	public static TreeMap<String, Object> toOrderedMap(Object obj) {
		return (TreeMap<String, Object>) toMap(JSON_MAPPER, obj, TreeMap.class);
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @return the list
	 */
	public static List<String> toPathKeyList(Object obj) {
		return toPathKeyList(obj, new OutputParams());
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @return the list
	 */
	public static List<String> toPathKeyList(Object obj, @Nonnull OutputParams params) {
		return toPathKeyList(obj, params, true);
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param includeArrayPos the include array pos
	 * @return the list
	 */
	public static List<String> toPathKeyList(Object obj, @Nonnull OutputParams params, boolean includeArrayPos) {
		return toPathKeyList(obj, params, includeArrayPos, null);
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param includeArrayPos the include array pos
	 * @param includePredicate the include predicate
	 * @return the list
	 */
	public static List<String> toPathKeyList(Object obj, @Nonnull OutputParams params, boolean includeArrayPos, @Nullable BiPredicate<String, Object> includePredicate) {
		return toPathKeyList(obj, params, includeArrayPos, includePredicate, null);
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param includeArrayPos the include array pos
	 * @param includePredicate the include predicate
	 * @param valueMaxLength the value max length
	 * @return the list
	 */
	public static List<String> toPathKeyList(
		Object obj,
		@Nonnull OutputParams params,
		boolean includeArrayPos,
		@Nullable BiPredicate<String, Object> includePredicate,
		@Nullable Integer valueMaxLength
	) {
		return toPathKeyList(obj, params, includeArrayPos, includePredicate, valueMaxLength, null);
	}

	/**
	 * To path key list.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @param includeArrayPos the include array pos
	 * @param includePredicate the include predicate
	 * @param valueMaxLength the value max length
	 * @param keyBlackList the key black list
	 * @return the list
	 */
	public static List<String> toPathKeyList(
		Object obj,
		@Nonnull OutputParams params,
		boolean includeArrayPos,
		@Nullable BiPredicate<String, Object> includePredicate,
		@Nullable Integer valueMaxLength,
		@Nullable Set<Pattern> keyBlackList
	) {
		if (obj == null) {
			return null;
		}
		if (params.getJsonIncludeType() == null) {
			params.setJsonIncludeType(false);
		}
		JsonNode node = toTree(SerializerFactory.resolveObjectMapper(params), obj, null);
		if (node == null) {
			return null;
		}
		List<String> list = new ArrayList<>();
		BiConsumer<String, Object> consumer = (key, val) -> {
			if (((keyBlackList == null) || Checks.passesWhiteAndBlackListCheck(key, null, keyBlackList).getLeft()) && ((includePredicate == null) || includePredicate.test(key, val))) {
				String valStr = val == null ? null : val.toString();
				if ((valStr != null) && (valueMaxLength != null) && (valStr.length() > valueMaxLength)) {
					valStr = valStr.substring(0, valueMaxLength);
				}
				list.add(key + '=' + valStr);
			}
		};
		addJsonNodeToPathKeyConsumer(consumer, node, "", includeArrayPos);
		return list;
	}

	/**
	 * To path key map.
	 *
	 * @param obj the obj
	 * @return the map
	 */
	public static Map<String, Object> toPathKeyMap(Object obj) {
		return toPathKeyMap(obj, new OutputParams());
	}

	/**
	 * To path key map.
	 *
	 * @param obj the obj
	 * @param params the params
	 * @return the map
	 */
	public static Map<String, Object> toPathKeyMap(Object obj, @Nonnull OutputParams params) {
		if (obj == null) {
			return null;
		}
		if (params.getJsonIncludeType() == null) {
			params.setJsonIncludeType(false);
		}
		JsonNode node = toTree(SerializerFactory.resolveObjectMapper(params), obj, null);
		if (node == null) {
			return null;
		}
		Map<String, Object> map = new LinkedHashMap<>();
		addJsonNodeToPathKeyConsumer(map::put, node, "", true);
		return map;
	}

	/**
	 * To pretty json.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toPrettyJson(Object obj) {
		return toJson(PRETTY_WRITER, obj);
	}

	/**
	 * To pretty json except.
	 *
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the string
	 */
	public static String toPrettyJsonExcept(Object obj, Set<String> ignoreFields) {
		return toJson(PRETTY_WRITER, obj, ignoreFields);
	}

	/**
	 * To smile.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static byte[] toSmile(Object obj) {
		return toSmile(obj, new OutputParams().setOutputFormat(OutputFormat.smile));
	}

	/**
	 * To smile.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the string
	 */
	public static byte[] toSmile(Object obj, OutputParams outputParams) {
		Pair<ObjectMapper, Object> pair = getMapperAndExecuteExpressions(obj, outputParams, null);
		return toSmile(pair.getLeft(), pair.getRight());
	}

	/**
	 * To smile.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static byte[] toSmile(ObjectMapper mapper, Object obj) {
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (SerializationException e) {
			throw e;
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object as Smile: class=%s", obj == null ? null : obj.getClass());
		}
		return new byte[0];
	}

	/**
	 * To string using builder.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toStringUsingBuilder(Object obj) {
		return ToStringBuilder.reflectionToString(obj, CommonConstants.DEFAULT_TO_STRING_STYLE);
	}

	/**
	 * To json node.
	 *
	 * @param obj the obj
	 * @return the json node
	 */
	public static JsonNode toTree(Object obj) {
		return toTree(JSON_MAPPER, obj, null);
	}

	/**
	 * To tree.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the json node
	 */
	public static JsonNode toTree(ObjectMapper mapper, Object obj, Set<String> ignoreFields) {
		try {
			if (isNotEmpty(ignoreFields)) {
				mapper = SerializerFactory.addOneTimeSkipFields(mapper.copy(), ignoreFields);
			}
			return mapper.valueToTree(obj);
		} catch (SerializationException e) {
			throw e;
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object of class [%s] as JSON node.", obj == null ? null : obj.getClass());
		}
		return null;
	}

	/**
	 * To xlsx.
	 *
	 * @param obj the obj
	 * @return the byte[]
	 */
	public static byte[] toXlsx(Object obj) {
		return toXlsx(obj, new OutputParams().setOutputFormat(xlsx));
	}

	/**
	 * To xlsx.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the byte[]
	 */
	public static byte[] toXlsx(Object obj, OutputParams outputParams) {
		if (outputParams == null) {
			outputParams = new OutputParams().setOutputFormat(xlsx);
		} else if (outputParams.getOutputFormat() == null) {
			outputParams.setOutputFormat(xlsx);
		}
		CsvSerializer csv = getCsvSerializer(obj, outputParams);
		try (Workbook workbook = csv.toBook()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			return baos.toByteArray();
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error writing object to XLSX: type=%s", obj == null ? null : obj.getClass());
		}
		return new byte[0];
	}

	/**
	 * To xml.
	 *
	 * @param <M> the generic type
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static <M extends ObjectMapper> String toXml(M mapper, Object obj) {
		String xml = StringUtils.EMPTY;
		try {
			xml = XML_MAPPER.writeValueAsString(obj);
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object as XML.");
		}
		return xml;
	}

	/**
	 * To xml.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toXml(Object obj) {
		return toXml(XML_MAPPER, obj);
	}

	/**
	 * To xml.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the string
	 */
	public static String toXml(Object obj, OutputParams outputParams) {
		return output(obj, outputParams.updateOutputFormatIfAllowed(OutputFormat.xml));
	}

	/**
	 * To xml using simple.
	 *
	 * @param source the source
	 * @param file the file
	 * @return the file
	 */
	public static File toXmlUsingSimple(Object source, File file) {
		try {
			XML_WRITE_SERIALIZER.write(source, file);
		} catch (Throwable e) {
			logError(LOG, e, "Serialization of object [%s] failed", source);
		}
		return file;
	}

	/**
	 * To yaml.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String toYaml(Object obj) {
		return toYaml(obj, true);
	}

	/**
	 * To yaml.
	 *
	 * @param obj the obj
	 * @param includeType the include type
	 * @return the string
	 */
	public static String toYaml(Object obj, boolean includeType) {
		return toYaml(includeType ? YAML_MAPPER : YAML_NO_TYPE_MAPPER, obj);
	}

	/**
	 * To yaml.
	 *
	 * @param obj the obj
	 * @param outputParams the output params
	 * @return the string
	 */
	public static String toYaml(Object obj, @Nonnull OutputParams outputParams) {
		return output(obj, outputParams.updateOutputFormatIfAllowed(OutputFormat.yaml));
	}

	/**
	 * To yaml.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static String toYaml(ObjectMapper mapper, Object obj) {
		return toYaml(mapper, obj, null);
	}

	/**
	 * To yaml.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @param ignoreFields the ignore fields
	 * @return the string
	 */
	public static String toYaml(ObjectMapper mapper, Object obj, Set<String> ignoreFields) {
		String yaml = StringUtils.EMPTY;
		try {
			if (isNotEmpty(ignoreFields)) {
				mapper = SerializerFactory.addOneTimeSkipFields(mapper.copy(), ignoreFields);
			}
			yaml = mapper.writeValueAsString(obj);
		} catch (SerializationException e) {
			throw e;
		} catch (Throwable e) {
			logError(LOG, e, "Error writing object of class [%s] as JSON.", obj == null ? null : obj.getClass());
		}
		return yaml;
	}

	/**
	 * Transpose csv.
	 *
	 * @param data the data
	 * @return the string
	 */
	public static String transposeCsv(String data) {
		List<Map<String, Object>> csv = Lists.newArrayList();
		int i = 0;
		List<String> lines = Collect.splitCsvAsList(data, '\n');
		CSVParser parser = Collect.getCsvParser(',');
		for (String line : lines) {
			List<String> lineData = Collect.splitCsvAsList(line, parser);
			String header = lineData.remove(0);
			int j = 0;
			for (String cell : lineData) {
				if (i == 0) {
					csv.add(Maps.newHashMap());
				}
				csv.get(j).put(header, cell);
				j++;
			}
			i++;
		}
		return toCsv(csv);
	}

	/**
	 * Write external.
	 *
	 * @param out the out
	 * @param obj the obj
	 */
	public static void writeExternal(ObjectOutput out, Object obj) {
		try {
			//			if ((obj instanceof DynamicDataHandler) || obj.getClass().getName().startsWith("com.github.lancethomps.lava")) {
			//				Map<String, Object> map = toMap(obj);
			//				if (map != null) {
			//					map.remove(TYPE_PROPERTY);
			//				}
			//				JSON_MAPPER.writeValue(out, map);
			//			} else {
			//				JSON_MAPPER.writeValue(out, obj);
			//			}
			JSON_MAPPER.writeValue(out, obj);
		} catch (Throwable e) {
			logError(LOG, e, "Issue writing object output using JSON: type=%s json=%s", obj == null ? null : obj.getClass().getName(), toJson(obj));
		}
	}

	/**
	 * Use reflection to write the object.
	 *
	 * @param out the object output
	 * @param obj the externalizable object
	 */
	public static void writeExternalReflection(ObjectOutput out, Externalizable obj) {
		Class<?> clazz = obj.getClass();
		PropertyDescriptor d = null;
		try {
			for (PropertyDescriptor current : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
				d = current;
				if ((d.getReadMethod() != null) && (d.getWriteMethod() != null)) {
					out.writeObject(Reflections.invokeSafely(d.getReadMethod(), obj));
				}
			}
		} catch (Throwable e) {
			logError(LOG, e, "Could not serialize bean of class [%s]. Failed on descriptor [%s].", clazz.getName(), d == null ? null : d.getShortDescription());
		}
	}

	/**
	 * Adds the json node to path key map.
	 *
	 * @param consumer the consumer
	 * @param node the node
	 * @param prefix the prefix
	 * @param includeArrayPos the include array pos
	 */
	private static void addJsonNodeToPathKeyConsumer(@Nonnull BiConsumer<String, Object> consumer, @Nonnull JsonNode node, @Nonnull String prefix, boolean includeArrayPos) {
		if (node.isNull()) {
			return;
		}
		if (node.isValueNode()) {
			consumer.accept(prefix, jsonNodeToObject(node));
		} else if (node.isArray()) {
			ArrayNode nodeArray = ((ArrayNode) node);
			for (int index = 0; index < nodeArray.size(); index++) {
				addJsonNodeToPathKeyConsumer(consumer, nodeArray.get(index), prefix + (includeArrayPos ? ("" + '[' + index + ']') : ""), includeArrayPos);
			}
		} else if (node.isObject()) {
			ObjectNode nodeObject = (ObjectNode) node;
			String fieldPrefix = Checks.isBlank(prefix) ? "" : (prefix + '.');
			nodeObject.fields().forEachRemaining(e -> addJsonNodeToPathKeyConsumer(consumer, e.getValue(), fieldPrefix + e.getKey(), includeArrayPos));
		}
	}
}
