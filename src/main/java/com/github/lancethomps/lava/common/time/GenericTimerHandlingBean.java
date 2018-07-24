package com.github.lancethomps.lava.common.time;

import java.util.Map;

/**
 * The Class GenericTimerHandlingBean.
 */
public class GenericTimerHandlingBean implements TimerHandlingBean {

	/** The timer logs. */
	private Map<String, Long> timerLogs;

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
		this.timerLogs = timerLogs;
		return (T) this;
	}

}
