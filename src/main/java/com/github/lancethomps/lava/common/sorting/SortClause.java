package com.github.lancethomps.lava.common.sorting;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.SimpleDomainObject;

/**
 * The Class SortClause.
 */
public class SortClause extends SimpleDomainObject {

	/** The field. */
	private String field;

	/** The order. */
	private SortOrder order;

	/** The predefined order. */
	private List<Object> predefinedOrder;

	/** The predefined order missing last. */
	private Boolean predefinedOrderMissingLast;

	/** The sort as type. */
	private Class<? extends Comparable<?>> sortAsType;

	/** The sort field function. */
	private Function<? extends Object, ? extends Object> sortFieldFunction;

	/**
	 * Instantiates a new sort clause.
	 */
	public SortClause() {
		super();
	}

	/**
	 * Instantiates a new sort clause.
	 *
	 * @param sortFieldFunction the sort field function
	 * @param order the order
	 */
	public SortClause(Function<? extends Object, ? extends Object> sortFieldFunction, SortOrder order) {
		super();
		this.sortFieldFunction = sortFieldFunction;
		this.order = order;
	}

	/**
	 * Instantiates a new sort clause.
	 *
	 * @param field the field
	 * @param order the order
	 */
	public SortClause(String field, SortOrder order) {
		this(field, order, null);
	}

	/**
	 * Instantiates a new sort clause.
	 *
	 * @param field the field
	 * @param order the order
	 * @param sortAsType the sort as type
	 */
	public SortClause(String field, SortOrder order, Class<? extends Comparable<?>> sortAsType) {
		this(field, order, sortAsType, null);
	}

	/**
	 * Instantiates a new sort clause.
	 *
	 * @param field the field
	 * @param order the order
	 * @param sortAsType the sort as type
	 * @param predefinedOrder the predefined order
	 */
	public SortClause(String field, SortOrder order, Class<? extends Comparable<?>> sortAsType, List<Object> predefinedOrder) {
		super();
		this.field = field;
		this.order = order;
		this.sortAsType = sortAsType;
		this.predefinedOrder = predefinedOrder;
	}

	/**
	 * From multi string.
	 *
	 * @param sort the sort
	 * @return the list
	 */
	public static List<SortClause> fromMultiString(String sort) {
		return fromMultiString(sort, true);
	}

	/**
	 * From multi string.
	 *
	 * @param sort the sort
	 * @param defaultAscending the default ascending
	 * @return the list
	 */
	public static List<SortClause> fromMultiString(String sort, boolean defaultAscending) {
		return Collect.splitCsvAsList(sort).stream().map(s -> SortClause.fromString(s, defaultAscending)).collect(Collectors.toList());
	}

	/**
	 * From string.
	 *
	 * @param sort the sort
	 * @return the sort clause
	 */
	public static SortClause fromString(String sort) {
		return fromString(sort, true);
	}

	/**
	 * From string.
	 *
	 * @param sort the sort
	 * @param defaultAscending the default ascending
	 * @return the sort clause
	 */
	public static SortClause fromString(String sort, boolean defaultAscending) {
		if (sort != null) {
			SortClause sortClause = new SortClause();
			String[] vals = Collect.splitCsv(StringUtils.trim(sort), ' ');
			sortClause.field = vals[0];
			if (vals.length > 1) {
				sortClause.order = SortOrder.fromString(vals[1], defaultAscending ? SortOrder.asc : SortOrder.desc);
			}
			return sortClause;
		}
		return null;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @return the order
	 */
	public SortOrder getOrder() {
		return order;
	}

	/**
	 * @return the predefinedOrder
	 */
	public List<Object> getPredefinedOrder() {
		return predefinedOrder;
	}

	/**
	 * @return the predefinedOrderMissingLast
	 */
	public Boolean getPredefinedOrderMissingLast() {
		return predefinedOrderMissingLast;
	}

	/**
	 * @return the sortAsType
	 */
	public Class<? extends Comparable<?>> getSortAsType() {
		return sortAsType;
	}

	/**
	 * @return the sortFieldFunction
	 */
	public Function<? extends Object, ? extends Object> getSortFieldFunction() {
		return sortFieldFunction;
	}

	/**
	 * Sets the field.
	 *
	 * @param field the field to set
	 * @return the sort clause
	 */
	public SortClause setField(String field) {
		this.field = field;
		return this;
	}

	/**
	 * Sets the order.
	 *
	 * @param order the order to set
	 * @return the sort clause
	 */
	public SortClause setOrder(SortOrder order) {
		this.order = order;
		return this;
	}

	/**
	 * Sets the predefined order.
	 *
	 * @param predefinedOrder the predefinedOrder to set
	 * @return the sort clause
	 */
	public SortClause setPredefinedOrder(List<Object> predefinedOrder) {
		this.predefinedOrder = predefinedOrder;
		return this;
	}

	/**
	 * Sets the predefined order missing last.
	 *
	 * @param predefinedOrderMissingLast the predefinedOrderMissingLast to set
	 * @return the sort clause
	 */
	public SortClause setPredefinedOrderMissingLast(Boolean predefinedOrderMissingLast) {
		this.predefinedOrderMissingLast = predefinedOrderMissingLast;
		return this;
	}

	/**
	 * Sets the sort as type.
	 *
	 * @param sortAsType the sortAsType to set
	 * @return the sort clause
	 */
	public SortClause setSortAsType(Class<? extends Comparable<?>> sortAsType) {
		this.sortAsType = sortAsType;
		return this;
	}

	/**
	 * Sets the sort field function.
	 *
	 * @param sortFieldFunction the sortFieldFunction to set
	 * @return the sort clause
	 */
	public SortClause setSortFieldFunction(Function<? extends Object, ? extends Object> sortFieldFunction) {
		this.sortFieldFunction = sortFieldFunction;
		return this;
	}

	/**
	 * Test predefined order missing last.
	 *
	 * @return true, if successful
	 */
	public boolean testPredefinedOrderMissingLast() {
		return (predefinedOrderMissingLast == null) || predefinedOrderMissingLast.booleanValue();
	}
}
