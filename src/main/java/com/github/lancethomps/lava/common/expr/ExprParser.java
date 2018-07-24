package com.github.lancethomps.lava.common.expr;

import com.github.lancethomps.lava.common.Enums;

/**
 * The Enum ExprParser.
 */
public enum ExprParser {

	/** The js. */
	JS("nashorn"),

	/** The ognl. */
	OGNL,

	/** The py. */
	PY("python"),

	/** The spring. */
	SPEL;

	/** The engine name. */
	private final String engineName;

	/**
	 * Instantiates a new expr parser.
	 */
	ExprParser() {
		this(null);
	}

	/**
	 * Instantiates a new expr parser.
	 *
	 * @param engineName the engine name
	 */
	ExprParser(String engineName) {
		this.engineName = engineName;
	}

	static {
		Enums.createStringToTypeMap(ExprParser.class, null, ExprParser::getEngineName);
	}

	/**
	 * Gets the engine name.
	 *
	 * @return the engineName
	 */
	public String getEngineName() {
		return engineName;
	}
}