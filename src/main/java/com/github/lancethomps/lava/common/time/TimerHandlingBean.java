package com.github.lancethomps.lava.common.time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public interface TimerHandlingBean {

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

  default <T extends TimerHandlingBean> T addTimer(String key, Stopwatch watch) {
    return addTimer(key, watch == null ? null : watch.getTime());
  }

  default <T extends TimerHandlingBean> T addTimerLogs(Map<String, Long> timerLogs) {
    if (timerLogs != null) {
      timerLogs.forEach(this::addTimer);
    }
    return (T) this;
  }

  default <T extends TimerHandlingBean> T addTimers(@Nullable TimerEnabledBean bean) {
    return addTimers(bean, null);
  }

  default <T extends TimerHandlingBean> T addTimers(@Nullable TimerEnabledBean bean, @Nullable String prefix) {
    if ((bean != null) && (bean.getWatches() != null)) {
      bean.getWatches().forEach((key, watch) -> addTimer(prefix == null ? key : (prefix + key), watch.getTime()));
    }
    return (T) this;
  }

  default <T extends TimerHandlingBean> T addTimersFromOther(TimerHandlingBean bean) {
    return addTimersFromOther(bean, null);
  }

  default <T extends TimerHandlingBean> T addTimersFromOther(TimerHandlingBean bean, String prefix) {
    if ((bean != null) && (bean.getTimerLogs() != null)) {
      bean.getTimerLogs().forEach((key, val) -> addTimer(prefix == null ? key : (prefix + key), val));
    }
    return (T) this;
  }

  default Long getTimer(String key) {
    return Optional.ofNullable(getTimerLogs()).map(timers -> timers.get(key)).orElse(null);
  }

  Map<String, Long> getTimerLogs();

  <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs);

}
