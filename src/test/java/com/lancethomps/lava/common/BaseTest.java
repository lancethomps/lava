package com.lancethomps.lava.common;

import java.io.File;

import org.junit.Rule;

import com.google.common.collect.Sets;
import com.lancethomps.lava.common.ser.jackson.CustomDeserializationProblemHandler;

public class BaseTest {

  static {
    try {
      if (System.getProperty("PROJ_DIR") != null) {
        Testing.setProjRootPath(System.getProperty("PROJ_DIR") + File.separatorChar);
      }
      if (Checks.isEmpty(CustomDeserializationProblemHandler.getIgnoreProperties())) {
        CustomDeserializationProblemHandler.setIgnoreProperties(Sets.newHashSet("@type"));
      }
    } catch (Throwable e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Rule
  public final ThreadNameTestWatcher threadNameWatcher = new ThreadNameTestWatcher();

}
