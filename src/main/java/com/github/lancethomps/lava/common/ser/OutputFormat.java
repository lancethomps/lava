package com.github.lancethomps.lava.common.ser;

import static com.github.lancethomps.lava.common.ser.OutputFormatType.data;
import static com.github.lancethomps.lava.common.ser.OutputFormatType.document;
import static com.github.lancethomps.lava.common.ser.OutputFormatType.image;

import java.io.File;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.lancethomps.lava.common.Enums;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The Enum OutputFormat.
 */
public enum OutputFormat {

	/** The csv. */
	csv(data, OutputFormat.CONTENT_TYPE_CSV, true),

	/** The gif. */
	gif(image, OutputFormat.CONTENT_TYPE_GIF),

	/** The html. */
	html(data, OutputFormat.CONTENT_TYPE_HTML),

	/** The jpeg. */
	jpeg(image, OutputFormat.CONTENT_TYPE_JPEG),

	/** The json. */
	json(data, OutputFormat.CONTENT_TYPE_JSON, true),

	/** The json compressed. */
	jsonCompressed(data, OutputFormat.CONTENT_TYPE_TEXT),

	/** The jsonp. */
	jsonp(data, OutputFormat.CONTENT_TYPE_JS),

	/** The pdf. */
	pdf(document, OutputFormat.CONTENT_TYPE_PDF),

	/** The png. */
	png(image, OutputFormat.CONTENT_TYPE_PNG),

	/** The smile. */
	smile(data, OutputFormat.CONTENT_TYPE_SMILE),

	/** The xls. */
	xls(document, OutputFormat.CONTENT_TYPE_XLS),

	/** The xlsx. */
	xlsx(document, OutputFormat.CONTENT_TYPE_XLSX),

	/** The xml. */
	xml(data, OutputFormat.CONTENT_TYPE_XML, true),

	/** The yaml. */
	yaml(data, OutputFormat.CONTENT_TYPE_YAML, true);

	/** The Constant CONTENT_CHARSET. */
	public static final String CONTENT_CHARSET = ";charset=UTF-8";

	/** The Constant CONTENT_TYPE_CSV. */
	public static final String CONTENT_TYPE_CSV = "text/csv" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_ENCODED. */
	public static final String CONTENT_TYPE_ENCODED = "application/x-www-form-urlencoded" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_GIF. */
	public static final String CONTENT_TYPE_GIF = "image/gif" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_HTML. */
	public static final String CONTENT_TYPE_HTML = "text/html" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_JPEG. */
	public static final String CONTENT_TYPE_JPEG = "image/jpeg" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_JS. */
	public static final String CONTENT_TYPE_JS = "application/javascript" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_JSON. */
	public static final String CONTENT_TYPE_JSON = "application/json" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_MANIFEST. */
	public static final String CONTENT_TYPE_MANIFEST = "text/cache-manifest;charset=ISO-8859-1";

	/** The Constant CONTENT_TYPE_PDF. */
	public static final String CONTENT_TYPE_PDF = "application/pdf" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_PNG. */
	public static final String CONTENT_TYPE_PNG = "image/png" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_SMILE. */
	public static final String CONTENT_TYPE_SMILE = "application/x-jackson-smile" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_TEXT. */
	public static final String CONTENT_TYPE_TEXT = "text/plain" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_XLS. */
	public static final String CONTENT_TYPE_XLS = "application/xls" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_XLSX. */
	public static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_XML. */
	public static final String CONTENT_TYPE_XML = "application/xml" + CONTENT_CHARSET;

	/** The Constant CONTENT_TYPE_YAML. */
	public static final String CONTENT_TYPE_YAML = "application/yaml" + CONTENT_CHARSET;

	/** The content type. */
	private final String contentType;

	/** The parse from header. */
	private final boolean parseFromHeader;

	/** The type. */
	private final OutputFormatType type;

	/**
	 * Instantiates a new output format.
	 *
	 * @param type the type
	 * @param contentType the content type
	 */
	OutputFormat(OutputFormatType type, String contentType) {
		this(type, contentType, false);
	}

	/**
	 * Instantiates a new output format.
	 *
	 * @param type the type
	 * @param contentType the content type
	 * @param parseFromHeader the parse from header
	 */
	OutputFormat(OutputFormatType type, String contentType, boolean parseFromHeader) {
		this.type = type;
		this.contentType = contentType;
		this.parseFromHeader = parseFromHeader;
	}

	static {
		Enums.createStringToTypeMap(OutputFormat.class, null, OutputFormat::getAcceptHeaderContentType);
	}

	/**
	 * From file.
	 *
	 * @param file the file
	 * @return the output format
	 */
	public static OutputFormat fromFile(@Nonnull File file) {
		return fromString(FilenameUtils.getExtension(file.getName()), null);
	}

	/**
	 * From string.
	 *
	 * @param text the text
	 * @return the output format
	 */
	public static OutputFormat fromString(String text) {
		return fromString(text, json);
	}

	/**
	 * From string.
	 *
	 * @param text the text
	 * @param defaultValue the default value
	 * @return the output format
	 */
	public static OutputFormat fromString(String text, OutputFormat defaultValue) {
		return Enums.fromString(OutputFormat.class, text, defaultValue, false);
	}

	/**
	 * From string with null.
	 *
	 * @param text the text
	 * @return the output format
	 */
	@JsonCreator
	public static OutputFormat fromStringWithNull(String text) {
		return fromString(text, null);
	}

	/**
	 * Gets the accept header content type.
	 *
	 * @return the accept header content type
	 */
	public String getAcceptHeaderContentType() {
		return parseFromHeader ? getContentTypeWithoutCharset() : null;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Gets the content type without charset.
	 *
	 * @return the content type without charset
	 */
	public String getContentTypeWithoutCharset() {
		return StringUtils.substringBefore(contentType, ";");
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public OutputFormatType getType() {
		return type;
	}

	/**
	 * @return the parseFromHeader
	 */
	public boolean isParseFromHeader() {
		return parseFromHeader;
	}
}
