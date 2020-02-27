// CHECKSTYLE.OFF: OpenCSV
package com.lancethomps.lava.common.file;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.expr.ExpressionsMatchResult;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.string.StringUtil;
import com.opencsv.CSVParser;

public class FileParser<T> {

  private static final Logger LOG = LogManager.getLogger(FileParser.class);
  @Nonnull
  private final FileParserOptions options;
  private String contents;
  private File file;
  private Map<String, Integer> headers;
  private boolean isListOfPojos;
  private List<String> rawLines;

  private List<T> resultList;

  private InputStream stream;

  private Class<T> type;

  @SuppressWarnings("unchecked")
  protected FileParser(Class<T> type, FileParserOptions options) {
    super();
    this.type = (type == null) && (options != null) ? (Class<T>) options.getType() : type;
    this.options = options == null ? new FileParserOptions() : options;
    this.headers = this.options.getHeaders();
  }

  public FileParser(File file) {
    this(file, null);
  }

  public FileParser(File file, Class<T> type) {
    this(file, type, new FileParserOptions());
  }

  public FileParser(File file, Class<T> type, FileParserOptions options) {
    this(type, options);
    this.file = file;
  }

  public FileParser(InputStream stream, Class<T> type, FileParserOptions options) {
    this(type, options);
    this.stream = stream;
  }

  public FileParser(String contents, Class<T> type, FileParserOptions options) {
    this(type, options);
    this.contents = contents;
  }

  public String getContents() {
    return contents;
  }

  @SuppressWarnings("unchecked")
  public List<T> getResultList() {
    return (this.resultList == null) && (rawLines != null) ? (List<T>) rawLines : resultList;
  }

  public List<Map<String, Object>> getResultMaps() {
    return (List<Map<String, Object>>) resultList;
  }

  @SuppressWarnings("unchecked")
  public FileParser<T> parseFile() throws FileParsingException {
    isListOfPojos = type != null;
    resultList = new ArrayList<>();
    try (LineNumberReader br = initializeReader()) {
      if ((options.getFirstRowNum() != null) && (options.getFirstRowNum() > 1)) {
        for (int idx = 1; idx < options.getFirstRowNum(); idx++) {
          br.readLine();
        }
      }
      String line = br.readLine();
      final boolean keysAsPaths = options.testKeysAsPaths();
      char sepChar = (options.getSepChar() != null) ? options.getSepChar() : FileUtil.getSeparationChar(line);
      if (Checks.isEmpty(headers)) {
        if (Checks.isNotEmpty(options.getRemoveLinesWithPrefixes())) {
          while ((line != null) && StringUtil.startsWith(line, options.getRemoveLinesWithPrefixes())) {
            line = br.readLine();
          }
          if (line == null) {
            return null;
          }
        }
        if (!options.testTranspose()) {
          headers = getConvertedHeaders(FileUtil.getHeaderPositionsPreserveCase(line, sepChar));
          line = br.readLine();
        }
      }
      CSVParser parser = Collect.getCsvParser(sepChar);
      if ((options.getMaxLines() != null)) {
        int totalLines = file != null ? FileUtil.countLines(file.getPath()) : Collect.splitCsv(contents, '\n').length;
        int startPos = Math.max(br.getLineNumber(), totalLines - options.getMaxLines());
        if (startPos != br.getLineNumber()) {
          FileUtil.goToLine(br, startPos);
        }
      }
      if (options.testTranspose()) {
        List<String[]> allData = new ArrayList<>();
        Serializer.parseCsvLines(options, br, parser, line, allData::add);
        List<String[]> transposedLines = Stream.of(Collect.transpose(allData.toArray(new String[][]{}))).collect(Collectors.toList());
        String[] headersLine = transposedLines.remove(0);
        headers = new HashMap<>();
        int i = 0;
        for (String header : headersLine) {
          headers.put(header, i);
          i++;
        }
        headers = getConvertedHeaders(headers);
        for (String[] data : transposedLines) {
          consumeLine(data);
        }
      } else {
        Serializer.parseCsvLines(options, br, parser, line, this::consumeLine);
      }
    } catch (FileParsingException e) {
      throw e;
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting data from file [%s] or contents [%s].", file, contents);
    }
    return this;
  }

