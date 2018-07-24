package com.github.lancethomps.lava.common.time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The Interface TimerHandlingBean.
 *
 * @author lathomps
 */
@SuppressWarnings("unchecked")
public interface TimerHandlingBean {

	/**
	 * Adds the timer.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param timer the timer
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimer(String key, Long timer) {
		if ((key != null) && (timer != null)) {
			if (getTimerLogs() == null) {
				setTimerLogs(new LinkedHashMap<>());
			}
			Long current;
			if ((current = getTimerLogs().get(key)) != null) {
				getTimerLogs().put(key, timer + current);
			} else {
				getTimerLogs().put(key, timer);
			}
		}
		return (T) this;
	}

	/**
	 * Adds the timer.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param watch the watch
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimer(String key, Stopwatch watch) {
		return addTimer(key, watch == null ? null : watch.getTime());
	}

	/**
	 * Adds the timer logs.
	 *
	 * @param <T> the generic type
	 * @param timerLogs the timer logs
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimerLogs(Map<String, Long> timerLogs) {
		if (timerLogs != null) {
			timerLogs.forEach(this::addTimer);
		}
		return (T) this;
	}

	/**
	 * Adds the timers.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimers(@Nullable TimerEnabledBean bean) {
		return addTimers(bean, null);
	}

	/**
	 * Adds the timers.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param prefix the prefix
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimers(@Nullable TimerEnabledBean bean, @Nullable String prefix) {
		if ((bean != null) && (bean.getWatches() != null)) {
			bean.getWatches().forEach((key, watch) -> addTimer(prefix == null ? key : (prefix + key), watch.getTime()));
		}
		return (T) this;
	}

	/**
	 * Adds the timers.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimersFromOther(TimerHandlingBean bean) {
		return addTimersFromOther(bean, null);
	}

	/**
	 * Adds the timers.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param prefix the prefix
	 * @return the t
	 */
	default <T extends TimerHandlingBean> T addTimersFromOther(TimerHandlingBean bean, String prefix) {
		if ((bean != null) && (bean.getTimerLogs() != null)) {
			bean.getTimerLogs().forEach((key, val) -> addTimer(prefix == null ? key : (prefix + key), val));
		}
		return (T) this;
	}

	/**
	 * Gets the timer.
	 *
	 * @param key the key
	 * @return the timer
	 */
	default Long getTimer(String key) {
		return Optional.ofNullable(getTimerLogs()).map(timers -> timers.get(key)).orElse(null);
	}

	/**
	 * Gets the timer logs.
	 *
	 * @return the timer logs
	 */
	Map<String, Long> getTimerLogs();

	/**
	 * Sets the timer logs.
	 *
	 * @param <T> the generic type
	 * @param timerLogs the timer logs
	 * @return the t
	 */
	<T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs);

}
