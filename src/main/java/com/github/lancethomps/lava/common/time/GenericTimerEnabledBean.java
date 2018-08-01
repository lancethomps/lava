package com.github.lancethomps.lava.common.time;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class AbstractTimerEnabledBean.
 *
 * @author lancethomps
 */
public class GenericTimerEnabledBean implements TimerEnabledBean {

	/** The watches. */
	@JsonIgnore
	private Map<String, Stopwatch> watches;

	/**
	 * Gets the watches.
	 *
	 * @return the watches
	 */
	@Override
	public Map<String, Stopwatch> getWatches() {
		return watches;
	}

	/**
	 * Sets the watches.
	 *
	 * @param <T> the generic type
	 * @param watches the watches
	 * @return the t
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
		this.watches = watches;
		return (T) this;
	}
}
