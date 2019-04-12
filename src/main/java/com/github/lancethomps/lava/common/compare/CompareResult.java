package com.github.lancethomps.lava.common.compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.merge.Merges;

public class CompareResult extends SimpleDomainObject {

  private static final long serialVersionUID = 1L;

  private Set<String> addedFields;

  private Boolean allNewData;

  private ObjectNode differences;

  private Set<String> fieldsWithDifferences;

  private Set<String> removedFields;

  private Set<String> updatedFields;

  public static CompareResult createAndInitialize() {
    return new CompareResult()
      .setAddedFields(new LinkedHashSet<>())
      .setDifferences(Merges.createNewObjectNode())
      .setFieldsWithDifferences(new LinkedHashSet<>())
      .setRemovedFields(new LinkedHashSet<>())
      .setUpdatedFields(new LinkedHashSet<>());
  }

  public static CompareResult createWithNewNode(@Nonnull CompareResult other, @Nonnull ObjectNode node) {
    return new CompareResult()
      .setAddedFields(other.getAddedFields())
      .setDifferences(node)
      .setFieldsWithDifferences(other.getFieldsWithDifferences())
      .setRemovedFields(other.getRemovedFields())
      .setUpdatedFields(other.getUpdatedFields());
  }

  public CompareResult addAddedFields(@Nonnull Collection<String> fields) {
    return addToFieldsWithDifferences(fields, this::getAddedFields, this::setAddedFields);
  }

  public CompareResult addAddedFields(@Nonnull String... fields) {
    return addAddedFields(Arrays.asList(fields));
  }

  public CompareResult addRemovedFields(@Nonnull Collection<String> fields) {
    return addToFieldsWithDifferences(fields, this::getRemovedFields, this::setRemovedFields);
  }

  public CompareResult addRemovedFields(@Nonnull String... fields) {
    return addRemovedFields(Arrays.asList(fields));
  }

  public CompareResult addUpdatedFields(@Nonnull Collection<String> fields) {
    return addToFieldsWithDifferences(fields, this::getUpdatedFields, this::setUpdatedFields);
  }

  public CompareResult addUpdatedFields(@Nonnull String... fields) {
    return addUpdatedFields(Arrays.asList(fields));
  }

  public Set<String> getAddedFields() {
    return addedFields;
  }

  public CompareResult setAddedFields(Set<String> addedFields) {
    this.addedFields = addedFields;
    return this;
  }

  public Boolean getAllNewData() {
    return allNewData;
  }

  public CompareResult setAllNewData(Boolean allNewData) {
    this.allNewData = allNewData;
    return this;
  }

  public ObjectNode getDifferences() {
    return differences;
  }

  public CompareResult setDifferences(ObjectNode differences) {
    this.differences = differences;
    return this;
  }

  public Set<String> getFieldsWithDifferences() {
    return fieldsWithDifferences;
  }

  public CompareResult setFieldsWithDifferences(Set<String> fieldsWithDifferences) {
    this.fieldsWithDifferences = fieldsWithDifferences;
    return this;
  }

  public Set<String> getRemovedFields() {
    return removedFields;
  }

  public CompareResult setRemovedFields(Set<String> removedFields) {
    this.removedFields = removedFields;
    return this;
  }

  public Set<String> getUpdatedFields() {
    return updatedFields;
  }

  public CompareResult setUpdatedFields(Set<String> updatedFields) {
    this.updatedFields = updatedFields;
    return this;
  }

  public boolean testAllNewData() {
    return (allNewData != null) && allNewData.booleanValue();
  }

  private CompareResult addToFieldsWithDifferences(
    @Nonnull Collection<String> fields,
    @Nonnull Supplier<Set<String>> getter,
    @Nonnull Consumer<Set<String>> setter
  ) {
    Set<String> current = getter.get();
    if (current == null) {
      setter.accept(new LinkedHashSet<>());
      current = getter.get();
    }
    current.addAll(fields);
    if (fieldsWithDifferences == null) {
      fieldsWithDifferences = new LinkedHashSet<>();
    }
    fieldsWithDifferences.addAll(fields);
    return this;
  }

}
