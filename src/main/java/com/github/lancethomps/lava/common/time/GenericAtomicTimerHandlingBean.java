package com.github.lancethomps.lava.common.time;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericAtomicTimerHandlingBean implements TimerHandlingBean {

  private ConcurrentHashMap<String, Long> timerLogs;

  @Override
  public Map<String, Long> getTimerLogs() {
    return timerLogs;
  }

  @Override
  public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
    this.timerLogs = timerLogs instanceof ConcurrentHashMap ? (ConcurrentHashMap<String, Long>) timerLogs : new ConcurrentHashMap<>(timerLogs);
    return (T) this;
  }

}
