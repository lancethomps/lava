package com.github.lancethomps.lava.common.web.requests.parsers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The Class MockRequestWithAllowedJsonParam.
 */
public class MockRequestWithAllowedJsonParam {

	/** The from date. */
	@RequestField
	private LocalDateTime fromDate;

	/** The group by field. */
	@RequestField
	private List<String> groupByFields;

	/** The request key. */
	@RequestField
	private String requestKey;

	/** The response fields. */
	@RequestField
	private List<String> responseFields;

	/** The to date. */
	@RequestField
	private LocalDateTime toDate;

	/**
	 * Gets the from date.
	 *
	 * @return the fromDate
	 */
	public LocalDateTime getFromDate() {
		return fromDate;
	}

	/**
	 * Gets the group by fields.
	 *
	 * @return the groupByFields
	 */
	public List<String> getGroupByFields() {
		return groupByFields;
	}

	/**
	 * Gets the request key.
	 *
	 * @return the requestKey
	 */
	public String getRequestKey() {
		return requestKey;
	}

	/**
	 * Gets the response fields.
	 *
	 * @return the responseFields
	 */
	public List<String> getResponseFields() {
		return responseFields;
	}

	/**
	 * Gets the to date.
	 *
	 * @return the toDate
	 */
	public LocalDateTime getToDate() {
		return toDate;
	}

	/**
	 * Sets the from date.
	 *
	 * @param fromDate the fromDate to set
	 * @return the mock request with allowed json param
	 */
	public MockRequestWithAllowedJsonParam setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
		return this;
	}

	/**
	 * Sets the group by fields.
	 *
	 * @param groupByFields the groupByFields to set
	 * @return the mock request with allowed json param
	 */
	public MockRequestWithAllowedJsonParam setGroupByFields(List<String> groupByFields) {
		this.groupByFields = groupByFields;
		return this;
	}

	/**
	 * Sets the request key.
	 *
	 * @param requestKey the requestKey to set
	 * @return the mock request with allowed json param
	 */
	public MockRequestWithAllowedJsonParam setRequestKey(String requestKey) {
		this.requestKey = requestKey;
		return this;
	}

	/**
	 * Sets the response fields.
	 *
	 * @param responseFields the responseFields to set
	 * @return the mock request with allowed json param
	 */
	public MockRequestWithAllowedJsonParam setResponseFields(List<String> responseFields) {
		this.responseFields = responseFields;
		return this;
	}

	/**
	 * Sets the to date.
	 *
	 * @param toDate the toDate to set
	 * @return the mock request with allowed json param
	 */
	public MockRequestWithAllowedJsonParam setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
		return this;
	}

}
