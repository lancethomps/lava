package com.github.lancethomps.lava.common;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;

import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.expr.ExpressionEvalException;
import com.github.lancethomps.lava.common.expr.ExpressionsMatchResult;
import com.github.lancethomps.lava.common.format.Formatting;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.MissingRequiredFieldException;
import com.github.lancethomps.lava.common.ser.OutputExpression;
import com.github.lancethomps.lava.common.ser.OutputExpressionRoot;
import com.google.common.collect.Iterables;

public class Checks {

  public static final Pair<Boolean, String> FAILURE_PAIR = Pair.of(false, null);

  public static final Pair<Boolean, Pattern> FAILURE_PAIR_REGEX = Pair.of(false, null);

  public static final Pair<Boolean, Pattern> SUCCESS_PAIR_REGEX = Pair.of(true, null);

  private static final Logger LOG = Logger.getLogger(Checks.class);

  public static void assertFalse(final boolean test) throws AssertionError {
    assertFalse(test, null);
  }

  public static void assertFalse(final boolean test, final String message, final Object... formatArgs) throws AssertionError {
    Assert.assertFalse(Formatting.getMessage(message, formatArgs), test);
  }

  public static void assertFileExists(final File file) throws AssertionError {
    assertFileExists(file, "File [%s] does not exist.", file);
  }

  public static void assertFileExists(final File file, final String message, final Object... formatArgs) throws AssertionError {
    assertTrue((file != null) && file.isFile(), message, formatArgs);
  }

  public static void assertFilesExist(final File... files) throws AssertionError {
    for (File file : files) {
      assertFileExists(file);
    }
  }

  public static void assertTrue(final boolean test) throws AssertionError {
    assertTrue(test, null);
  }

  public static void assertTrue(final boolean test, final String message, final Object... formatArgs) throws AssertionError {
    Assert.assertTrue(Formatting.getMessage(message, formatArgs), test);
  }

  public static boolean containsWithWildcards(Collection<String> wildcards, String valToCheck) {
    if (isNotEmpty(wildcards) && (valToCheck != null)) {
      if (wildcards.contains(valToCheck)) {
        return true;
      }
      return wildcards.stream().anyMatch(val -> wildcardMatch(valToCheck, val));
    }
    return false;
  }

