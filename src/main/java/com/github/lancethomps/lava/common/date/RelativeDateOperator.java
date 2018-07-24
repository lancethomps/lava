package com.github.lancethomps.lava.common.date;

import com.github.lancethomps.lava.common.Enums;

/**
 * The Enum RelativeDateOperator.
 */
public enum RelativeDateOperator {

	/** The minus. */
	MINUS("-"),

	/** The plus. */
	PLUS("+"),

	/** The round. */
	ROUND("/"),

	/** The setting. */
	SETTING(":");

	/** The symbol. */
	private final String symbol;

	/**
	 * Instantiates a new relative date operator.
	 *
	 * @param symbol the symbol
	 */
	RelativeDateOperator(String symbol) {
		this.symbol = symbol;
	}

	static {
		Enums.createStringToTypeMap(RelativeDateOperator.class, null, RelativeDateOperator::getSymbol);
	}

	/**
	 * Gets the symbol.
	 *
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

}
