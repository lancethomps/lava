package com.github.lancethomps.lava.common.ser;

import java.util.Set;

import com.github.lancethomps.lava.common.web.requests.parsers.RequestField;

/**
 * The Class CsvParams.
 */
public class CsvParams extends ExternalizableBean {

	/** The always include headers. */
	@RequestField
	private Set<String> alwaysIncludeHeaders;

	/** The skip map conversion. */
	@RequestField
	private Boolean skipMapConversion;

	/**
	 * @return the alwaysIncludeHeaders
	 */
	public Set<String> getAlwaysIncludeHeaders() {
		return alwaysIncludeHeaders;
	}

	/**
	 * @return the skipMapConversion
	 */
	public Boolean getSkipMapConversion() {
		return skipMapConversion;
	}

	/**
	 * Sets the always include headers.
	 *
	 * @param alwaysIncludeHeaders the alwaysIncludeHeaders to set
	 * @return the csv params
	 */
	public CsvParams setAlwaysIncludeHeaders(Set<String> alwaysIncludeHeaders) {
		this.alwaysIncludeHeaders = alwaysIncludeHeaders;
		return this;
	}

	/**
	 * Sets the skip map conversion.
	 *
	 * @param skipMapConversion the skipMapConversion to set
	 * @return the csv params
	 */
	public CsvParams setSkipMapConversion(Boolean skipMapConversion) {
		this.skipMapConversion = skipMapConversion;
		return this;
	}

}