  public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
    return isBlank(str) ? defaultStr : str;
  }

  @Nonnull
  public static <T extends CharSequence> T defaultIfBlankSupplier(final T str, @Nonnull final Supplier<T> defaultStrSupplier) {
    return (str == null) || isBlank(str) ? defaultStrSupplier.get() : str;
  }

  public static <C extends Collection<T>, T> C defaultIfEmpty(C val, C defaultVal) {
    return isEmpty(val) ? defaultVal : val;
  }

  public static <T> T defaultIfNull(T val, @Nonnull Supplier<T> supplier) {
    return val == null ? supplier.get() : val;
  }

  public static <T> T defaultIfNull(T val, T defaultVal) {
    return val == null ? defaultVal : val;
  }

  @Nonnull
  public static ExpressionsMatchResult doAnyExpressionsMatch(@Nonnull List<OutputExpression> expressions, @Nullable Object rootObject) {
    final ExpressionsMatchResult result = new ExpressionsMatchResult().setMatched(false);
    for (OutputExpression expr : ExprFactory.compileCreateExpressions(expressions, false, false, true)) {
      try {
        Object val = ExprFactory.evalWithException(rootObject, expr.getCompiledExpression(), true);
        if ((val != null) && (val instanceof Boolean) && ((Boolean) val)) {
          return result.setMatched(true).setMatchedExpression(expr);
        }
      } catch (ExpressionEvalException e) {
        Logs.logError(LOG, e, "Error while checking for matching expressions.");
        result.addError(e.getMessage());
      }
    }
    return result;
  }

  @Nonnull
  public static ExpressionsMatchResult doAnyExpressionsMatchUsingRootWrapper(
    @Nonnull List<OutputExpression> expressions,
    @Nullable Object rootObject,
    @Nullable Object context
  ) {
    OutputExpressionRoot root = new OutputExpressionRoot(rootObject, new HashMap<>(), context);
    return doAnyExpressionsMatch(expressions, root);
  }

  @Nonnull
  public static ExpressionsMatchResult doAnyExpressionsMatchWithException(@Nonnull List<OutputExpression> expressions, @Nullable Object rootObject)
    throws ExpressionEvalException,
           MissingRequiredFieldException,
           ScriptException {
    final ExpressionsMatchResult result = new ExpressionsMatchResult().setMatched(false);
    for (OutputExpression expr : ExprFactory.compileCreateExpressionsWithException(expressions, false, false, true)) {
      Object val = ExprFactory.evalWithException(rootObject, expr.getCompiledExpression(), true);
      if ((val != null) && (val instanceof Boolean) && ((Boolean) val)) {
        return result.setMatched(true).setMatchedExpression(expr);
      }
    }
    return result;
  }

  public static List<String> filterWithWhiteAndBlackList(
    @Nonnull Collection<String> allValues,
    @Nullable final Collection<Pattern> whiteList,
    @Nullable final Collection<Pattern> blackList
  ) {
    return filterWithWhiteAndBlackList(allValues, whiteList, blackList, false);
  }

  public static List<String> filterWithWhiteAndBlackList(
    @Nonnull Collection<String> allValues,
    @Nullable final Collection<Pattern> whiteList,
    @Nullable final Collection<Pattern> blackList,
    boolean whiteListPriority
  ) {
    return allValues
      .stream()
      .filter(val -> passesWhiteAndBlackListCheck(val, whiteList, blackList, whiteListPriority).getLeft())
      .collect(Collectors.toList());
  }

  public static List<String> filterWithWildcards(
    @Nonnull Collection<String> allValues,
    @Nullable Collection<String> includes,
    @Nullable Collection<String> excludes,
    boolean useIncludesMatch
  ) {
    List<String> matched;
    if (isNotEmpty(includes)) {
      if (useIncludesMatch) {
        matched = allValues.stream().filter(value -> containsWithWildcards(includes, value)).collect(Collectors.toList());
      } else {
        matched = new ArrayList<>(includes);
      }
    } else {
      matched = new ArrayList<>(allValues);
    }
    if (isNotEmpty(excludes)) {
      matched.removeIf(value -> Checks.containsWithWildcards(excludes, value));
    }
    return matched;
  }

  public static Optional<Pattern> findMatchingPattern(@Nonnull final String value, final Collection<Pattern> patterns) {
    return patterns == null ? Optional.empty() : patterns.stream().filter(regex -> regex.matcher(value).matches()).findFirst();
  }

  @SafeVarargs
  public static <T extends CharSequence> T firstNonBlank(final T... strings) {
    if (isEmpty(strings)) {
      return null;
    }
    for (T str : strings) {
      if (isNotBlank(str)) {
        return str;
      }
    }
    return null;
  }

  @SafeVarargs
  public static <T> T firstNonNull(final T... values) {
    if (isEmpty(values)) {
      return null;
    }
    for (T val : values) {
      if (val != null) {
        return val;
      }
    }
    return null;
  }

  public static int getArrayLength(final Object array) {
    if (array == null) {
      return 0;
    }
    return Array.getLength(array);
  }

  public static boolean isBlank(final CharSequence cs) {
    return StringUtils.isBlank(cs);
  }

  public static boolean isEmpty(final boolean[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(final byte[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(final char[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(Collection<?> coll) {
    return (coll == null) || coll.isEmpty();
  }

  public static boolean isEmpty(final double[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(final float[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(final int[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(Iterable<?> it) {
    return (it == null) || (Iterables.size(it) == 0);
  }

  public static boolean isEmpty(final long[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return (map == null) || map.isEmpty();
  }

  public static boolean isEmpty(final short[] array) {
    return getArrayLength(array) == 0;
  }

  public static <T> boolean isEmpty(final T[] array) {
    return getArrayLength(array) == 0;
  }

  public static boolean isNotBlank(final CharSequence cs) {
    return StringUtils.isNotBlank(cs);
  }

  public static boolean isNotEmpty(final boolean[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(final byte[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(final char[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(Collection<?> coll) {
    return !isEmpty(coll);
  }

  public static boolean isNotEmpty(final double[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(final float[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(final int[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(Iterable<?> it) {
    return !isEmpty(it);
  }

  public static boolean isNotEmpty(final long[] array) {
    return !isEmpty(array);
  }

  public static boolean isNotEmpty(Map<?, ?> map) {
    return !isEmpty(map);
  }

  public static boolean isNotEmpty(final short[] array) {
    return !isEmpty(array);
  }

  public static <T> boolean isNotEmpty(final T[] array) {
    return !isEmpty(array);
  }

  public static boolean isNull(Object obj) {
    return obj == null;
  }

  public static boolean nonNull(Object obj) {
    return obj != null;
  }

  public static Pair<Boolean, Pattern> passesWhiteAndBlackListCheck(
    @Nonnull final String value,
    @Nullable final Collection<Pattern> whiteList,
    @Nullable final Collection<Pattern> blackList
  ) {
    return passesWhiteAndBlackListCheck(value, whiteList, blackList, false);
  }

  public static Pair<Boolean, Pattern> passesWhiteAndBlackListCheck(
    @Nonnull final String value,
    @Nullable final Collection<Pattern> whiteList,
    @Nullable final Collection<Pattern> blackList,
    final boolean whiteListPriority
  ) {
    Pattern matched;
    if (whiteListPriority) {
      if ((whiteList != null) && !whiteList.isEmpty()) {
        if ((matched = findMatchingPattern(value, whiteList).orElse(null)) != null) {
          return Pair.of(true, matched);
        }
        return FAILURE_PAIR_REGEX;
      }
      if ((blackList != null) && !blackList.isEmpty()) {
        if ((matched = findMatchingPattern(value, blackList).orElse(null)) != null) {
          return Pair.of(false, matched);
        }
      }
    } else {
      if ((blackList != null) && !blackList.isEmpty()) {
        if ((matched = findMatchingPattern(value, blackList).orElse(null)) != null) {
          return Pair.of(false, matched);
        }
      }
      if ((whiteList != null) && !whiteList.isEmpty()) {
        if ((matched = findMatchingPattern(value, whiteList).orElse(null)) != null) {
          return Pair.of(true, matched);
        }
        return FAILURE_PAIR_REGEX;
      }
    }
    return SUCCESS_PAIR_REGEX;
  }

  public static Pair<Boolean, Object> passesWhiteAndBlackListCheck(
    @Nonnull final String value,
    @Nullable final Collection<String> whiteList,
    @Nullable final Collection<String> blackList,
    @Nullable final Collection<Pattern> whiteListPatterns,
    @Nullable final Collection<Pattern> blackListPatterns,
    final boolean whiteListPriority
  ) {
    Pattern matched;
    if (whiteListPriority) {
      if (isNotEmpty(whiteList)) {
        if (!whiteList.contains(value)) {
          if (isEmpty(whiteListPatterns)) {
            return Pair.of(false, null);
          }
        } else {
          return Pair.of(true, value);
        }
      }
      if (isNotEmpty(whiteListPatterns)) {
        if ((matched = findMatchingPattern(value, whiteListPatterns).orElse(null)) != null) {
          return Pair.of(true, matched);
        }
        return Pair.of(false, null);
      }
      if ((blackList != null) && blackList.contains(value)) {
        return Pair.of(false, value);
      }
      if ((matched = findMatchingPattern(value, blackListPatterns).orElse(null)) != null) {
        return Pair.of(false, matched);
      }
    } else {
      if ((blackList != null) && blackList.contains(value)) {
        return Pair.of(false, value);
      }
      if ((matched = findMatchingPattern(value, blackListPatterns).orElse(null)) != null) {
        return Pair.of(false, matched);
      }
      if (isNotEmpty(whiteList)) {
        if (!whiteList.contains(value)) {
          if (isEmpty(whiteListPatterns)) {
            return Pair.of(false, null);
          }
        } else {
          return Pair.of(true, value);
        }
      }
      if (isNotEmpty(whiteListPatterns)) {
        if ((matched = findMatchingPattern(value, whiteListPatterns).orElse(null)) != null) {
          return Pair.of(true, matched);
        }
        return Pair.of(false, null);
      }
    }
    return Pair.of(true, null);
  }

  public static <T> boolean passesWhiteAndBlackListCheck(
    @Nonnull final T value,
    @Nullable final Collection<T> whiteList,
    @Nullable final Collection<T> blackList
  ) {
    return passesWhiteAndBlackListCheck(value, whiteList, blackList, false);
  }

  public static <T> boolean passesWhiteAndBlackListCheck(
    @Nonnull final T value,
    @Nullable final Collection<T> whiteList,
    @Nullable final Collection<T> blackList,
    final boolean whiteListPriority
  ) {
    if (whiteListPriority) {
      if ((whiteList != null) && !whiteList.isEmpty()) {
        return whiteList.contains(value);
      }
      if ((blackList != null) && !blackList.isEmpty()) {
        return !blackList.contains(value);
      }
    } else {
      if ((blackList != null) && !blackList.isEmpty()) {
        if (blackList.contains(value)) {
          return false;
        }
      }
      if ((whiteList != null) && !whiteList.isEmpty()) {
        return whiteList.contains(value);
      }
    }
    return true;
  }

  public static boolean regexMatch(@Nullable String value, @Nonnull Collection<Pattern> patterns) {
    return value == null ? false : passesWhiteAndBlackListCheck(value, patterns, null).getLeft();
  }

  public static <C extends Collection<String>> C removeMatches(@Nonnull C includeVals, @Nonnull Collection<String> excludeVals, boolean wildcards) {
    if (!wildcards) {
      includeVals.removeAll(excludeVals);
      return includeVals;
    }
    includeVals.removeIf(value -> {
      for (String wildcard : excludeVals) {
        if (wildcardMatch(value, wildcard)) {
          return true;
        }
      }
      return false;
    });
    return includeVals;
  }

  public static void throwIfFailsWhiteAndBlackListCheck(
    @Nonnull final String value,
    @Nullable final Collection<Pattern> whiteList,
    @Nullable final Collection<Pattern> blackList,
    final boolean whiteListPriority
  ) throws AssertionError {
    Pair<Boolean, Pattern> result = passesWhiteAndBlackListCheck(value, whiteList, blackList, whiteListPriority);
    if (!result.getLeft()) {
      throw new AssertionError(String.format("Failed white/black list check for pattern [%s]", result.getRight()));
    }
  }

  public static boolean wildcardMatch(String value, String wildcard) {
    return FilenameUtils.wildcardMatch(value, wildcard);
  }

}
