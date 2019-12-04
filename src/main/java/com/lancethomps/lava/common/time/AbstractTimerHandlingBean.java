package com.lancethomps.lava.common.time;

import java.util.Map;

public abstract class AbstractTimerHandlingBean implements TimerHandlingBean {

  private Map<String, Long> timerLogs;

  @Override
  public Map<String, Long> getTimerLogs() {
    return timerLogs;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
    this.timerLogs = timerLogs;
    return (T) this;
  }

}
