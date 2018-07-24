package com.github.lancethomps.lava.common.web.ua;

import java.util.regex.Pattern;

import com.github.lancethomps.lava.common.Patterns;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class AbstractUserAgentRegex.
 *
 * @author lathomps
 */
public abstract class AbstractUserAgentParserRegex extends ExternalizableBean {

	/** The parsed regex. */
	private Pattern parsedRegex;

	/** The regex. */
	private String regex;

	/** The regex flag. */
	private String regexFlag;

	@Override
	public void afterDeserialization() {
		super.afterDeserialization();
		if (regex != null) {
			parsedRegex = Pattern.compile(regex, Patterns.asOptions(regexFlag));
		}
	}

	/**
	 * @return the regex
	 */
	public Pattern getParsedRegex() {
		return parsedRegex;
	}

	/**
	 * @return the regex
	 */
	public String getRegex() {
		return regex;
	}

	/**
	 * @return the regexFlag
	 */
	public String getRegexFlag() {
		return regexFlag;
	}

	/**
	 * @param parsedRegex the parsedRegex to set
	 */
	public void setParsedRegex(Pattern parsedRegex) {
		this.parsedRegex = parsedRegex;
	}

	/**
	 * @param regex the regex to set
	 */
	public void setRegex(String regex) {
		this.regex = regex;
	}

	/**
	 * @param regexFlag the regexFlag to set
	 */
	public void setRegexFlag(String regexFlag) {
		this.regexFlag = regexFlag;
	}
}
