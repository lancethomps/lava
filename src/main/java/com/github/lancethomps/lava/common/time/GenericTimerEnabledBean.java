package com.github.lancethomps.lava.common.time;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericTimerEnabledBean implements TimerEnabledBean {

  @JsonIgnore
  private Map<String, Stopwatch> watches;

  @Override
  public Map<String, Stopwatch> getWatches() {
    return watches;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
    this.watches = watches;
    return (T) this;
  }

}
