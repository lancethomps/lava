package com.lancethomps.lava.common.time;

import org.junit.Assert;
import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;

public class TimingTest extends BaseTest {

  @Test
  public void testElapsedTimeString() throws Exception {
    Assert.assertEquals("1 sec", Timing.getElapsedTimeString(1200L));
    Assert.assertEquals(
      "1 days, 1 hours, 10 min, 1 sec",
      Timing.getElapsedTimeString(Timing.ONE_DAY_MS + Timing.ONE_HOUR_MS + (Timing.ONE_MIN_MS * 10) + 1200L)
    );
  }

}