  public FileParser<T> parseFileToList() {
    try (LineNumberReader br = file != null ? FileUtil.getFileReader(file) : new LineNumberReader(new StringReader(contents))) {
      int count = 0;
      rawLines = Lists.newArrayList();
      String line = br.readLine();
      while (line != null) {
        if (Checks.isEmpty(options.getRemoveLinesWithPrefixes()) || !StringUtil.startsWith(line, options.getRemoveLinesWithPrefixes())) {
          rawLines.add(line);
          count++;
          line = br.readLine();
          if ((options.getMaxLines() != null) && (count >= options.getMaxLines())) {
            break;
          }
        } else {
          line = br.readLine();
        }
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting data from file [%s] or contents [%s].", file, contents);
    }
    return this;
  }

  public FileParser<T> parseFileToString() {
    parseFileToList();
    contents = Checks.isEmpty(rawLines) ? null : StringUtils.join(rawLines, System.lineSeparator());
    return this;
  }

  private void consumeLine(String[] data) throws FileParsingException {
    Map<String, Object> dataMap = new TreeMap<>();
    if (options.getRowConstants() != null) {
      dataMap.putAll(options.getRowConstants());
    }
    int dataLength = data == null ? 0 : data.length;
    for (Entry<String, Integer> entry : headers.entrySet()) {
      int pos = entry.getValue();
      if (pos < dataLength) {
        String key = entry.getKey();
        Object value = data[entry.getValue()];
        if (isNotBlank((String) value) && shouldUseFieldValue(key, (String) value)) {
          if (options.testTrimValues()) {
            value = ((String) value).trim();
          }
          if (options.getFieldConverter() != null) {
            try {
              value = options.getFieldConverter().convertObject(value, key);
            } catch (Exception e) {
              Logs.logError(LOG, e, "Error creating %s object from map [%s].", type, dataMap);
              return;
            }
          }
          if (options.testKeysAsPaths()) {
            Serializer.addPathKeyToMap(dataMap, key, value);
          } else {
            dataMap.put(key, value);
          }
        }
      }
    }
    if (options.getPostProcessor() != null) {
      dataMap = options.getPostProcessor().postProcessDataMap(dataMap);
    }
    if (options.testUsePathKeys()) {
      dataMap = Serializer.convertPathKeyMapToPojoMap(dataMap, null, type);
    }
    if (dataMap != null) {
      if (options.getCopyFields() != null) {
        for (Entry<String, Set<String>> copy : options.getCopyFields().entrySet()) {
          Object val = dataMap.get(copy.getKey());
          if (val != null) {
            for (String dest : copy.getValue()) {
              dataMap.putIfAbsent(dest, val);
            }
          }
        }
      }
      T result = isListOfPojos ? Serializer.fromMap(dataMap, type) : (T) dataMap;
      if (options.getRowValidationExpressions() != null) {
        ExpressionsMatchResult exprMatch = Checks.doAnyExpressionsMatchUsingRootWrapper(options.getRowValidationExpressions(), result, this);
        if (exprMatch.testMatchedOrHasErrors()) {
          String message = String.format(
            "Found disallowed row: rowData=%s %s ",
            LOG.isTraceEnabled() ? dataMap : Logs.ENABLE_TRACE_MSG,
            exprMatch
          );
          if (options.testStrict()) {
            throw new FileParsingException(message);
          }
          Logs.logWarn(LOG, message);
          return;
        }
      }
      resultList.add(result);
    }
  }

  private Map<String, Integer> getConvertedHeaders(Map<String, Integer> headers) throws Exception {
    if ((options.getHeaderConverter() != null)) {
      Map<String, Integer> convertedHeaders = new HashMap<>();
      for (Entry<String, Integer> entry : headers.entrySet()) {
        String converted = (String) options.getHeaderConverter().convertObject(entry.getKey(), null);
        if (isNotBlank(converted)) {
          convertedHeaders.put(converted, entry.getValue());
        }
      }
      headers = convertedHeaders;
    } else if ((options.getHeaderConversions() != null)) {
      Map<String, Integer> convertedHeaders = new HashMap<>();
      for (Entry<String, Integer> entry : headers.entrySet()) {
        String converted = options.getHeaderConversions().getOrDefault(entry.getKey(), entry.getKey());
        if (isNotBlank(converted)) {
          convertedHeaders.put(converted, entry.getValue());
        }
      }
      headers = convertedHeaders;
    }
    final boolean hasIgnoreFields = Checks.isNotEmpty(options.getIgnoreFields());
    final boolean hasHeadersWhiteAndBlackList = Checks.isNotEmpty(options.getHeadersBlackList()) || Checks.isNotEmpty(options.getHeadersWhiteList());
    if (hasIgnoreFields || hasHeadersWhiteAndBlackList) {
      for (String key : new ArrayList<>(headers.keySet())) {
        if (hasIgnoreFields && options.getIgnoreFields().contains(key)) {
          headers.remove(key);
          continue;
        }
        if (hasHeadersWhiteAndBlackList) {
          Pair<Boolean, Pattern> checkResult =
            Checks.passesWhiteAndBlackListCheck(key, options.getHeadersWhiteList(), options.getHeadersBlackList(), true);
          if (!checkResult.getLeft()) {
            String message = String.format("Found disallowed column: key=%s pattern=%s ", key, checkResult.getRight());
            if (options.testStrict()) {
              throw new FileParsingException(message);
            }
            Logs.logWarn(LOG, message);
            headers.remove(key);
          }
        }
      }
    }
    return headers;
  }

  private LineNumberReader initializeReader() throws IOException {
    if (file != null) {
      return FileUtil.getFileReader(file);
    } else if (stream != null) {
      return new LineNumberReader(new InputStreamReader(stream));
    }
    return new LineNumberReader(new StringReader(contents));
  }

  private boolean shouldUseFieldValue(String key, String value) throws FileParsingException {
    Set<Pattern> whiteList = Checks.defaultIfNull(
      options.getFieldValuesWhiteListByColumn() != null ? options.getFieldValuesWhiteListByColumn().get(key) : null,
      options.getFieldValuesWhiteList()
    );
    Set<Pattern> blackList = Checks.defaultIfNull(
      options.getFieldValuesBlackListByColumn() != null ? options.getFieldValuesBlackListByColumn().get(key) : null,
      options.getFieldValuesBlackList()
    );
    if (Checks.isNotEmpty(whiteList) || Checks.isNotEmpty(blackList)) {
      Pair<Boolean, Pattern> checkResult = Checks.passesWhiteAndBlackListCheck(value, whiteList, blackList, true);
      if (!checkResult.getLeft().booleanValue()) {
        String message =
          String.format("Found disallowed field value: key=%s pattern=%s value=%s ", key, checkResult.getRight(), Logs.getSplunkValueString(value));
        if (options.testStrict()) {
          throw new FileParsingException(message);
        }
        Logs.logWarn(LOG, message);
      }
      return checkResult.getLeft();
    }
    return true;
  }

}
