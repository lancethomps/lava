package com.lancethomps.lava.common.date;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;
import com.lancethomps.lava.common.Randoms;
import com.lancethomps.lava.common.TestingCommon;
import com.lancethomps.lava.common.logging.Logs;

public class RelativeDateTest extends BaseTest {

  private static final Logger LOG = Logger.getLogger(RelativeDateTest.class);

  @Test
  public void testRandomRelativeDate() throws Exception {
    final int opsPerIteration = 20;
    final List<RelativeDateOperator> validRandomOps =
      Stream.of(RelativeDateOperator.values()).filter(op -> op != RelativeDateOperator.SETTING).collect(Collectors.toList());
    final List<String> zoneShortIds = new ArrayList<>(Dates.SHORT_IDS.keySet());
    final List<String> calNames = Arrays.asList("US", "GB", "CA");
    TestingCommon.testConsumer(1000, num -> {
      List<Triple<RelativeDateOperator, RelativeDateType, String>> info = new ArrayList<>();
      String date = 'T' + IntStream
        .iterate(0, i -> i + 1)
        .limit(opsPerIteration)
        .mapToObj(i -> {
          RelativeDateType type = Randoms.createRandomEnum(RelativeDateType.class);
          RelativeDateOperator op;
          if ((type.getUnit() == null) && (type != RelativeDateType.q)) {
            op = RelativeDateOperator.SETTING;
          } else {
            op = Randoms.getRandomFromCollection(validRandomOps);
          }
          String value;
          if (op == RelativeDateOperator.ROUND) {
            value = "";
          } else if (op == RelativeDateOperator.SETTING) {
            if (type == RelativeDateType.z) {
              value = zoneShortIds.get(Randoms.randomInt(0, zoneShortIds.size() - 2));
            } else {
              value = Randoms.getRandomFromCollection(calNames);
            }
          } else {
            value = String.valueOf(Randoms.randomInt(1, 10));
          }
          info.add(Triple.of(op, type, value));
          return op.getSymbol() + value + type.name();
        })
        .collect(Collectors.joining());
      Logs.logTrace(LOG, "%s", date);
      Pair<LocalDateTime, List<Triple<RelativeDateOperator, RelativeDateType, String>>> parsed = Dates.parseRelativeDateUnsafeWithInfo(date);
      Assert.assertThat(
        parsed.getRight(),
        IsIterableContainingInOrder.contains(info.toArray())
      );
      Logs.logTrace(LOG, "%1$-" + (opsPerIteration * 5) + "s ==> %2$s", date, parsed.getLeft());
    });
  }

}
