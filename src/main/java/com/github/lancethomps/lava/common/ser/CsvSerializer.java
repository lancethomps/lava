package com.github.lancethomps.lava.common.ser;

import static com.github.lancethomps.lava.common.Checks.isEmpty;
import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.ser.OutputFormat.xls;
import static com.github.lancethomps.lava.common.ser.OutputFormat.xlsx;
import static com.github.lancethomps.lava.common.ser.Serializer.toMapViaJson;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.expression.spel.standard.SpelExpression;

import com.github.lancethomps.lava.common.Reflections;
import com.github.lancethomps.lava.common.collections.MapUtil;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.excel.ExcelFactory;
import com.github.lancethomps.lava.common.sorting.Sorting;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class CsvSerializer.
 */
public class CsvSerializer {

	/** The Constant DEFAULT_DELIMITER. */
	public static final String DEFAULT_DELIMITER = ",";

	/** The Constant DEFAULT_SKIP_PROPS. */
	public static final Set<String> DEFAULT_SKIP_PROPS = new HashSet<>(Arrays.asList("@type"));

	/** The Constant KEY_PREFIX_LIST_SHELL. */
	public static final String KEY_PREFIX_LIST_SHELL = "%s[%s]";

	/** The Constant TABLE. */
	public static final String TABLE = "<table class=\"%s\"><thead>%s</thead><tbody>%s</tbody></table>";

	/** The Constant TD. */
	public static final String TD = "<td class=\"%s\">%s</td>";

	/** The Constant HTML_TH_SHELL. */
	public static final String TH = "<th class=\"%s\">%s</th>";

	/** The Constant TR. */
	public static final String TR = "<tr class=\"%s\">%s</tr>";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(CsvSerializer.class);

	/** The Constant SIMPLE_OUTPUT_CLASSES. */
	private static final Set<String> SIMPLE_OUTPUT_CLASSES = Sets.newHashSet();

	/** The as flattened objects. */
	private boolean asFlattenedObjects;

	/** The as html. */
	private boolean asHtml;

	/** The as html email. */
	private boolean asHtmlEmail;

	/** The book. */
	private Workbook book;

	/** The chaining sep. */
	private String chainingSep = ".";

	/** The csv delimiter. */
	private String csvDelimiter = DEFAULT_DELIMITER;

	/** The data. */
	private List<Map<String, String>> data;

	/** The expand json fields. */
	private boolean expandJsonFields;

	/** The expand json fields inner. */
	private boolean expandJsonFieldsInner = true;

	/** The has skip patterns. */
	private boolean hasSkipPatterns;

	/** The headers. */
	private Set<String> headers;

	/** The headers order. */
	private List<String> headersOrder;

	/** The html cell class. */
	private String htmlCellClass = EMPTY;

	/** The html header class. */
	private String htmlHeaderClass = EMPTY;

	/** The html header row class. */
	private String htmlHeaderRowClass = EMPTY;

	/** The html row class. */
	private String htmlRowClass = EMPTY;

	/** The html table class. */
	private String htmlTableClass = EMPTY;

	/** The include properties. */
	private Set<String> includeProperties;

	/** The is workbook. */
	private boolean isWorkbook;

	/** The lines. */
	private List<StringBuilder> lines;

	/** The obj. */
	private Object obj;

	/** The object mapper. */
	private ObjectMapper objectMapper;

	/** The output format. */
	private OutputFormat outputFormat;

	/** The params. */
	private OutputParams params;

	/** The resolved skip rows expression. */
	private SpelExpression resolvedSkipRowsExpression;

	/** The sandbox. */
	private boolean sandbox = true;

	/** The sheet. */
	private Sheet sheet;

	/** The skip patterns. */
	private List<Pattern> skipPatterns;

	/** The skip properties. */
	private Set<String> skipProperties = new HashSet<>();

	/** The skip rows expression. */
	private String skipRowsExpression;

	/** The sort by. */
	private String sort;

	/** The sort as type. */
	private Class<?> sortAsType;

