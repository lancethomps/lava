package com.lancethomps.lava.common.sorting;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.SimpleDomainObject;

public class SortClause extends SimpleDomainObject {

  private String field;

  private SortOrder order;

  private List<Object> predefinedOrder;

  private Boolean predefinedOrderMissingLast;

  private Class<? extends Comparable<?>> sortAsType;

  private Function<? extends Object, ? extends Object> sortFieldFunction;

  public SortClause() {
    super();
  }

  public SortClause(Function<? extends Object, ? extends Object> sortFieldFunction, SortOrder order) {
    super();
    this.sortFieldFunction = sortFieldFunction;
    this.order = order;
  }

  public SortClause(String field, SortOrder order) {
    this(field, order, null);
  }

  public SortClause(String field, SortOrder order, Class<? extends Comparable<?>> sortAsType) {
    this(field, order, sortAsType, null);
  }

  public SortClause(String field, SortOrder order, Class<? extends Comparable<?>> sortAsType, List<Object> predefinedOrder) {
    super();
    this.field = field;
    this.order = order;
    this.sortAsType = sortAsType;
    this.predefinedOrder = predefinedOrder;
  }

  public static List<SortClause> fromMultiString(String sort) {
    return fromMultiString(sort, true);
  }

  public static List<SortClause> fromMultiString(String sort, boolean defaultAscending) {
    return Collect.splitCsvAsList(sort).stream().map(s -> SortClause.fromString(s, defaultAscending)).collect(Collectors.toList());
  }

  public static SortClause fromString(String sort) {
    return fromString(sort, true);
  }

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

  public String getField() {
    return field;
  }

  public SortClause setField(String field) {
    this.field = field;
    return this;
  }

  public SortOrder getOrder() {
    return order;
  }

  public SortClause setOrder(SortOrder order) {
    this.order = order;
    return this;
  }

  public List<Object> getPredefinedOrder() {
    return predefinedOrder;
  }

  public SortClause setPredefinedOrder(List<Object> predefinedOrder) {
    this.predefinedOrder = predefinedOrder;
    return this;
  }

  public Boolean getPredefinedOrderMissingLast() {
    return predefinedOrderMissingLast;
  }

  public SortClause setPredefinedOrderMissingLast(Boolean predefinedOrderMissingLast) {
    this.predefinedOrderMissingLast = predefinedOrderMissingLast;
    return this;
  }

  public Class<? extends Comparable<?>> getSortAsType() {
    return sortAsType;
  }

  public SortClause setSortAsType(Class<? extends Comparable<?>> sortAsType) {
    this.sortAsType = sortAsType;
    return this;
  }

  public Function<? extends Object, ? extends Object> getSortFieldFunction() {
    return sortFieldFunction;
  }

  public SortClause setSortFieldFunction(Function<? extends Object, ? extends Object> sortFieldFunction) {
    this.sortFieldFunction = sortFieldFunction;
    return this;
  }

  public boolean testPredefinedOrderMissingLast() {
    return (predefinedOrderMissingLast == null) || predefinedOrderMissingLast.booleanValue();
  }

}
