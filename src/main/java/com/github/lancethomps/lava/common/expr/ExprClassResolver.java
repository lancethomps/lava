package com.github.lancethomps.lava.common.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import ognl.ClassResolver;

public class ExprClassResolver implements ClassResolver {

  private static Set<String> allowedExprClasses = Sets.newHashSet("java.lang.Math", "java.lang.Double");

  private static Map<String, Class<?>> classes = new HashMap<>(101);

  public ExprClassResolver() {
    super();
  }

  public static Set<String> getAllowedExprClasses() {
    return allowedExprClasses;
  }

  public static void setAllowedExprClasses(Set<String> allowedExprClasses) {
    ExprClassResolver.allowedExprClasses = allowedExprClasses;
    classes.clear();
  }

  @Override
  public Class classForName(String className, Map context) throws ClassNotFoundException {
    Class result = null;

    if ((result = classes.get(className)) == null) {
      try {
        result = allowedExprClasses.contains(className) ? Class.forName(className) : null;
      } catch (ClassNotFoundException ex) {
        if (className.indexOf('.') == -1) {
          result = Class.forName("java.lang." + className);
          classes.put("java.lang." + className, result);
        }
      }
      classes.put(className, result);
    }
    return result;
  }

}