	/** The transpose data. */
	private boolean transposeData;

	/** The use chaining list sep. */
	private boolean useChainingListSep = true;

	/**
	 * Instantiates a new csv serializer.
	 *
	 * @param obj the obj
	 */
	public CsvSerializer(Object obj) {
		this(obj, false);
	}

	/**
	 * Instantiates a new csv serializer.
	 *
	 * @param obj the obj
	 * @param asHtml the as html
	 */
	public CsvSerializer(Object obj, boolean asHtml) {
		super();
		this.obj = obj;
		this.asHtml = asHtml;
	}

	/**
	 * Instantiates a new csv serializer.
	 *
	 * @param obj the obj
	 * @param params the params
	 */
	public CsvSerializer(Object obj, OutputParams params) {
		super();
		this.obj = obj;
		this.params = params;
	}

	/**
	 * Convert map to json map.
	 *
	 * @param map the map
	 * @return the map
	 */
	public static Map<String, Object> convertMapToJsonMap(Map<?, ?> map) {
		Map<String, Object> jsonMap = new HashMap<>();
		for (Entry<?, ?> ent : map.entrySet()) {
			jsonMap.put(simpleObjectToString(Serializer.JSON_OUTPUT_MAPPER, ent.getKey()), ent.getValue());
		}
		return jsonMap;
	}

