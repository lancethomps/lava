package com.github.lancethomps.lava.common.expr.spel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.ClassUtils;

import com.github.lancethomps.lava.common.collections.FastHashMap;

public class NonSandboxedSpelTypeLocator extends StandardTypeLocator {

  private final ClassLoader classLoader;

  private final List<String> knownPackagePrefixes = new LinkedList<>();

  private final Map<String, Class<?>> typeLookupCache = new FastHashMap<>();

  public NonSandboxedSpelTypeLocator() {
    classLoader = ClassUtils.getDefaultClassLoader();
    registerImport("java.lang");
  }

  @Override
  public Class<?> findType(String typeName) throws EvaluationException {
    Class<?> type;
    if (typeName.contains(".")) {
      type = findTypeByName(typeName);
      if (type == null) {
        type = findTypeByPrefixes(typeName);
      }
    } else {
      type = typeLookupCache.computeIfAbsent(typeName, k -> {
        Class<?> foundType = findTypeByPrefixes(typeName);
        if (foundType == null) {
          foundType = findTypeByName(typeName);
        }
        return foundType;
      });
    }
    if (type != null) {
      return type;
    }
    throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
  }

  @Override
  public List<String> getImportPrefixes() {
    return Collections.unmodifiableList(knownPackagePrefixes);
  }

  @Override
  public void registerImport(String prefix) {
    if (knownPackagePrefixes != null) {
      knownPackagePrefixes.add(prefix);
    }
  }

  @Override
  public void removeImport(String prefix) {
    knownPackagePrefixes.remove(prefix);
  }

  private Class<?> findTypeByName(String typeName) {
    try {
      return ClassUtils.forName(typeName, classLoader);
    } catch (ClassNotFoundException ey) {

    }
    return null;
  }

  private Class<?> findTypeByPrefixes(String typeName) {
    for (String prefix : knownPackagePrefixes) {
      try {
        return ClassUtils.forName(prefix + '.' + typeName, classLoader);
      } catch (ClassNotFoundException ex) {

      }
    }
    return null;
  }

}
