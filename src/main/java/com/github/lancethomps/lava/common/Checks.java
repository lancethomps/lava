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

/**
 * The Class Checks.
 *
 * @author lancethomps
 */
public class Checks {

	/** The Constant FAILURE_PAIR. */
	public static final Pair<Boolean, String> FAILURE_PAIR = Pair.of(false, null);

	/** The Constant FAILURE_PAIR_REGEX. */
	public static final Pair<Boolean, Pattern> FAILURE_PAIR_REGEX = Pair.of(false, null);

	/** The Constant SUCCESS_PAIR_REGEX. */
	public static final Pair<Boolean, Pattern> SUCCESS_PAIR_REGEX = Pair.of(true, null);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Checks.class);

	/**
	 * Assert false.
	 *
	 * @param test the test
	 * @throws AssertionError the assertion error
	 */
	public static void assertFalse(final boolean test) throws AssertionError {
		assertFalse(test, null);
	}

	/**
	 * Assert false.
	 *
	 * @param test the test
	 * @param message the message
	 * @param formatArgs the format args
	 * @throws AssertionError the assertion error
	 */
	public static void assertFalse(final boolean test, final String message, final Object... formatArgs) throws AssertionError {
		Assert.assertFalse(Formatting.getMessage(message, formatArgs), test);
	}

	/**
	 * Assert file exists.
	 *
	 * @param file the file
	 * @throws AssertionError the assertion error
	 */
	public static void assertFileExists(final File file) throws AssertionError {
		assertFileExists(file, "File [%s] does not exist.", file);
	}

	/**
	 * Assert file exists.
	 *
	 * @param file the file
	 * @param message the message
	 * @param formatArgs the format args
	 * @throws AssertionError the assertion error
	 */
	public static void assertFileExists(final File file, final String message, final Object... formatArgs) throws AssertionError {
		assertTrue((file != null) && file.isFile(), message, formatArgs);
	}

	/**
	 * Assert files exist.
	 *
	 * @param files the files
	 * @throws AssertionError the assertion error
	 */
	public static void assertFilesExist(final File... files) throws AssertionError {
		for (File file : files) {
			assertFileExists(file);
		}
	}

	/**
	 * Assert true.
	 *
	 * @param test the test
	 * @throws AssertionError the assertion error
	 */
	public static void assertTrue(final boolean test) throws AssertionError {
		assertTrue(test, null);
	}

	/**
	 * Assert true.
	 *
	 * @param test the test
	 * @param message the message
	 * @param formatArgs the format args
	 * @throws AssertionError the assertion error
	 */
	public static void assertTrue(final boolean test, final String message, final Object... formatArgs) throws AssertionError {
		Assert.assertTrue(Formatting.getMessage(message, formatArgs), test);
	}

	/**
	 * Contains with wildcards.
	 *
	 * @param wildcards the wildcards
	 * @param valToCheck the val to check
	 * @return true, if successful
	 */
	public static boolean containsWithWildcards(Collection<String> wildcards, String valToCheck) {
		if (isNotEmpty(wildcards) && (valToCheck != null)) {
			if (wildcards.contains(valToCheck)) {
				return true;
			}
			if (wildcards.stream().anyMatch(val -> wildcardMatch(valToCheck, val))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Default if blank.
	 *
	 * @param <T> the generic type
	 * @param str the str
	 * @param defaultStr the default str
	 * @return the t
	 */
	public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
		return isBlank(str) ? defaultStr : str;
	}

	/**
	 * Default if blank.
	 *
	 * @param <T> the generic type
	 * @param str the str
	 * @param defaultStrSupplier the default str supplier
	 * @return the t
	 */
	@Nonnull
	public static <T extends CharSequence> T defaultIfBlankSupplier(final T str, @Nonnull final Supplier<T> defaultStrSupplier) {
		return (str == null) || isBlank(str) ? defaultStrSupplier.get() : str;
	}

	/**
	 * Default if empty.
	 *
	 * @param <C> the generic type
	 * @param <T> the generic type
	 * @param val the val
	 * @param defaultVal the default val
	 * @return the c
	 */
	public static <C extends Collection<T>, T> C defaultIfEmpty(C val, C defaultVal) {
		return isEmpty(val) ? defaultVal : val;
	}

	/**
	 * Default if null.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param supplier the supplier
	 * @return the t
	 */
	public static <T> T defaultIfNull(T val, @Nonnull Supplier<T> supplier) {
		return val == null ? supplier.get() : val;
	}

	/**
	 * Default if null.
	 *
	 * @param <T> the generic type
	 * @param val the val
	 * @param defaultVal the default val
	 * @return the t
	 */
	public static <T> T defaultIfNull(T val, T defaultVal) {
		return val == null ? defaultVal : val;
	}

	/**
	 * Do any expressions match.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @return true, if successful
	 */
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

	/**
	 * Do any expressions match using root wrapper.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @param context the context
	 * @return true, if successful
	 */
	@Nonnull
	public static ExpressionsMatchResult doAnyExpressionsMatchUsingRootWrapper(
		@Nonnull List<OutputExpression> expressions,
		@Nullable Object rootObject,
		@Nullable Object context
	) {
		OutputExpressionRoot root = new OutputExpressionRoot(rootObject, new HashMap<>(), context);
		return doAnyExpressionsMatch(expressions, root);
	}

	/**
	 * Do any expressions match with exception.
	 *
	 * @param expressions the expressions
	 * @param rootObject the root object
	 * @return the expressions match result
	 * @throws ExpressionEvalException the expression eval exception
	 * @throws MissingRequiredFieldException the missing required field exception
	 * @throws ScriptException the script exception
	 */
	@Nonnull
	public static ExpressionsMatchResult doAnyExpressionsMatchWithException(@Nonnull List<OutputExpression> expressions, @Nullable Object rootObject) throws ExpressionEvalException,
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

	/**
	 * Filter with white and black list.
	 *
	 * @param allValues the all values
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return the list
	 */
	public static List<String> filterWithWhiteAndBlackList(
		@Nonnull Collection<String> allValues,
		@Nullable final Collection<Pattern> whiteList,
		@Nullable final Collection<Pattern> blackList
	) {
		return filterWithWhiteAndBlackList(allValues, whiteList, blackList, false);
	}

	/**
	 * Filter with white and black list.
	 *
	 * @param allValues the all values
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPriority the white list priority
	 * @return the list
	 */
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

	/**
	 * Filter with wildcards.
	 *
	 * @param allValues the all values
	 * @param includes the includes
	 * @param excludes the excludes
	 * @param useIncludesMatch the use includes match
	 * @return the list
	 */
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

	/**
	 * Find matching pattern.
	 *
	 * @param value the value
	 * @param patterns the patterns
	 * @return the optional
	 */
	public static Optional<Pattern> findMatchingPattern(@Nonnull final String value, final Collection<Pattern> patterns) {
		return patterns == null ? Optional.empty() : patterns.stream().filter(regex -> regex.matcher(value).matches()).findFirst();
	}

	/**
	 * First non blank.
	 *
	 * @param <T> the generic type
	 * @param strings the strings
	 * @return the t
	 */
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

	/**
	 * First non null.
	 *
	 * @param <T> the generic type
	 * @param values the values
	 * @return the t
	 */
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

	/**
	 * Gets the array length.
	 *
	 * @param array the array
	 * @return the array length
	 */
	public static int getArrayLength(final Object array) {
		if (array == null) {
			return 0;
		}
		return Array.getLength(array);
	}

	/**
	 * Checks if is blank.
	 *
	 * @param cs the cs
	 * @return true, if is blank
	 */
	public static boolean isBlank(final CharSequence cs) {
		return StringUtils.isBlank(cs);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final boolean[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final byte[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final char[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param coll the coll
	 * @return true, if is empty
	 */
	public static boolean isEmpty(Collection<?> coll) {
		return (coll == null) || coll.isEmpty();
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final double[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final float[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final int[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param it the it
	 * @return true, if is empty
	 */
	public static boolean isEmpty(Iterable<?> it) {
		return (it == null) || (Iterables.size(it) == 0);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final long[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param map the map
	 * @return true, if is empty
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null) || map.isEmpty();
	}

	/**
	 * Checks if is empty.
	 *
	 * @param array the array
	 * @return true, if is empty
	 */
	public static boolean isEmpty(final short[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is empty.
	 *
	 * @param <T> the generic type
	 * @param array the array
	 * @return true, if is empty
	 */
	public static <T> boolean isEmpty(final T[] array) {
		return getArrayLength(array) == 0;
	}

	/**
	 * Checks if is not blank.
	 *
	 * @param cs the cs
	 * @return true, if is not blank
	 */
	public static boolean isNotBlank(final CharSequence cs) {
		return StringUtils.isNotBlank(cs);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final boolean[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final byte[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final char[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param coll the coll
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(Collection<?> coll) {
		return !isEmpty(coll);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final double[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final float[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final int[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param it the it
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(Iterable<?> it) {
		return !isEmpty(it);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final long[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param map the map
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(final short[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is not empty.
	 *
	 * @param <T> the generic type
	 * @param array the array
	 * @return true, if is not empty
	 */
	public static <T> boolean isNotEmpty(final T[] array) {
		return !isEmpty(array);
	}

	/**
	 * Checks if is null.
	 *
	 * @param obj the obj
	 * @return true, if is null
	 */
	public static boolean isNull(Object obj) {
		return obj == null;
	}

	/**
	 * Non null.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	/**
	 * Passes white and black list check.
	 *
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return the pair
	 */
	public static Pair<Boolean, Pattern> passesWhiteAndBlackListCheck(
		@Nonnull final String value,
		@Nullable final Collection<Pattern> whiteList,
		@Nullable final Collection<Pattern> blackList
	) {
		return passesWhiteAndBlackListCheck(value, whiteList, blackList, false);
	}

	/**
	 * Passes white and black list check.
	 *
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPriority the white list priority
	 * @return true, if successful
	 */
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

	/**
	 * Passes white and black list check.
	 *
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPatterns the white list patterns
	 * @param blackListPatterns the black list patterns
	 * @param whiteListPriority the white list priority
	 * @return the pair
	 */
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

	/**
	 * Passes white and black list check.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @return true, if successful
	 */
	public static <T> boolean passesWhiteAndBlackListCheck(
		@Nonnull final T value,
		@Nullable final Collection<T> whiteList,
		@Nullable final Collection<T> blackList
	) {
		return passesWhiteAndBlackListCheck(value, whiteList, blackList, false);
	}

	/**
	 * Passes white and black list check.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPriority the white list priority
	 * @return true, if successful
	 */
	public static <T> boolean passesWhiteAndBlackListCheck(
		@Nonnull final T value,
		@Nullable final Collection<T> whiteList,
		@Nullable final Collection<T> blackList,
		final boolean whiteListPriority
	) {
		if (whiteListPriority) {
			if ((whiteList != null) && !whiteList.isEmpty()) {
				if (whiteList.contains(value)) {
					return true;
				}
				return false;
			}
			if ((blackList != null) && !blackList.isEmpty()) {
				if (blackList.contains(value)) {
					return false;
				}
			}
		} else {
			if ((blackList != null) && !blackList.isEmpty()) {
				if (blackList.contains(value)) {
					return false;
				}
			}
			if ((whiteList != null) && !whiteList.isEmpty()) {
				if (whiteList.contains(value)) {
					return true;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Regex match.
	 *
	 * @param value the value
	 * @param patterns the patterns
	 * @return true, if successful
	 */
	public static boolean regexMatch(@Nullable String value, @Nonnull Collection<Pattern> patterns) {
		return value == null ? false : passesWhiteAndBlackListCheck(value, patterns, null).getLeft();
	}

	/**
	 * Removes the matches.
	 *
	 * @param <C> the generic type
	 * @param includeVals the include vals
	 * @param excludeVals the exclude vals
	 * @param wildcards the wildcards
	 * @return the c
	 */
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

	/**
	 * Throw if fails white and black list check.
	 *
	 * @param value the value
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPriority the white list priority
	 * @throws AssertionError the assertion error
	 */
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

	/**
	 * Wildcard match.
	 *
	 * @param value the value
	 * @param wildcard the wildcard
	 * @return true, if successful
	 */
	public static boolean wildcardMatch(String value, String wildcard) {
		return FilenameUtils.wildcardMatch(value, wildcard);
	}
}
