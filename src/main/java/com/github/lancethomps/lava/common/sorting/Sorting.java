package com.github.lancethomps.lava.common.sorting;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Reflections;
import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.Serializer;

/**
 * The Class SortingUtil.
 */
public class Sorting {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Sorting.class);

	/**
	 * Creates the comparator.
	 *
	 * @param <T> the generic type
	 * @param sorts the sorts
	 * @return the comparator
	 */
	public static <T> Comparator<T> createComparator(final List<SortClause> sorts) {
		return createComparator(sorts, true, null, null);
	}

	/**
	 * Creates the comparator.
	 *
	 * @param <T> the generic type
	 * @param sorts the sorts
	 * @param defaultAscending the default ascending
	 * @param defaultSortAsType the default sort as type
	 * @param type the type
	 * @return the comparator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Comparator<T> createComparator(
		final List<SortClause> sorts,
		final boolean defaultAscending,
		final Class<? extends Comparable<?>> defaultSortAsType,
		final Class<?> type
	) {
		return (T m1, T m2) -> {
			int c = 0;
			for (SortClause clause : sorts) {
				Class<? extends Comparable<?>> sortAsType = clause.getSortAsType() == null ? defaultSortAsType : clause.getSortAsType();
				boolean ascending = (clause.getOrder() != null ? clause.getOrder() : defaultAscending ? SortOrder.asc : SortOrder.desc) == SortOrder.asc;
				Object o1;
				Object o2;
				if (clause.getSortFieldFunction() != null) {
					o1 = ((Function<Object, Object>) clause.getSortFieldFunction()).apply(m1);
					o2 = ((Function<Object, Object>) clause.getSortFieldFunction()).apply(m2);
				} else if (((m1 == null) || (m1 instanceof Map)) && ((m2 == null) || (m2 instanceof Map))) {
					o1 = m1 == null ? null : ((Map) m1).get(clause.getField());
					o2 = m2 == null ? null : ((Map) m2).get(clause.getField());
				} else {
					Method getter = Reflections.getGetterForField(type, clause.getField());
					o1 = Reflections.invokeSafely(getter, m1);
					o2 = Reflections.invokeSafely(getter, m2);
				}
				int index1 = (clause.getPredefinedOrder() != null) && (o1 != null) ? clause.getPredefinedOrder().indexOf(o1) : -1;
				int index2 = (clause.getPredefinedOrder() != null) && (o2 != null) ? clause.getPredefinedOrder().indexOf(o2) : -1;
				if ((o1 == null) && (o2 == null)) {
					c = 0;
				} else if (o1 == null) {
					c = 1;
				} else if (o2 == null) {
					c = -1;
				} else if (index1 != -1) {
					if (index2 != -1) {
						c = index1 - index2;
					} else if (clause.testPredefinedOrderMissingLast()) {
						c = -1;
					} else {
						c = 1;
					}
				} else if (index2 != -1) {
					if (index1 != -1) {
						c = index1 - index2;
					} else if (clause.testPredefinedOrderMissingLast()) {
						c = 1;
					} else {
						c = -1;
					}
				} else if (sortAsType != null) {
					Comparable<?> u1 = o1.getClass() == defaultSortAsType ? (Comparable<?>) o1 : Serializer.parseString(o1.toString(), sortAsType);
					Comparable<?> u2 = o2.getClass() == defaultSortAsType ? (Comparable<?>) o2 : Serializer.parseString(o2.toString(), sortAsType);
					if (ascending) {
						c = ((Comparable<Object>) u1).compareTo(u2);
					} else {
						c = ((Comparable<Object>) u2).compareTo(u1);
					}
				} else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
					if (ascending) {
						c = ((Comparable<Object>) o1).compareTo(o2);
					} else {
						c = ((Comparable<Object>) o2).compareTo(o1);
					}
				}
				if (c != 0) {
					break;
				}
			}
			return c;
		};
	}

	/**
	 * Sort by field.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sorts the sorts
	 * @return the list
	 */
	public static <T> List<T> sort(List<T> list, final List<SortClause> sorts) {
		return sort(list, sorts, true, null);
	}

	/**
	 * Sort by field.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sorts the sorts
	 * @param defaultAscending the default ascending
	 * @param defaultSortAsType the sort as type
	 * @return the list
	 */
	public static <T> List<T> sort(List<T> list, final List<SortClause> sorts, final boolean defaultAscending, final Class<? extends Comparable<?>> defaultSortAsType) {
		if (Checks.isEmpty(list)) {
			return list;
		}
		Class<?> type = list.get(0).getClass();
		Collections.sort(list, createComparator(sorts, defaultAscending, defaultSortAsType, type));
		return list;
	}

	/**
	 * Sort.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sorts the sorts
	 * @return the list
	 */
	public static <T> List<T> sort(List<T> list, final SortClause... sorts) {
		return sort(list, Checks.isEmpty(sorts) ? Collections.emptyList() : Arrays.asList(sorts));
	}

	/**
	 * Sort.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sortMultiString the sort multi string
	 * @return the list
	 */
	public static <T> List<T> sort(List<T> list, final String sortMultiString) {
		return sort(list, SortClause.fromMultiString(sortMultiString));
	}

	/**
	 * Sort and return.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the list
	 */
	@SuppressWarnings({ "rawtypes" })
	public static <T extends Comparable> List<T> sortAndReturn(List<T> list) {
		return sortAndReturn(list, null);
	}

	/**
	 * Sort and return.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param comparator the comparator
	 * @return the list
	 */
	@SuppressWarnings({ "rawtypes" })
	public static <T extends Comparable> List<T> sortAndReturn(List<T> list, Comparator<? super T> comparator) {
		Collections.sort(list, comparator);
		return list;
	}

	/**
	 * Sort by field.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sortingKey the sorting key
	 * @param defaultAscending the default ascending
	 * @param sortAsType the sort as type
	 * @return the list
	 */
	public static <T> List<T> sortByField(List<T> list, final String sortingKey, final boolean defaultAscending, final Class<? extends Comparable<?>> sortAsType) {
		List<SortClause> sorts = SortClause.fromMultiString(sortingKey, defaultAscending);
		return sort(list, sorts, defaultAscending, sortAsType);
	}

	/**
	 * Sort by function.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param list the list
	 * @param func the func
	 * @param ascending the ascending
	 * @return the list
	 */
	public static <T extends Object, U extends Comparable<U>> List<T> sortByFunction(final List<T> list, final Function<T, U> func, final boolean ascending) {
		if (Checks.isEmpty(list)) {
			return list;
		}
		Comparator<T> comp = (t1, t2) -> ascending ? func.apply(t1).compareTo(func.apply(t2)) : func.apply(t2).compareTo(func.apply(t1));
		Collections.sort(list, comp);
		return list;
	}

	/**
	 * Sort list of maps.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sorts the sorts
	 * @param defaultAscending the default ascending
	 * @param sortAsType the sort as type
	 * @return the list
	 */
	public static <T> List<Map<String, T>> sortListOfMaps(
		List<Map<String, T>> list,
		final List<SortClause> sorts,
		final boolean defaultAscending,
		final Class<? extends Comparable<?>> sortAsType
	) {
		Collections.sort(list, createComparator(sorts, defaultAscending, (Class<? extends Comparable<?>>) sortAsType, Map.class));
		return list;
	}

	/**
	 * Sort list of maps.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sortingKey the sorting key
	 * @param defaultAscending the default ascending
	 * @return the list
	 */
	public static <T> List<Map<String, T>> sortListOfMaps(List<Map<String, T>> list, final String sortingKey, final boolean defaultAscending) {
		return sortListOfMaps(list, sortingKey, defaultAscending, null);
	}

	/**
	 * Sort list of maps.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param list the list
	 * @param sortingKey the sorting key
	 * @param defaultAscending the default ascending
	 * @param sortAsType the sort as type
	 * @return the list
	 */
	public static <T, U> List<Map<String, T>> sortListOfMaps(
		List<Map<String, T>> list,
		final String sortingKey,
		final boolean defaultAscending,
		final Class<? extends Comparable<?>> sortAsType
	) {
		return sortListOfMaps(list, SortClause.fromMultiString(sortingKey, defaultAscending), defaultAscending, sortAsType);
	}

	/**
	 * Sort map by value.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param map the map
	 * @return the map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sort recursively.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @param sorts the sorts
	 * @param childrenExpr the children expr
	 * @return the list
	 */
	public static <T> List<T> sortRecursively(List<T> list, @Nonnull final List<SortClause> sorts, @Nonnull String childrenExpr) {
		sort(list, sorts, true, null);
		if (Checks.isNotEmpty(list)) {
			for (T val : list) {
				Object children = ExprFactory.eval(val, childrenExpr);
				if ((children != null) && (children instanceof List)) {
					sortRecursively((List<?>) children, sorts, childrenExpr);
				}
			}
		}
		return list;
	}
}
