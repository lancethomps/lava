package com.lancethomps.lava.common.logging;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.lancethomps.lava.common.time.Stopwatch;

public class TimerLogs {

  private Map<String, Stopwatch> stopWatches = new TreeMap<>();

  public TimerLogs(String key) {
    super();
    start(key);
  }

  public TimerLogs() {
    super();
  }

  public Map<String, Stopwatch> getStopwatches() {
    return stopWatches;
  }

  public Map<String, Long> getTimes() {
    Map<String, Long> times = new TreeMap<>();
    for (Entry<String, Stopwatch> entry : stopWatches.entrySet()) {
      times.put(entry.getKey(), entry.getValue().getTime());
    }
    return times;
  }

  public void start(String key) {
    if (stopWatches.containsKey(key)) {
      stopWatches.get(key).resume();
    } else {
      Stopwatch watch = new Stopwatch();
      watch.start();
      stopWatches.put(key, watch);
    }
  }

  public void stop(String key) {
    if (stopWatches.containsKey(key)) {
      stopWatches.get(key).suspend();
    }
  }

  @Override
  public String toString() {
    String msg = "" + System.lineSeparator();
    for (Entry<String, Stopwatch> entry : stopWatches.entrySet()) {
      msg = msg + entry.getValue().getTime() + "ms" + " - " + entry.getKey() + System.lineSeparator();
    }
    return msg;
  }

}
