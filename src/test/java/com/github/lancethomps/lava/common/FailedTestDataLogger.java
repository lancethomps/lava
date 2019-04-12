package com.github.lancethomps.lava.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;

public class FailedTestDataLogger extends TestWatcher {

  private static final Logger LOG = Logger.getLogger(FailedTestDataLogger.class);

  private final Map<Object, Object> data = new LinkedHashMap<>();

  private final LinkedBlockingDeque<Object> dataList = new LinkedBlockingDeque<>();

  public void addData(Object value) {
    dataList.add(value);
  }

  public void addData(Object key, Object value) {
    synchronized (data) {
      if (data.containsKey(key)) {
        data.put(key.toString() + '.' + LocalDateTime.now().toString(), value);
      } else {
        data.put(key, value);
      }
    }
  }

  @Override
  protected void failed(Throwable e, Description description) {
    synchronized (data) {
      if (!data.isEmpty()) {
        Logs.logErrorWithoutKeeping(LOG, null, "Failure data: %s", Serializer.toPrettyJson(data));
        data.clear();
      }
    }
    List<Object> tempList = new ArrayList<>();
    dataList.drainTo(tempList);
    if (!tempList.isEmpty()) {
      Logs.logErrorWithoutKeeping(LOG, null, "Failure data list: %s", Serializer.toPrettyJson(tempList));
    }
  }

}
