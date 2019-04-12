package com.github.lancethomps.lava.common.ser;

import static com.github.lancethomps.lava.common.ser.OutputFormatType.data;
import static com.github.lancethomps.lava.common.ser.OutputFormatType.document;
import static com.github.lancethomps.lava.common.ser.OutputFormatType.image;

import java.io.File;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.lancethomps.lava.common.Enums;

public enum OutputFormat {

  csv(data, OutputFormat.CONTENT_TYPE_CSV, true),

  gif(image, OutputFormat.CONTENT_TYPE_GIF),

  html(data, OutputFormat.CONTENT_TYPE_HTML),

  jpeg(image, OutputFormat.CONTENT_TYPE_JPEG),

  json(data, OutputFormat.CONTENT_TYPE_JSON, true),

  jsonCompressed(data, OutputFormat.CONTENT_TYPE_TEXT),

  jsonp(data, OutputFormat.CONTENT_TYPE_JS),

  pdf(document, OutputFormat.CONTENT_TYPE_PDF),

  png(image, OutputFormat.CONTENT_TYPE_PNG),

  smile(data, OutputFormat.CONTENT_TYPE_SMILE),

  xls(document, OutputFormat.CONTENT_TYPE_XLS),

  xlsx(document, OutputFormat.CONTENT_TYPE_XLSX),

  xml(data, OutputFormat.CONTENT_TYPE_XML, true),

  yaml(data, OutputFormat.CONTENT_TYPE_YAML, true);

  public static final String CONTENT_CHARSET = ";charset=UTF-8";
  public static final String CONTENT_TYPE_CSV = "text/csv" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_ENCODED = "application/x-www-form-urlencoded" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_GIF = "image/gif" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_HTML = "text/html" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_JPEG = "image/jpeg" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_JS = "application/javascript" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_JSON = "application/json" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_MANIFEST = "text/cache-manifest;charset=ISO-8859-1";
  public static final String CONTENT_TYPE_PDF = "application/pdf" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_PNG = "image/png" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_SMILE = "application/x-jackson-smile" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_TEXT = "text/plain" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_XLS = "application/xls" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_XML = "application/xml" + CONTENT_CHARSET;
  public static final String CONTENT_TYPE_YAML = "application/yaml" + CONTENT_CHARSET;

  static {
    Enums.createStringToTypeMap(OutputFormat.class, null, OutputFormat::getAcceptHeaderContentType);
  }

  private final String contentType;
  private final boolean parseFromHeader;
  private final OutputFormatType type;

  OutputFormat(OutputFormatType type, String contentType) {
    this(type, contentType, false);
  }

  OutputFormat(OutputFormatType type, String contentType, boolean parseFromHeader) {
    this.type = type;
    this.contentType = contentType;
    this.parseFromHeader = parseFromHeader;
  }

  public static OutputFormat fromFile(@Nonnull File file) {
    return fromString(FilenameUtils.getExtension(file.getName()), null);
  }

  public static OutputFormat fromString(String text) {
    return fromString(text, json);
  }

  public static OutputFormat fromString(String text, OutputFormat defaultValue) {
    return Enums.fromString(OutputFormat.class, text, defaultValue, false);
  }

  @JsonCreator
  public static OutputFormat fromStringWithNull(String text) {
    return fromString(text, null);
  }

  public String getAcceptHeaderContentType() {
    return parseFromHeader ? getContentTypeWithoutCharset() : null;
  }

  public String getContentType() {
    return contentType;
  }

  public String getContentTypeWithoutCharset() {
    return StringUtils.substringBefore(contentType, ";");
  }

  public OutputFormatType getType() {
    return type;
  }

  public boolean isParseFromHeader() {
    return parseFromHeader;
  }
}
