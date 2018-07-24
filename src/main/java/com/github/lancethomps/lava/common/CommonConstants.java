package com.github.lancethomps.lava.common;

import org.apache.commons.lang3.builder.StandardToStringStyle;

import com.github.lancethomps.lava.common.string.OmittingToStringStyle;

/**
 * The Class CommonConstants.
 *
 * @author lathomps
 */
public class CommonConstants {

	/** The Constant CLASSPATH_PREFIX. */
	public static final String CLASSPATH_PREFIX = "classpath:";

	/** The Constant DEFAULT_TO_STRING_STYLE. */
	public static final StandardToStringStyle DEFAULT_TO_STRING_STYLE = new StandardToStringStyle();

	/** The Constant LOCALE_PARAM. */
	public static final String LOCALE_PARAM = "locale";

	/** The Constant LOG_LINE_SEP. */
	public static final String LOG_LINE_SEP = "************************************************************************************";

	/** The Constant REQUEST_ID_KEY. */
	public static final String REQUEST_ID_KEY = "wtp.REQUEST_ID";

	/** The Constant REQUEST_START_KEY. */
	public static final String REQUEST_START_KEY = "_zzRequestStartTime";

	/** The Constant TO_STRING_STYLE_WITHOUT_NULL. */
	public static final OmittingToStringStyle TO_STRING_STYLE_WITHOUT_NULL = OmittingToStringStyle.DEFAULT_INSTANCE;

	/** The Constant UNKNOWN_USER. */
	public static final String UNKNOWN = "Unknown";

	/** The Constant UNKNOWN_USER. */
	public static final String UNKNOWN_USER = UNKNOWN;

	static {
		DEFAULT_TO_STRING_STYLE.setUseIdentityHashCode(false);
		DEFAULT_TO_STRING_STYLE.setUseShortClassName(true);
		DEFAULT_TO_STRING_STYLE.setNullText("null");
		DEFAULT_TO_STRING_STYLE.setFieldSeparator(", ");
	}

}
