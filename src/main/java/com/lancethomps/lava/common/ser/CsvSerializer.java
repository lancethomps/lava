package com.lancethomps.lava.common.ser;

import static com.lancethomps.lava.common.Checks.isEmpty;
import static com.lancethomps.lava.common.Checks.isNotEmpty;
import static com.lancethomps.lava.common.ser.OutputFormat.xls;
import static com.lancethomps.lava.common.ser.OutputFormat.xlsx;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.expression.spel.standard.SpelExpression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lancethomps.lava.common.Reflections;
import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.lambda.Lambdas;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.excel.ExcelFactory;
import com.lancethomps.lava.common.sorting.Sorting;
import com.lancethomps.lava.common.string.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CsvSerializer {

  public static final String DEFAULT_DELIMITER = ",";

  public static final Set<String> DEFAULT_SKIP_PROPS = new HashSet<>(Arrays.asList("@type"));

  public static final String KEY_PREFIX_LIST_SHELL = "%s[%s]";

  public static final String TABLE = "<table class=\"%s\"><thead>%s</thead><tbody>%s</tbody></table>";

  public static final String TD = "<td class=\"%s\">%s</td>";

  public static final String TH = "<th class=\"%s\">%s</th>";

  public static final String TR = "<tr class=\"%s\">%s</tr>";

  private static final Logger LOG = Logger.getLogger(CsvSerializer.class);

  private static final Set<String> SIMPLE_OUTPUT_CLASSES = Sets.newHashSet();

  private boolean asFlattenedObjects;

  private boolean asHtml;

  private boolean asHtmlEmail;

  private Workbook book;

  private String chainingSep = ".";

  private String csvDelimiter = DEFAULT_DELIMITER;

  private List<Map<String, String>> data;

  private boolean expandJsonFields;

  private boolean expandJsonFieldsInner = true;

  private boolean hasSkipPatterns;

  private Set<String> headers;

  private List<String> headersOrder;

  private String htmlCellClass = EMPTY;

  private String htmlHeaderClass = EMPTY;

  private String htmlHeaderRowClass = EMPTY;

  private String htmlRowClass = EMPTY;

  private String htmlTableClass = EMPTY;

  private Set<String> includeProperties;

  private boolean isWorkbook;

  private List<StringBuilder> lines;

  private Object obj;

  private ObjectMapper objectMapper;

  private OutputFormat outputFormat;

  private OutputParams params;

  private SpelExpression resolvedSkipRowsExpression;

  private boolean sandbox = true;

  private Sheet sheet;

  private List<Pattern> skipPatterns;

  private Set<String> skipProperties = new HashSet<>();

  private String skipRowsExpression;

  private String sort;

  private Class<?> sortAsType;

  private boolean transposeData;

  private boolean useChainingListSep = true;

  public CsvSerializer(Object obj) {
    this(obj, false);
  }

  public CsvSerializer(Object obj, boolean asHtml) {
    super();
    this.obj = obj;
    this.asHtml = asHtml;
  }

  public CsvSerializer(Object obj, OutputParams params) {
    super();
    this.obj = obj;
    this.params = params;
  }

  public static Map<String, Object> convertMapToJsonMap(Map<?, ?> map) {
    Map<String, Object> jsonMap = new HashMap<>();
    for (Entry<?, ?> ent : map.entrySet()) {
      jsonMap.put(simpleObjectToString(Serializer.JSON_OUTPUT_MAPPER, ent.getKey()), ent.getValue());
    }
    return jsonMap;
  }

  public static boolean isSimpleOutputClass(Class<?> clazz) {
    return Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz) || Enum.class
      .isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || (clazz == String.class) ||
      SIMPLE_OUTPUT_CLASSES.contains(clazz.getSimpleName());
  }

  public static String simpleObjectToString(ObjectMapper mapper, Object obj) {
    if (obj instanceof String) {
      return (String) obj;
    }
    return Serializer.toJson(mapper, obj);
  }

  public Workbook getBook() {
    return book;
  }

  public String getChainingSep() {
    return chainingSep;
  }

  public CsvSerializer setChainingSep(String chainingSep) {
    this.chainingSep = chainingSep;
    return this;
  }

  public String getCsvDelimiter() {
    return csvDelimiter;
  }

  public CsvSerializer setCsvDelimiter(String csvDelimiter) {
    this.csvDelimiter = defaultIfEmpty(csvDelimiter, DEFAULT_DELIMITER);
    return this;
  }

  public List<String> getHeadersOrder() {
    return headersOrder;
  }

  public CsvSerializer setHeadersOrder(List<String> headersOrder) {
    this.headersOrder = headersOrder;
    return this;
  }

  public String getHtmlCellClass() {
    return htmlCellClass;
  }

  public CsvSerializer setHtmlCellClass(String htmlCellClass) {
    if (htmlCellClass != null) {
      this.htmlCellClass = htmlCellClass;
    }
    return this;
  }

  public String getHtmlHeaderClass() {
    return htmlHeaderClass;
  }

  public CsvSerializer setHtmlHeaderClass(String htmlHeaderClass) {
    if (htmlHeaderClass != null) {
      this.htmlHeaderClass = htmlHeaderClass;
    }
    return this;
  }

  public String getHtmlHeaderRowClass() {
    return htmlHeaderRowClass;
  }

  public CsvSerializer setHtmlHeaderRowClass(String htmlHeaderRowClass) {
    if (htmlHeaderRowClass != null) {
      this.htmlHeaderRowClass = htmlHeaderRowClass;
    }
    return this;
  }

  public String getHtmlRowClass() {
    return htmlRowClass;
  }

  public CsvSerializer setHtmlRowClass(String htmlRowClass) {
    if (htmlRowClass != null) {
      this.htmlRowClass = htmlRowClass;
    }
    return this;
  }

  public String getHtmlTableClass() {
    return htmlTableClass;
  }

  public CsvSerializer setHtmlTableClass(String htmlTableClass) {
    if (htmlTableClass != null) {
      this.htmlTableClass = htmlTableClass;
    }
    return this;
  }

  public Set<String> getIncludeProperties() {
    return includeProperties;
  }

  public CsvSerializer setIncludeProperties(Set<String> includeProperties) {
    this.includeProperties = includeProperties;
    return this;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public CsvSerializer setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  public OutputFormat getOutputFormat() {
    return outputFormat;
  }

  public CsvSerializer setOutputFormat(OutputFormat outputFormat) {
    this.outputFormat = outputFormat;
    return this;
  }

  public OutputParams getParams() {
    return params;
  }

  public CsvSerializer setParams(OutputParams params) {
    this.params = params;
    return this;
  }

  public List<Pattern> getSkipPatterns() {
    return skipPatterns;
  }

  public CsvSerializer setSkipPatterns(List<Pattern> skipPatterns) {
    this.skipPatterns = skipPatterns;
    return this;
  }

  public Set<String> getSkipProperties() {
    return skipProperties;
  }

  public CsvSerializer setSkipProperties(Set<String> skipProperties) {
    this.skipProperties = skipProperties;
    return this;
  }

  public String getSkipRowsExpression() {
    return skipRowsExpression;
  }

  public CsvSerializer setSkipRowsExpression(String skipRowsExpression) {
    this.skipRowsExpression = skipRowsExpression;
    return this;
  }

  public String getSort() {
    return sort;
  }

  public CsvSerializer setSort(String sort) {
    this.sort = sort;
    return this;
  }

  public Class<?> getSortAsType() {
    return sortAsType;
  }

  public CsvSerializer setSortAsType(Class<?> sortAsType) {
    this.sortAsType = sortAsType;
    return this;
  }

  public boolean isAsFlattenedObjects() {
    return asFlattenedObjects;
  }

  public CsvSerializer setAsFlattenedObjects(boolean asFlattenedObjects) {
    this.asFlattenedObjects = asFlattenedObjects;
    return this;
  }

  public boolean isAsHtmlEmail() {
    return asHtmlEmail;
  }

  public CsvSerializer setAsHtmlEmail(boolean asHtmlEmail) {
    this.asHtmlEmail = asHtmlEmail;
    return this;
  }

  public boolean isExpandJsonFields() {
    return expandJsonFields;
  }

  public CsvSerializer setExpandJsonFields(boolean expandJsonFields) {
    this.expandJsonFields = expandJsonFields;
    return this;
  }

  public boolean isExpandJsonFieldsInner() {
    return expandJsonFieldsInner;
  }

  public CsvSerializer setExpandJsonFieldsInner(boolean expandJsonFieldsInner) {
    this.expandJsonFieldsInner = expandJsonFieldsInner;
    return this;
  }

  public boolean isSandbox() {
    return sandbox;
  }

  public CsvSerializer setSandbox(boolean sandbox) {
    this.sandbox = sandbox;
    return this;
  }

  public boolean isTransposeData() {
    return transposeData;
  }

  public CsvSerializer setTransposeData(boolean transposeData) {
    this.transposeData = transposeData;
    return this;
  }

  public boolean isUseChainingListSep() {
    return useChainingListSep;
  }

  public CsvSerializer setUseChainingListSep(boolean useChainingListSep) {
    this.useChainingListSep = useChainingListSep;
    return this;
  }

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
    boolean skipMapConversion =
      (params.getCsvParams() != null) && (params.getCsvParams().getSkipMapConversion() != null) && params.getCsvParams().getSkipMapConversion();
    (Reflections.isListType(obj.getClass()) ? (Collection<Object>) obj : Lists.newArrayList(obj))
      .stream()
      .filter(this::shouldProcessRow)
      .forEach(bean -> {
        Map<String, String> beanData = new HashMap<>();
        Map<String, Object> map =
          Reflections.isMapType(bean.getClass()) ? (skipMapConversion ? (Map<String, Object>) bean : convertMapToJsonMap((Map<?, ?>) bean))
            : Serializer.toMapViaJson(objectMapper, bean);
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

  public Workbook toBook() {
    toCsv();
    return getBook();
  }

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
      thead = format(
        TR,
        htmlHeaderRowClass,
        sortedHeaders.stream().map(h -> format(asHtmlEmail ? TD : TH, htmlHeaderClass, h)).reduce(EMPTY, String::concat)
      );
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
          if (params.testCsvAlwaysQuote()) {
            cell = "\"" + StringUtils.replace(cell, "\"", "\"\"") + "\"";
          } else if (!params.testCsvNeverQuote()) {
            cell = escapeCsv(cell);
          }
        }
        rowData.add(cell);
      }
      if (asHtml) {
        tbody +=
          format(TR, htmlRowClass, rowData.stream().map(h -> format(TD, htmlCellClass, h)).reduce(EMPTY, String::concat)) + System.lineSeparator();
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

  @Override
  protected void finalize() throws Throwable {
    if (book != null) {
      try {
        book.close();
      } catch (Throwable e) {
      }
    }
    super.finalize();
  }

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
          isNotEmpty(params.getCsvWrapFieldsInFunction()) && params.getCsvWrapFieldsInFunction().contains(fieldName) && StringUtil.isNumeric(val) ?
            "=\"" + val + '"' : val
        );
      } else if (Reflections.isListType(rootClass)) {
        List<?> innerBeanList = (List<?>) bean;
        for (int i = 0; i < innerBeanList.size(); i++) {
          String newFullKey = useChainingListSep ? String.format(KEY_PREFIX_LIST_SHELL, fullKey, i) : fullKey + i;
          addData(data, fieldName, newFullKey, innerBeanList.get(i));
        }
      } else {
        Map<String, Object> map =
          Reflections.isMapType(rootClass) ? convertMapToJsonMap((Map<?, ?>) bean) : Serializer.toMapViaJson(objectMapper, bean);
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
      skipPatterns = skipProperties
        .stream()
        .filter(p -> p.startsWith("@REGEX@"))
        .map(p -> Pattern.compile(removeStart(p, "@REGEX@")))
        .collect(Collectors.toList());
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

  private List<String> getSortedHeaders() {
    List<String> sortedHeaders = headers
      .stream()
      .map((asFlattenedObjects || params.testCsvNeverQuote()) ? Function.identity() : asHtml ? StringEscapeUtils::escapeXml11 : StringEscapeUtils::escapeCsv)
      .sorted()
      .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(headersOrder)) {
      Lists.reverse(headersOrder).forEach(h -> {
        if (sortedHeaders.contains(h)) {
          sortedHeaders.remove(h);
          sortedHeaders.add(0, h);
        }
      });
    }
    return sortedHeaders;
  }

  private boolean shouldProcessRow(Object bean) {
    if (resolvedSkipRowsExpression != null) {
      Object skip = ExprFactory.evalSpel(bean, resolvedSkipRowsExpression);
      return (skip == null) || (!(skip instanceof Boolean)) || (!((Boolean) skip));
    }
    return true;
  }

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
