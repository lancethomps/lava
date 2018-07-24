package com.github.lancethomps.lava.common.web.requests;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.string.WordUtil;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The Class MissingRequestParameter.
 */
public class MissingRequestParameter extends ExternalizableBean {

	/** The name. */
	private String name;

	/** The type. */
	private String type;

	/**
	 * Instantiates a new missing api request parameter.
	 */
	public MissingRequestParameter() {
		super();
	}

	/**
	 * Instantiates a new missing api request parameter.
	 *
	 * @param name the name
	 * @param type the type
	 */
	public MissingRequestParameter(String name, String type) {
		this();
		this.name = name;
		this.type = type;
	}

	/**
	 * From string.
	 *
	 * @param param the param
	 * @return the missing request parameter
	 */
	@JsonCreator
	public static MissingRequestParameter fromString(String param) {
		String name = null;
		if (contains(param, ':')) {
			name = trimToNull(substringBefore(param, ":"));
		} else {
			name = param;
		}
		return new MissingRequestParameter(name, trimToNull(substringAfter(param, ":")));
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		String message = "Required " + type + " parameter '" + name + "' is not present";
		if ((name != null) && (WordUtil.getSingularVersionOfWord(name) != null)) {
			String singleParam = WordUtil.getSingularVersionOfWord(name);
			message += " - it may also be specified as a singular version using the '" + singleParam + "' parameter";
		}
		return message;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 * @return the missing api request parameter
	 */
	public MissingRequestParameter setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 * @return the missing api request parameter
	 */
	public MissingRequestParameter setType(String type) {
		this.type = type;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder param = new StringBuilder();
		if (getName() != null) {
			param.append(getName());
		}
		if (getType() != null) {
			param.append(':').append(getType());
		}
		return param.toString();
	}

}
