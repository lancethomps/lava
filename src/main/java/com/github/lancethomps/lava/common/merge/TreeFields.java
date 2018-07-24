package com.github.lancethomps.lava.common.merge;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class TreeFields.
 *
 * @author lathomps
 */
public class TreeFields extends ExternalizableBean {

	/** The agg vals. */
	private Map<String, BigDecimal> aggVals = new HashMap<>();

	/** The max vals. */
	private Map<String, Comparable<?>> maxVals = new HashMap<>();

	/** The min vals. */
	private Map<String, Comparable<?>> minVals = new HashMap<>();

	/**
	 * @return the aggVals
	 */
	public Map<String, BigDecimal> getAggVals() {
		return aggVals;
	}

	/**
	 * @return the maxVals
	 */
	public Map<String, Comparable<?>> getMaxVals() {
		return maxVals;
	}

	/**
	 * @return the minVals
	 */
	public Map<String, Comparable<?>> getMinVals() {
		return minVals;
	}

	/**
	 * @param aggVals the aggVals to set
	 */
	public void setAggVals(Map<String, BigDecimal> aggVals) {
		this.aggVals = aggVals;
	}

	/**
	 * @param maxVals the maxVals to set
	 */
	public void setMaxVals(Map<String, Comparable<?>> maxVals) {
		this.maxVals = maxVals;
	}

	/**
	 * @param minVals the minVals to set
	 */
	public void setMinVals(Map<String, Comparable<?>> minVals) {
		this.minVals = minVals;
	}

}
