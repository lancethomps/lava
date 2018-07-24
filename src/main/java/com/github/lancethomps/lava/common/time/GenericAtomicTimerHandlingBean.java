package com.github.lancethomps.lava.common.time;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class GenericAtomicTimerHandlingBean.
 */
public class GenericAtomicTimerHandlingBean implements TimerHandlingBean {

	/** The timer logs. */
	private ConcurrentHashMap<String, Long> timerLogs;

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#getTimerLogs()
	 */
	@Override
	public Map<String, Long> getTimerLogs() {
		return timerLogs;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#setTimerLogs(java.util.Map)
	 */
	@Override
	public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
		this.timerLogs = timerLogs instanceof ConcurrentHashMap ? (ConcurrentHashMap<String, Long>) timerLogs : new ConcurrentHashMap<>(timerLogs);
		return (T) this;
	}

}