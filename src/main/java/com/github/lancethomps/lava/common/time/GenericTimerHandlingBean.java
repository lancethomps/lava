package com.github.lancethomps.lava.common.time;

import java.util.Map;

public class GenericTimerHandlingBean implements TimerHandlingBean {

  private Map<String, Long> timerLogs;

  @Override
  public Map<String, Long> getTimerLogs() {
    return timerLogs;
  }

  @Override
  public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
    this.timerLogs = timerLogs;
    return (T) this;
  }

}