	/**
	 * Checks if is simple output class.
	 *
	 * @param clazz the clazz
	 * @return true, if is simple output class
	 */
	public static boolean isSimpleOutputClass(Class<?> clazz) {
		return Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz) || Enum.class
			.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || (clazz == String.class) || SIMPLE_OUTPUT_CLASSES.contains(clazz.getSimpleName());
	}

	/**
	 * Simple object to string.
	 *
	 * @param mapper the mapper
	 * @param obj the obj
	 * @return the string
	 */
	public static String simpleObjectToString(ObjectMapper mapper, Object obj) {
		if (obj instanceof String) {
			return (String) obj;
		}
		return Serializer.toJson(mapper, obj);
	}

	/**
	 * Gets the book.
	 *
	 * @return the book
	 */
	public Workbook getBook() {
		return book;
	}

	/**
	 * Gets the chaining sep.
	 *
	 * @return the chainingSep
	 */
	public String getChainingSep() {
		return chainingSep;
	}

	/**
	 * @return the csvDelimiter
	 */
	public String getCsvDelimiter() {
		return csvDelimiter;
	}

	/**
	 * Gets the headers order.
	 *
	 * @return the headersOrder
	 */
	public List<String> getHeadersOrder() {
		return headersOrder;
	}

	/**
	 * Gets the html cell class.
	 *
	 * @return the htmlCellClass
	 */
	public String getHtmlCellClass() {
		return htmlCellClass;
	}

	/**
	 * Gets the html header class.
	 *
	 * @return the htmlHeaderClass
	 */
	public String getHtmlHeaderClass() {
		return htmlHeaderClass;
	}

	/**
	 * Gets the html header row class.
	 *
	 * @return the htmlHeaderRowClass
	 */
	public String getHtmlHeaderRowClass() {
		return htmlHeaderRowClass;
	}

	/**
	 * Gets the html row class.
	 *
	 * @return the htmlRowClass
	 */
	public String getHtmlRowClass() {
		return htmlRowClass;
	}

	/**
	 * Gets the html table class.
	 *
	 * @return the htmlTableClass
	 */
	public String getHtmlTableClass() {
		return htmlTableClass;
	}

	/**
	 * @return the includeProperties
	 */
	public Set<String> getIncludeProperties() {
		return includeProperties;
	}

	/**
	 * Gets the object mapper.
	 *
	 * @return the objectMapper
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * Gets the output format.
	 *
	 * @return the outputFormat
	 */
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @return the params
	 */
	public OutputParams getParams() {
		return params;
	}

	/**
	 * Gets the skip patterns.
	 *
	 * @return the skipPatterns
	 */
	public List<Pattern> getSkipPatterns() {
		return skipPatterns;
	}

	/**
	 * Gets the skip properties.
	 *
	 * @return the skipProperties
	 */
	public Set<String> getSkipProperties() {
		return skipProperties;
	}

	/**
	 * @return the skipRowsExpression
	 */
	public String getSkipRowsExpression() {
		return skipRowsExpression;
	}

	/**
	 * Gets the sort.
	 *
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * @return the sortAsType
	 */
	public Class<?> getSortAsType() {
		return sortAsType;
	}

	/**
	 * Checks if is as flattened objects.
	 *
	 * @return the asFlattenedObjects
	 */
	public boolean isAsFlattenedObjects() {
		return asFlattenedObjects;
	}

	/**
	 * Checks if is as html email.
	 *
	 * @return the asHtmlEmail
	 */
	public boolean isAsHtmlEmail() {
		return asHtmlEmail;
	}

	/**
	 * @return the expandJsonFields
	 */
	public boolean isExpandJsonFields() {
		return expandJsonFields;
	}

	/**
	 * @return the expandJsonFieldsInner
	 */
	public boolean isExpandJsonFieldsInner() {
		return expandJsonFieldsInner;
	}

	/**
	 * @return the sandbox
	 */
	public boolean isSandbox() {
		return sandbox;
	}

	/**
	 * Checks if is transpose data.
	 *
	 * @return the transposeData
	 */
	public boolean isTransposeData() {
		return transposeData;
	}

	/**
	 * Checks if is use chaining list sep.
	 *
	 * @return the useChainingListSep
	 */
	public boolean isUseChainingListSep() {
		return useChainingListSep;
	}

	/**
	 * Parses the.
	 */
	@SuppressWarnings("unchecked")
	public void parse() {
		checkInit();
		Logs.logTrace(LOG, "Parsing CSV...");
		data = Lists.newArrayList();
		headers = new HashSet<>();
		if ((params.getCsvParams() != null) && (params.getCsvParams().getAlwaysIncludeHeaders() != null)) {
			headers.addAll(params.getCsvParams().getAlwaysIncludeHeaders());
		}
		if (asHtml) {
			skipProperties.add("@type");
		}
		boolean skipMapConversion = (params.getCsvParams() != null) && (params.getCsvParams().getSkipMapConversion() != null) && params.getCsvParams().getSkipMapConversion();
		(Reflections.isListType(obj.getClass()) ? (Collection<Object>) obj : Lists.newArrayList(obj)).stream().filter(this::shouldProcessRow).forEach(bean -> {
			Map<String, String> beanData = new HashMap<>();
			Map<String, Object> map = Reflections.isMapType(bean.getClass()) ? (skipMapConversion ? (Map<String, Object>) bean : convertMapToJsonMap((Map<?, ?>) bean))
				: toMapViaJson(objectMapper, bean);
			if (isNotEmpty(includeProperties)) {
				Sets.newHashSet(map.keySet()).stream().filter(key -> !includeProperties.contains(key)).forEach(map::remove);
			}
			Lambdas.consumeIfTrue(expandJsonFields, map, m -> MapUtil.expandJsonFields(m, expandJsonFieldsInner, skipProperties));
			for (Entry<String, Object> ent : map.entrySet()) {
				if (!skipProperties.contains(ent.getKey())) {
					addData(beanData, ent.getKey(), ent.getKey(), ent.getValue());
				}
			}
			if (isNotEmpty(beanData)) {
				data.add(beanData);
			}
		});
		if (isNotBlank(sort)) {
			Sorting.sortListOfMaps(data, sort, true, (Class<? extends Comparable<?>>) sortAsType);
		}
	}

	/**
	 * Sets the as flattened objects.
	 *
	 * @param asFlattenedObjects the asFlattenedObjects to set
	 * @return the csv serializer
	 */
	public CsvSerializer setAsFlattenedObjects(boolean asFlattenedObjects) {
		this.asFlattenedObjects = asFlattenedObjects;
		return this;
	}

	/**
	 * Sets the as html email.
	 *
	 * @param asHtmlEmail the asHtmlEmail to set
	 * @return the csv serializer
	 */
	public CsvSerializer setAsHtmlEmail(boolean asHtmlEmail) {
		this.asHtmlEmail = asHtmlEmail;
		return this;
	}

	/**
	 * Sets the chaining sep.
	 *
	 * @param chainingSep the chainingSep to set
	 * @return the csv serializer
	 */
	public CsvSerializer setChainingSep(String chainingSep) {
		this.chainingSep = chainingSep;
		return this;
	}

	/**
	 * Sets the csv delimiter.
	 *
	 * @param csvDelimiter the csvDelimiter to set
	 * @return the csv serializer
	 */
	public CsvSerializer setCsvDelimiter(String csvDelimiter) {
		this.csvDelimiter = defaultIfEmpty(csvDelimiter, DEFAULT_DELIMITER);
		return this;
	}

	/**
	 * Sets the expand json fields.
	 *
	 * @param expandJsonFields the expandJsonFields to set
	 * @return the csv serializer
	 */
	public CsvSerializer setExpandJsonFields(boolean expandJsonFields) {
		this.expandJsonFields = expandJsonFields;
		return this;
	}

	/**
	 * Sets the expand json fields inner.
	 *
	 * @param expandJsonFieldsInner the expandJsonFieldsInner to set
	 * @return the csv serializer
	 */
	public CsvSerializer setExpandJsonFieldsInner(boolean expandJsonFieldsInner) {
		this.expandJsonFieldsInner = expandJsonFieldsInner;
		return this;
	}

	/**
	 * Sets the headers order.
	 *
	 * @param headersOrder the headersOrder to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHeadersOrder(List<String> headersOrder) {
		this.headersOrder = headersOrder;
		return this;
	}

	/**
	 * Sets the html cell class.
	 *
	 * @param htmlCellClass the htmlCellClass to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHtmlCellClass(String htmlCellClass) {
		if (htmlCellClass != null) {
			this.htmlCellClass = htmlCellClass;
		}
		return this;
	}

	/**
	 * Sets the html header class.
	 *
	 * @param htmlHeaderClass the htmlHeaderClass to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHtmlHeaderClass(String htmlHeaderClass) {
		if (htmlHeaderClass != null) {
			this.htmlHeaderClass = htmlHeaderClass;
		}
		return this;
	}

	/**
	 * Sets the html header row class.
	 *
	 * @param htmlHeaderRowClass the htmlHeaderRowClass to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHtmlHeaderRowClass(String htmlHeaderRowClass) {
		if (htmlHeaderRowClass != null) {
			this.htmlHeaderRowClass = htmlHeaderRowClass;
		}
		return this;
	}

	/**
	 * Sets the html row class.
	 *
	 * @param htmlRowClass the htmlRowClass to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHtmlRowClass(String htmlRowClass) {
		if (htmlRowClass != null) {
			this.htmlRowClass = htmlRowClass;
		}
		return this;
	}

	/**
	 * Sets the html table class.
	 *
	 * @param htmlTableClass the htmlTableClass to set
	 * @return the csv serializer
	 */
	public CsvSerializer setHtmlTableClass(String htmlTableClass) {
		if (htmlTableClass != null) {
			this.htmlTableClass = htmlTableClass;
		}
		return this;
	}

	/**
	 * Sets the include properties.
	 *
	 * @param includeProperties the includeProperties to set
	 * @return the csv serializer
	 */
	public CsvSerializer setIncludeProperties(Set<String> includeProperties) {
		this.includeProperties = includeProperties;
		return this;
	}

	/**
	 * Sets the object mapper.
	 *
	 * @param objectMapper the objectMapper to set
	 * @return the csv serializer
	 */
	public CsvSerializer setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		return this;
	}

	/**
	 * Sets the output format.
	 *
	 * @param outputFormat the outputFormat to set
	 * @return the csv serializer
	 */
	public CsvSerializer setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * Sets the params.
	 *
	 * @param params the params to set
	 * @return the csv serializer
	 */
	public CsvSerializer setParams(OutputParams params) {
		this.params = params;
		return this;
	}

	/**
	 * Sets the sandbox.
	 *
	 * @param sandbox the sandbox to set
	 * @return the csv serializer
	 */
	public CsvSerializer setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
		return this;
	}

	/**
	 * Sets the skip patterns.
	 *
	 * @param skipPatterns the skipPatterns to set
	 * @return the csv serializer
	 */
	public CsvSerializer setSkipPatterns(List<Pattern> skipPatterns) {
		this.skipPatterns = skipPatterns;
		return this;
	}

	/**
	 * Sets the skip properties.
	 *
	 * @param skipProperties the skipProperties to set
	 * @return the csv serializer
	 */
	public CsvSerializer setSkipProperties(Set<String> skipProperties) {
		this.skipProperties = skipProperties;
		return this;
	}

	/**
	 * Sets the skip rows expression.
	 *
	 * @param skipRowsExpression the skipRowsExpression to set
	 * @return the csv serializer
	 */
	public CsvSerializer setSkipRowsExpression(String skipRowsExpression) {
		this.skipRowsExpression = skipRowsExpression;
		return this;
	}

	/**
	 * Sets the sort.
	 *
	 * @param sort the sort
	 * @return the csv serializer
	 */
	public CsvSerializer setSort(String sort) {
		this.sort = sort;
		return this;
	}

	/**
	 * Sets the sort as type.
	 *
	 * @param sortAsType the sortAsType to set
	 * @return the csv serializer
	 */
	public CsvSerializer setSortAsType(Class<?> sortAsType) {
		this.sortAsType = sortAsType;
		return this;
	}

	/**
	 * Sets the transpose data.
	 *
	 * @param transposeData the transposeData to set
	 * @return the csv serializer
	 */
	public CsvSerializer setTransposeData(boolean transposeData) {
		this.transposeData = transposeData;
		return this;
	}

	/**
	 * Sets the use chaining list sep.
	 *
	 * @param useChainingListSep the useChainingListSep to set
	 * @return the csv serializer
	 */
	public CsvSerializer setUseChainingListSep(boolean useChainingListSep) {
		this.useChainingListSep = useChainingListSep;
		return this;
	}

	/**
	 * To book.
	 *
	 * @return the workbook
	 */
	public Workbook toBook() {
		toCsv();
		return getBook();
	}

	/**
	 * To csv.
	 *
	 * @return the string
	 */
	public String toCsv() {
		checkInit();
		if ((obj != null) && ((headers == null) || (data == null))) {
			parse();
		}
		if (asFlattenedObjects) {
			Map<String, Object> flattened = Serializer.createJsonMap();
			if (transposeData) {
				transpose();
			}
			flattened.put("headers", transposeData ? headers : getSortedHeaders());
			flattened.put("data", data);
			return Serializer.toJson(flattened);
		}
		String thead = null;
		String tbody = null;
		List<String> sortedHeaders = getSortedHeaders();
		lines = Lists.newArrayList();
		if (asHtml) {
			thead = format(TR, htmlHeaderRowClass, sortedHeaders.stream().map(h -> format(asHtmlEmail ? TD : TH, htmlHeaderClass, h)).reduce(EMPTY, String::concat));
			tbody = EMPTY;
		} else if (transposeData || isWorkbook) {
			for (int headerNum = 0; headerNum < sortedHeaders.size(); headerNum++) {
				addHeader(sortedHeaders.get(headerNum), headerNum);
			}
		} else {
			lines.add(new StringBuilder(join(sortedHeaders, csvDelimiter)));
		}
		int rowNum = 1;
		for (Map<String, String> dataMap : data) {
			List<String> rowData = Lists.newArrayList();
			for (String header : sortedHeaders) {
				String cell = dataMap.get(header);
				if (cell == null) {
					cell = asHtml ? "&nbsp;" : "";
				} else if (asHtml) {
					cell = StringEscapeUtils.escapeXml11(cell);
				} else if (!cell.startsWith("=")) {
					cell = escapeCsv(cell);
				}
				rowData.add(cell);
			}
			if (asHtml) {
				tbody += format(TR, htmlRowClass, rowData.stream().map(h -> format(TD, htmlCellClass, h)).reduce(EMPTY, String::concat)) + System.lineSeparator();
			} else if (transposeData || isWorkbook) {
				for (int cellNum = 0; cellNum < rowData.size(); cellNum++) {
					addCellData(rowData.get(cellNum), rowNum, cellNum);
				}
			} else {
				lines.add(new StringBuilder(join(rowData, csvDelimiter)));
			}
			rowNum++;
		}
		StringBuilder csv = new StringBuilder();
		if (asHtml) {
			if (asHtmlEmail) {
				tbody = thead + tbody;
				thead = EMPTY;
			}
			String table = format(TABLE, htmlTableClass, thead, tbody);
			if (asHtmlEmail) {
				table = StringUtils.remove(table, "<thead></thead>");
			}
			csv.append(table);
		} else if (!isWorkbook) {
			lines.forEach(line -> {
				csv.append(line.toString()).append(System.lineSeparator());
			});
		}
		return csv.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if (book != null) {
			try {
				book.close();
			} catch (Throwable e) {
				;
			}
		}
		super.finalize();
	}

	/**
	 * Adds the cell data.
	 *
	 * @param cell the cell
	 * @param rowNum the row num
	 * @param cellNum the cell num
	 */
	private void addCellData(String cell, int rowNum, int cellNum) {
		if (transposeData) {
			if (isWorkbook) {
				ExcelFactory.createCell(sheet.getRow(cellNum), rowNum, cell);
			} else {
				lines.get(cellNum).append(csvDelimiter).append(cell);
			}
		} else if (isWorkbook) {
			Row row = cellNum == 0 ? sheet.createRow(rowNum) : sheet.getRow(rowNum);
			ExcelFactory.createCell(row, cellNum, cell);
		}
	}

	/**
	 * Adds the data.
	 *
	 * @param data the data
	 * @param fieldName the field name
	 * @param fullKey the key prefix
	 * @param bean the bean
	 */
	private void addData(Map<String, String> data, String fieldName, String fullKey, Object bean) {
		if (bean == null) {
			return;
		}
		Class<?> rootClass = bean.getClass();
		try {
			if (isSimpleOutputClass(rootClass)) {
				if (hasSkipPatterns && skipPatterns.stream().anyMatch(p -> p.matcher(fullKey).matches())) {
					return;
				}
				headers.add(fullKey);
				String val = simpleObjectToString(objectMapper, bean);
				data.put(
					fullKey,
					isNotEmpty(params.getCsvWrapFieldsInFunction()) && params.getCsvWrapFieldsInFunction().contains(fieldName) && StringUtil.isNumeric(val) ? "=\"" + val + '"' : val
				);
			} else if (Reflections.isListType(rootClass)) {
				List<?> innerBeanList = (List<?>) bean;
				for (int i = 0; i < innerBeanList.size(); i++) {
					String newFullKey = useChainingListSep ? String.format(KEY_PREFIX_LIST_SHELL, fullKey, i) : fullKey + i;
					addData(data, fieldName, newFullKey, innerBeanList.get(i));
				}
			} else {
				Map<String, Object> map = Reflections.isMapType(rootClass) ? convertMapToJsonMap((Map<?, ?>) bean) : Serializer.toMapViaJson(objectMapper, bean);
				for (Entry<String, Object> ent : map.entrySet()) {
					if (!skipProperties.contains(ent.getKey())) {
						String innerFullKey = isBlank(chainingSep) ? fullKey + StringUtils.capitalize(ent.getKey()) : fullKey + chainingSep + ent.getKey();
						addData(data, ent.getKey(), innerFullKey, ent.getValue());
					}
				}
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue getting timer logs for class [%s]!", rootClass);
		}
	}

	/**
	 * Adds the header.
	 *
	 * @param header the header
	 * @param headerNum the header num
	 */
	private void addHeader(String header, int headerNum) {
		if (transposeData) {
			if (isWorkbook) {
				Row row = sheet.createRow(headerNum);
				ExcelFactory.createCell(row, 0, header);
			} else {
				lines.add(new StringBuilder(header));
			}
		} else if (isWorkbook) {
			if (headerNum == 0) {
				sheet.createRow(0);
			}
			ExcelFactory.createCell(sheet.getRow(0), headerNum, header);
		}
	}

	/**
	 * Check mapper.
	 */
	private void checkInit() {
		if (params == null) {
			params = new OutputParams();
		}
		if (objectMapper == null) {
			objectMapper = SerializerFactory.resolveObjectMapper(params);
			if (objectMapper == null) {
				objectMapper = SerializerFactory.resolveObjectMapper(new OutputParams().setOutputFormat(OutputFormat.json));
			}
		}
		if (isNotEmpty(skipProperties) && isEmpty(skipPatterns)) {
			skipPatterns = skipProperties.stream().filter(p -> p.startsWith("@REGEX@")).map(p -> Pattern.compile(removeStart(p, "@REGEX@"))).collect(Collectors.toList());
		}
		hasSkipPatterns = isNotEmpty(skipPatterns);
		if (isNotBlank(skipRowsExpression)) {
			resolvedSkipRowsExpression = ExprFactory.getSpelExpression(skipRowsExpression, sandbox);
		}
		isWorkbook = (outputFormat != null) && ((outputFormat == xls) || (outputFormat == xlsx));
		if (isWorkbook) {
			book = outputFormat == xls ? ExcelFactory.createNewXls() : ExcelFactory.createNewXlsx();
			sheet = ExcelFactory.createOrGetFirstSheet(book);
		}
	}

	/**
	 * Gets the sorted headers.
	 *
	 * @return the sorted headers
	 */
	private List<String> getSortedHeaders() {
		List<String> sortedHeaders = headers
			.stream()
			.map(asFlattenedObjects ? Function.identity() : asHtml ? StringEscapeUtils::escapeXml11 : StringEscapeUtils::escapeCsv)
			.collect(Collectors.toList());
		Collections.sort(sortedHeaders);
		if (CollectionUtils.isNotEmpty(headersOrder)) {
			Collections.reverse(headersOrder);
			headersOrder.forEach(h -> {
				if (sortedHeaders.contains(h)) {
					sortedHeaders.remove(h);
					sortedHeaders.add(0, h);
				}
			});
		}
		return sortedHeaders;
	}

	/**
	 * Should process row.
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	private boolean shouldProcessRow(Object bean) {
		if (resolvedSkipRowsExpression != null) {
			Object skip = ExprFactory.evalSpel(bean, resolvedSkipRowsExpression);
			if ((skip != null) && (skip instanceof Boolean) && ((Boolean) skip)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Transpose.
	 */
	private void transpose() {
		List<Map<String, String>> transposed = Lists.newArrayList();
		List<String> origHeaders = getSortedHeaders();
		Set<String> transposedHeaders = Sets.newLinkedHashSet();
		boolean first = true;
		for (String header : origHeaders) {
			Map<String, String> transposedData = Maps.newHashMap();
			transposedData.put("header", header);
			if (first) {
				transposedHeaders.add("header");
			}
			int dataPos = 1;
			for (Map<String, String> dataMap : data) {
				String key = "row" + dataPos;
				if (first) {
					transposedHeaders.add(key);
				}
				transposedData.put(key, defaultIfBlank(dataMap.get(header), EMPTY));
				dataPos++;
			}
			transposed.add(transposedData);
			first = false;
		}
		headers = transposedHeaders;
		data = transposed;
	}
}
