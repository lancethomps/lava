package com.github.lancethomps.lava.common.time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public interface TimerEnabledBean {

  default <T extends TimerEnabledBean> T addTimersToBean(TimerHandlingBean bean) {
    if (bean != null) {
      bean.addTimers(this);
    }
    return (T) this;
  }

  default <T extends TimerEnabledBean> T addWatch(String key, Stopwatch watch) {
    if (watch != null) {
      if (getWatches() == null) {
        setWatches(new LinkedHashMap<>());
      }
      getWatches().put(key, watch);
    }
    return (T) this;
  }

  default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean) {
    return addWatchesFromOther(bean, false);
  }

  default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override) {
    return addWatchesFromOther(bean, override, null);
  }

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

  default <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, String prefix) {
    return addWatchesFromOther(bean, false, prefix);
  }

  default <T extends TimerEnabledBean> T createAtomicWatch(String key) {
    if (getWatches() == null) {
      setWatches(new LinkedHashMap<>());
    }
    getWatches().put(key, new AtomicStopwatch());
    return (T) this;
  }

  default Long getTime(String key) {
    return Optional.ofNullable(getWatches()).map(watches -> watches.get(key)).map(Stopwatch::getTime).orElse(null);
  }

  default Stopwatch getWatch(String key) {
    return Optional.ofNullable(getWatches()).map(watches -> watches.get(key)).orElse(null);
  }

  default Stopwatch getWatchAddIfAbsent(String key) {
    Map<String, Stopwatch> watches = getWatches();
    if (watches == null) {
      setWatches(new LinkedHashMap<>());
      getWatches().put(key, new Stopwatch());
    }
    return getWatches().computeIfAbsent(key, k -> new Stopwatch());
  }

  Map<String, Stopwatch> getWatches();

  <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches);

  default <T extends TimerEnabledBean> T pause(String key) {
    getWatches().get(key).suspend();
    return (T) this;
  }

  default <T extends TimerEnabledBean> T pauseIfNeeded(String key) {
    getWatches().get(key).suspendIfNeeded();
    return (T) this;
  }

  default <T extends TimerEnabledBean> T pauseSync(String key) {
    synchronized (this) {
      return pause(key);
    }
  }

  default TimerContext start(@Nonnull String key) {
    if (getWatches() == null) {
      setWatches(new LinkedHashMap<String, Stopwatch>());
    }
    return getWatches().computeIfAbsent(key, k -> new Stopwatch(false)).startOrResume();
  }

  default <T extends TimerEnabledBean> T startAndReturn(@Nonnull String key) {
    start(key);
    return (T) this;
  }

  default TimerContext startSync(String key) {
    synchronized (this) {
      return start(key);
    }
  }

}
