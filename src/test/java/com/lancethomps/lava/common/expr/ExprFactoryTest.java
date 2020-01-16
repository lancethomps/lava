package com.lancethomps.lava.common.expr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;
import com.lancethomps.lava.common.Randoms;
import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.logging.Logs;

public class ExprFactoryTest extends BaseTest {

  private static final Logger LOG = LogManager.getLogger(ExprFactoryTest.class);

  private static double mathFunctionsAllowedDelta = 0.5d;

  private static int mathFunctionsVals = 20;

  private static Map<String, Object> root;

  private static Map<String, Object> getExprRoot() {
    if (root != null) {
      return root;
    }
    List<Number> vals = new ArrayList<>();
    for (int pos = 0; pos < mathFunctionsVals; pos++) {
      vals.add(Randoms.randomInt(-100, 100));
    }
    root = MapUtil.createFrom("intList", vals, "x", 1, "y", 2);
    Logs.logDebug(LOG, "Script engine root: %s", root);
    return root;
  }

  @Test
  public void testJsEngineJavaMathFunctions() throws Exception {
    Map<String, Object> root = getExprRoot();
    List<Number> vals = MapUtil.getFromMap(root, "intList");
    Assert.assertEquals(
      ExprFunctions.average(vals),
      ExprFactory.eval(ExprParser.JS, root, "average(root.intList)", false, true),
      mathFunctionsAllowedDelta
    );
    Assert.assertEquals(
      ExprFunctions.max(vals),
      ExprFactory.eval(ExprParser.JS, root, "max(root.intList)", false, true),
      mathFunctionsAllowedDelta
    );
    Assert.assertEquals(
      ExprFunctions.min(vals),
      ExprFactory.eval(ExprParser.JS, root, "min(root.intList)", false, true),
      mathFunctionsAllowedDelta
    );
    Assert.assertEquals(
      ExprFunctions.sum(vals),
      ExprFactory.eval(ExprParser.JS, root, "sum(root.intList)", false, true),
      mathFunctionsAllowedDelta
    );
  }

  @Test
  public void testJsEngineVariableAccess() throws Exception {
    Map<String, Object> root = getExprRoot();
    Assert.assertEquals(
      (Integer) root.get("x") + (Integer) root.get("y"),
      ExprFactory.eval(ExprParser.JS, root, "root.x + root.y", false, true),
      0.01d
    );
  }

  @Test
  public void testPyEngineVariableAccess() throws Exception {
    Map<String, Object> root = getExprRoot();
    Assert
      .assertEquals(
        (Integer) root.get("x") + (Integer) root.get("y"),
        ((Number) ExprFactory.eval(ExprParser.PY, root, "result = root['x'] + root['y']", false, true)).doubleValue(),
        0.01d
      );
  }

  @Test
  public void testSpelExprFunctions() throws Exception {
    Map<String, Object> root = getExprRoot();
    List<Number> vals = MapUtil.getFromMap(root, "intList");
    List<Method> methodsToTest = Stream
      .of(ExprFunctions.class.getDeclaredMethods())
      .filter(m -> (ArrayUtils.getLength(m.getParameterTypes()) > 0) && (m.getParameterTypes()[0] == Collection.class))
      .filter(m -> (m.getReturnType() == Double.TYPE) || (m.getReturnType() == Double.class))
      .collect(Collectors.toList());
    for (Method method : methodsToTest) {
      double expected = (double) method.invoke(null, vals);
      double actual = ExprFactory.eval(ExprParser.SPEL, root, '#' + method.getName() + "(['intList'])", false, true);
      Assert.assertEquals(expected, actual, mathFunctionsAllowedDelta);
    }
    String envProp = "HOME";
    Assert.assertEquals(
      "parseAndReplaceWithProps did not run correctly",
      StringUtils.trimToNull(System.getenv(envProp)),
      StringUtils.trimToNull(ExprFactory.eval(ExprParser.SPEL, null, String.format("#parseAndReplaceWithProps('${env:%s}')", envProp), false, true))
    );
  }

}
