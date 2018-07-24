package com.github.lancethomps.lava.common.metrics;

import java.util.Map;

import com.github.lancethomps.lava.common.DynamicDataHandler;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class HealthCheckOwner.
 */
public class HealthCheckOwner implements DynamicDataHandler {

	/** The data. */
	@JsonIgnore
	private Map<String, Object> data;

	/** The dept code. */
	private String deptCode;

	/** The email. */
	private String email;

	/** The first name. */
	private String firstName;

	/** The last name. */
	private String lastName;

	/** The middle name. */
	private String middleName;

	/** The user id. */
	private String userId;

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	@Override
	@JsonAnyGetter
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * Gets the dept code.
	 *
	 * @return the deptCode
	 */
	public String getDeptCode() {
		return deptCode;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Gets the first name.
	 *
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Gets the last name.
	 *
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Gets the middle name.
	 *
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.DynamicDataHandler#setData(java.util.Map)
	 */
	@Override
	public <T extends DynamicDataHandler> T setData(Map<String, Object> data) {
		this.data = data;
		return (T) this;
	}

	/**
	 * Sets the dept code.
	 *
	 * @param deptCode the deptCode to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setDeptCode(String deptCode) {
		this.deptCode = deptCode;
		return this;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the email to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setEmail(String email) {
		this.email = email;
		return this;
	}

	/**
	 * Sets the first name.
	 *
	 * @param firstName the firstName to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName the lastName to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * Sets the middle name.
	 *
	 * @param middleName the middleName to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setMiddleName(String middleName) {
		this.middleName = middleName;
		return this;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the userId to set
	 * @return the health check owner
	 */
	public HealthCheckOwner setUserId(String userId) {
		this.userId = userId;
		return this;
	}

}
