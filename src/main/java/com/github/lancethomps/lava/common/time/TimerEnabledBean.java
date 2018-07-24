package com.github.lancethomps.lava.common.time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * The Interface TimerEnabledBean.
 */
@SuppressWarnings("unchecked")
public interface TimerEnabledBean {

	/**
	 * Adds the timers to bean.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addTimersToBean(TimerHandlingBean bean) {
		if (bean != null) {
			bean.addTimers(this);
		}
		return (T) this;
	}

	/**
	 * Adds the watch.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param watch the watch
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addWatch(String key, Stopwatch watch) {
		if (watch != null) {
			if (getWatches() == null) {
				setWatches(new LinkedHashMap<>());
			}
			getWatches().put(key, watch);
		}
		return (T) this;
	}

	/**
	 * Adds the watches from other.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean) {
		return addWatchesFromOther(bean, false);
	}

	/**
	 * Adds the watches from other.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param override the override
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override) {
		return addWatchesFromOther(bean, override, null);
	}

	/**
	 * Adds the watches from other.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param override the override
	 * @param prefix the prefix
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override, String prefix) {
		if ((bean != null) && (bean.getWatches() != null)) {
			if (getWatches() == null) {
				setWatches(new LinkedHashMap<>());
			}
			if (override && (prefix == null)) {
				getWatches().putAll(bean.getWatches());
			} else {
				Map<String, Stopwatch> watches = getWatches();
				bean.getWatches().forEach((key, watch) -> {
					watches.merge(prefix == null ? key : (prefix + key), watch, (old, incoming) -> old.addTimeFromOther(incoming));
				});
			}
		}
		return (T) this;
	}

	/**
	 * Adds the watches from other.
	 *
	 * @param <T> the generic type
	 * @param bean the bean
	 * @param prefix the prefix
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, String prefix) {
		return addWatchesFromOther(bean, false, prefix);
	}

	/**
	 * Creates the atomic watch.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T createAtomicWatch(String key) {
		if (getWatches() == null) {
			setWatches(new LinkedHashMap<>());
		}
		getWatches().put(key, new AtomicStopwatch());
		return (T) this;
	}

	/**
	 * Gets the time.
	 *
	 * @param key the key
	 * @return the time
	 */
	default Long getTime(String key) {
		return Optional.ofNullable(getWatches()).map(watches -> watches.get(key)).map(Stopwatch::getTime).orElse(null);
	}

	/**
	 * Gets the watch.
	 *
	 * @param key the key
	 * @return the watch
	 */
	default Stopwatch getWatch(String key) {
		return Optional.ofNullable(getWatches()).map(watches -> watches.get(key)).orElse(null);
	}

	/**
	 * Gets the watch add if absent.
	 *
	 * @param key the key
	 * @return the watch add if absent
	 */
	default Stopwatch getWatchAddIfAbsent(String key) {
		Map<String, Stopwatch> watches = getWatches();
		if (watches == null) {
			setWatches(new LinkedHashMap<>());
			getWatches().put(key, new Stopwatch());
		}
		return getWatches().computeIfAbsent(key, k -> new Stopwatch());
	}

	/**
	 * Gets the watches.
	 *
	 * @return the watches
	 */
	Map<String, Stopwatch> getWatches();

	/**
	 * Pause timer.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T pause(String key) {
		getWatches().get(key).suspend();
		return (T) this;
	}

	/**
	 * Pause if needed.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T pauseIfNeeded(String key) {
		getWatches().get(key).suspendIfNeeded();
		return (T) this;
	}

	/**
	 * Pause sync.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T pauseSync(String key) {
		synchronized (this) {
			return pause(key);
		}
	}

	/**
	 * Sets the watches.
	 *
	 * @param <T> the generic type
	 * @param watches the watches
	 * @return the t
	 */
	<T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches);

	/**
	 * Start timer.
	 *
	 * @param key the key
	 * @return the t
	 */
	default TimerContext start(@Nonnull String key) {
		if (getWatches() == null) {
			setWatches(new LinkedHashMap<String, Stopwatch>());
		}
		return getWatches().computeIfAbsent(key, k -> new Stopwatch(false)).startOrResume();
	}

	/**
	 * Start and return.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	default <T extends TimerEnabledBean> T startAndReturn(@Nonnull String key) {
		start(key);
		return (T) this;
	}

	/**
	 * Start sync.
	 *
	 * @param key the key
	 * @return the t
	 */
	default TimerContext startSync(String key) {
		synchronized (this) {
			return start(key);
		}
	}
}
