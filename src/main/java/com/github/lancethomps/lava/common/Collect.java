// CHECKSTYLE.OFF: OpenCSV
package com.github.lancethomps.lava.common;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.sorting.CaseInsensitiveStringSort;
import com.github.lancethomps.lava.common.sorting.Sorting;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.collect.Lists;
import com.opencsv.CSVParser;

/**
 * The Class ArrayUtil.
 */
public class Collect {

	/** The Constant DEFAULT_ESC. */
	public static final char DEFAULT_ESC = '\\';

	/** The Constant DEFAULT_KEYVAL_SEP. */
	public static final char DEFAULT_KEYVAL_SEP = '~';

	/** The Constant DEFAULT_PARAM_SEP. */
	public static final char DEFAULT_PARAM_SEP = '|';

	/** The Constant DEFAULT_QUOTE. */
	public static final char DEFAULT_QUOTE = '"';

	/** The Constant DEFAULT_SEP. */
	public static final char DEFAULT_SEP = ',';

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Collect.class);

	/**
	 * Adds the all.
	 *
	 * @param <C> the generic type
	 * @param <E> the element type
	 * @param collection the collection
	 * @param other the other
	 * @return the c
	 */
	public static <C extends Collection<E>, E> C addAll(final C collection, final C other) {
		if (other != null) {
			collection.addAll(other);
		}
		return collection;
	}

	/**
	 * Adds the all.
	 *
	 * @param <C> the generic type
	 * @param <E> the element type
	 * @param collection the collection
	 * @param others the others
	 * @return the c
	 */
	public static <C extends Collection<E>, E> C addAll(final C collection, final E... others) {
		if (Checks.isNotEmpty(others)) {
			collection.addAll(Arrays.asList(others));
		}
		return collection;
	}

	/**
	 * Break up into distinct combos.
	 *
	 * @param <C> the generic type
	 * @param <E> the element type
	 * @param collection the collection
	 * @param desiredSubListSize the desired sub list size
	 * @return the list
	 */
	public static <C extends Collection<E>, E> List<List<E>> breakUpIntoDistinctCombos(@Nonnull final C collection, final int desiredSubListSize) {
		if (collection.size() <= desiredSubListSize) {
			final List<E> singleSubList = collection instanceof List ? (List<E>) collection : new ArrayList<>(collection);
			final List<List<E>> singleSubListWrapper = new ArrayList<>(1);
			singleSubListWrapper.add(singleSubList);
			return singleSubListWrapper;
		}
		final List<List<E>> allCombos = new ArrayList<>(collection.size() * 2);
		final List<List<E>> subLists = breakUpList(collection, Math.floorDiv(desiredSubListSize, 2));
		for (int subListIdx = 0; subListIdx < (subLists.size() - 1); subListIdx++) {
			for (int otherSubListIdx = (subListIdx + 1); otherSubListIdx < subLists.size(); otherSubListIdx++) {
				final List<E> subList = new ArrayList<>(subLists.get(subListIdx));
				subList.addAll(subLists.get(otherSubListIdx));
				allCombos.add(subList);
			}
		}
		return allCombos;
	}

	/**
	 * Break up list.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param size the size
	 * @return the list
	 */
	public static <T> List<List<T>> breakUpList(Collection<T> list, int size) {
		List<List<T>> lists = Lists.newArrayList();
		List<T> breakUp = Lists.newArrayList(list);
		while (!breakUp.isEmpty()) {
			List<T> sub = breakUp.subList(0, Math.min(breakUp.size(), size));
			lists.add(Lists.newArrayList(sub));
			sub.clear();
		}
		return lists;
	}

	/**
	 * Break up list by total sub lists.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param totalSubLists the total sub lists
	 * @return the list
	 */
	public static <T> List<List<T>> breakUpListByTotalSubLists(List<T> list, int totalSubLists) {
		List<List<T>> lists = Lists.newArrayList();
		List<T> breakUp = Lists.newArrayList(list);
		int size = Double.valueOf(Math.ceil((double) list.size() / (double) totalSubLists)).intValue();
		while (!breakUp.isEmpty()) {
			List<T> sub = breakUp.subList(0, Math.min(breakUp.size(), size));
			lists.add(Lists.newArrayList(sub));
			sub.clear();
		}
		return lists;
	}

	/**
	 * Creates the ordered map.
	 *
	 * @param <V> the value type
	 * @param current the current
	 * @return the tree map
	 */
	public static <V> TreeMap<String, V> createOrderedMap(Map<String, V> current) {
		TreeMap<String, V> map = new TreeMap<>(CaseInsensitiveStringSort.INSTANCE);
		if (current != null) {
			map.putAll(current);
		}
		return map;
	}

	/**
	 * Gets the csv parser.
	 *
	 * @param sep the sep
	 * @return the csv parser
	 */
	public static CSVParser getCsvParser(char sep) {
		return new CSVParser(sep, DEFAULT_QUOTE, DEFAULT_ESC, false, true);
	}

	/**
	 * Gets the random from list.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the random from list
	 */
	public static <T> T getRandomFromList(List<T> list) {
		if (isNotEmpty(list)) {
			int index = (int) Math.floor(Math.random() * list.size());
			return list.get(index);
		}
		return null;
	}

	/**
	 * Gets the sub list of desired size.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param desiredSize the desired size
	 * @return the sub list of desired size
	 */
	public static <T> List<T> getSubListOfDesiredSize(List<T> list, int desiredSize) {
		return list.subList(0, Math.min(list.size(), desiredSize));
	}

	/**
	 * Merge.
	 *
	 * @param <C> the generic type
	 * @param <T> the generic type
	 * @param type the type
	 * @param collections the collections
	 * @return the c
	 */
	@SafeVarargs
	public static <C extends Collection<T>, T> C merge(Class<C> type, Collection<T>... collections) {
		if ((collections != null) && (collections.length > 0)) {
			try {
				C main = ClassUtil.createInstance(type, false);
				for (Collection<T> collection : collections) {
					CollectionUtils.addAll(main, collection);
				}
				return main;
			} catch (IllegalArgumentException e) {
				Logs.logError(LOG, e, "Couldn't construct a new collection of type [%s]", type);
			}
		}
		return null;
	}

	/**
	 * Merge.
	 *
	 * @param <T> the generic type
	 * @param collections the collections
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> List<T> merge(Collection<T>... collections) {
		return merge(ArrayList.class, collections);
	}

	/**
	 * Merge all.
	 *
	 * @param <K> the key type
	 * @param <C> the generic type
	 * @param <V> the value type
	 * @param existing the existing
	 * @param other the other
	 * @return the map
	 */
	public static <K, C extends Collection<V>, V> Map<K, C> mergeAll(Map<K, C> existing, Map<K, C> other) {
		return mergeAll(existing, other, Collect::addAll);
	}

	/**
	 * Merge all.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param existing the existing
	 * @param other the other
	 * @param remappingFunction the remapping function
	 * @return the map
	 */
	public static <K, V> Map<K, V> mergeAll(Map<K, V> existing, Map<K, V> other, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		if ((existing != null) && (other != null)) {
			other.forEach((key, val) -> existing.merge(key, val, remappingFunction));
		}
		return existing;
	}

	/**
	 * Pairs to map.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the collector
	 */
	public static <K, V> Collector<Pair<K, V>, ?, Map<K, V>> pairsToMap() {
		return Collectors.toMap(Pair::getLeft, Pair::getRight, (a, b) -> a, HashMap::new);
	}

	/**
	 * Reverse.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param createNew the create new
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> reverse(List<T> list, boolean createNew) {
		if (list == null) {
			return list;
		}
		if (createNew) {
			List<T> newList;
			try {
				newList = ClassUtil.createInstance(list.getClass(), false);
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue creating new list of type [%s]", list.getClass());
				newList = new ArrayList<>();
			}
			newList.addAll(list);
			list = newList;
		}
		Collections.reverse(list);
		return list;
	}

	/**
	 * Sort.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the list
	 */
	@SuppressWarnings({ "rawtypes" })
	public static <T extends Comparable> List<T> sort(List<T> list) {
		return Sorting.sortAndReturn(list);
	}

	/**
	 * Split csv.
	 *
	 * @param line the line
	 * @return the string[]
	 */
	public static String[] splitCsv(String line) {
		return splitCsv(line, new CSVParser(DEFAULT_SEP, DEFAULT_QUOTE, DEFAULT_ESC, false, true));
	}

	/**
	 * Split csv.
	 *
	 * @param line the line
	 * @param sep the sep
	 * @return the string[]
	 */
	public static String[] splitCsv(String line, char sep) {
		return splitCsv(line, new CSVParser(sep, DEFAULT_QUOTE, DEFAULT_ESC, false, true));
	}

	/**
	 * Split csv.
	 *
	 * @param line the line
	 * @param parser the parser
	 * @return the string[]
	 */
	public static String[] splitCsv(String line, CSVParser parser) {
		try {
			return splitCsvWithException(line, parser);
		} catch (IOException e) {
			Logs.logError(LOG, e, "Could not split line: line=%s parser=%s", Logs.getSplunkValueString(line), Serializer.toLogString(parser));
		}
		return new String[] {};
	}

	/**
	 * Split csv as list.
	 *
	 * @param line the line
	 * @return the list
	 */
	public static List<String> splitCsvAsList(String line) {
		return Lists.newArrayList(splitCsv(line));
	}

	/**
	 * Split csv as list.
	 *
	 * @param line the line
	 * @param sep the sep
	 * @return the list
	 */
	public static List<String> splitCsvAsList(String line, char sep) {
		return newArrayList(splitCsv(line, sep));
	}

	/**
	 * Split csv as list.
	 *
	 * @param line the line
	 * @param parser the parser
	 * @return the list
	 */
	public static List<String> splitCsvAsList(String line, CSVParser parser) {
		return newArrayList(splitCsv(line, parser));
	}

	/**
	 * Split csv with exception.
	 *
	 * @param line the line
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String[] splitCsvWithException(String line) throws IOException {
		return splitCsvWithException(line, new CSVParser(DEFAULT_SEP, DEFAULT_QUOTE, DEFAULT_ESC, false, true));
	}

	/**
	 * Split csv with exception.
	 *
	 * @param line the line
	 * @param sep the sep
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String[] splitCsvWithException(String line, char sep) throws IOException {
		return splitCsvWithException(line, new CSVParser(sep, DEFAULT_QUOTE, DEFAULT_ESC, false, true));
	}

	/**
	 * Split csv with exception.
	 *
	 * @param line the line
	 * @param parser the parser
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String[] splitCsvWithException(String line, CSVParser parser) throws IOException {
		return parser.parseLine(line);
	}

	/**
	 * Split number csv.
	 *
	 * @param <T> the generic type
	 * @param line the line
	 * @param clazz the clazz
	 * @return the t[]
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T[] splitNumberCsv(String line, Class<T> clazz) {
		String[] strings = splitCsv(line);
		T[] numberArray = (T[]) Array.newInstance(clazz, strings.length);
		try {
			Constructor<T> constructor = clazz.getConstructor(String.class);
			for (int i = 0; i < strings.length; i++) {
				numberArray[i] = constructor.newInstance(strings[i]);
			}
		} catch (Exception e) {
			Logs.logError(LOG, e, "Could not split number line [%s] for class [%s]!", line, clazz);
		}
		return numberArray;
	}

	/**
	 * Split number csv as list.
	 *
	 * @param <T> the generic type
	 * @param line the line
	 * @param clazz the clazz
	 * @return the list
	 */
	public static <T extends Number> List<T> splitNumberCsvAsList(String line, Class<T> clazz) {
		return newArrayList(splitNumberCsv(line, clazz));
	}

	/**
	 * Translate commandline.
	 *
	 * @param toProcess the to process
	 * @return the string[]
	 */
	public static String[] translateCommandline(String toProcess) {
		if ((toProcess == null) || (toProcess.length() == 0)) {
			// no command? no string
			return new String[0];
		}
		// parse with a simple finite state machine

		final int normal = 0;
		final int inQuote = 1;
		final int inDoubleQuote = 2;
		int state = normal;
		final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
		final ArrayList<String> result = new ArrayList<>();
		final StringBuilder current = new StringBuilder();
		boolean lastTokenHasBeenQuoted = false;

		while (tok.hasMoreTokens()) {
			String nextTok = tok.nextToken();
			switch (state) {
			case inQuote:
				if ("\'".equals(nextTok)) {
					lastTokenHasBeenQuoted = true;
					state = normal;
				} else {
					current.append(nextTok);
				}
				break;
			case inDoubleQuote:
				if ("\"".equals(nextTok)) {
					lastTokenHasBeenQuoted = true;
					state = normal;
				} else {
					current.append(nextTok);
				}
				break;
			default:
				if ("\'".equals(nextTok)) {
					state = inQuote;
				} else if ("\"".equals(nextTok)) {
					state = inDoubleQuote;
				} else if (" ".equals(nextTok)) {
					if (lastTokenHasBeenQuoted || (current.length() != 0)) {
						result.add(current.toString());
						current.setLength(0);
					}
				} else {
					current.append(nextTok);
				}
				lastTokenHasBeenQuoted = false;
				break;
			}
		}
		if (lastTokenHasBeenQuoted || (current.length() != 0)) {
			result.add(current.toString());
		}
		if ((state == inQuote) || (state == inDoubleQuote)) {
			throw new IllegalArgumentException("unbalanced quotes in " + toProcess);
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Transpose.
	 *
	 * @param <T> the generic type
	 * @param matrix the matrix
	 * @return the t[][]
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[][] transpose(T[][] matrix) {
		if (matrix == null) {
			return null;
		} else if (matrix.length == 0) {
			return matrix;
		}
		Class<T> type = null;
		for (T[] sub : matrix) {
			if ((sub != null) && (sub.length > 0)) {
				type = (Class<T>) sub[0].getClass();
				break;
			}
		}
		return transpose(matrix, type);
	}

	/**
	 * Transpose.
	 *
	 * @param <T> the generic type
	 * @param matrix the matrix
	 * @param type the type
	 * @return the t[][]
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[][] transpose(T[][] matrix, Class<T> type) {
		int m = matrix.length;
		int n = Stream.of(matrix).mapToInt(d -> d.length).max().orElse(0);
		T[][] trasposedMatrix = (T[][]) Array.newInstance(type, n, m);
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				T[] sub = matrix[y];
				if (sub.length > x) {
					trasposedMatrix[x][y] = sub[x];
				}
			}
		}

		return trasposedMatrix;
	}

	/**
	 * Wildcard get.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param key the key
	 * @return the t
	 */
	public static <T> T wildcardGet(@Nonnull Map<String, T> map, @Nonnull String key) {
		return wildcardGet(map, key, true);
	}

	/**
	 * Gets the wildcard match.
	 *
	 * @param <T> the generic type
	 * @param map the map
	 * @param key the key
	 * @param checkExact the check exact
	 * @return the wildcard match
	 */
	public static <T> T wildcardGet(@Nonnull Map<String, T> map, @Nonnull String key, boolean checkExact) {
		T val;
		if (checkExact && ((val = map.get(key)) != null)) {
			return val;
		}
		for (Entry<String, T> entry : map.entrySet()) {
			if (FilenameUtils.wildcardMatch(key, entry.getKey())) {
				Logs.logTrace(LOG, "Map key wildcard match found for key [%s] matching map wildcard key [%s].", key, entry.getKey());
				return entry.getValue();
			}
		}
		return null;
	}
}
