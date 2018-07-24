package com.github.lancethomps.lava.common.compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.SimpleDomainObject;
import com.github.lancethomps.lava.common.merge.Merges;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class CompareResult.
 */
public class CompareResult extends SimpleDomainObject {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The added fields. */
	private Set<String> addedFields;

	/** The all new data. */
	private Boolean allNewData;

	/** The differences. */
	private ObjectNode differences;

	/** The fields with differences. */
	private Set<String> fieldsWithDifferences;

	/** The removed fields. */
	private Set<String> removedFields;

	/** The updated fields. */
	private Set<String> updatedFields;

	/**
	 * Creates the and initialize.
	 *
	 * @return the compare result
	 */
	public static CompareResult createAndInitialize() {
		return new CompareResult()
			.setAddedFields(new LinkedHashSet<>())
			.setDifferences(Merges.createNewObjectNode())
			.setFieldsWithDifferences(new LinkedHashSet<>())
			.setRemovedFields(new LinkedHashSet<>())
			.setUpdatedFields(new LinkedHashSet<>());
	}

	/**
	 * Creates the with new node.
	 *
	 * @param other the other
	 * @param node the node
	 * @return the compare result
	 */
	public static CompareResult createWithNewNode(@Nonnull CompareResult other, @Nonnull ObjectNode node) {
		return new CompareResult()
			.setAddedFields(other.getAddedFields())
			.setDifferences(node)
			.setFieldsWithDifferences(other.getFieldsWithDifferences())
			.setRemovedFields(other.getRemovedFields())
			.setUpdatedFields(other.getUpdatedFields());
	}

	/**
	 * Adds the added fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addAddedFields(@Nonnull Collection<String> fields) {
		return addToFieldsWithDifferences(fields, this::getAddedFields, this::setAddedFields);
	}

	/**
	 * Adds the added fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addAddedFields(@Nonnull String... fields) {
		return addAddedFields(Arrays.asList(fields));
	}

	/**
	 * Adds the removed fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addRemovedFields(@Nonnull Collection<String> fields) {
		return addToFieldsWithDifferences(fields, this::getRemovedFields, this::setRemovedFields);
	}

	/**
	 * Adds the removed fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addRemovedFields(@Nonnull String... fields) {
		return addRemovedFields(Arrays.asList(fields));
	}

	/**
	 * Adds the updated fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addUpdatedFields(@Nonnull Collection<String> fields) {
		return addToFieldsWithDifferences(fields, this::getUpdatedFields, this::setUpdatedFields);
	}

	/**
	 * Adds the updated fields.
	 *
	 * @param fields the fields
	 * @return the compare result
	 */
	public CompareResult addUpdatedFields(@Nonnull String... fields) {
		return addUpdatedFields(Arrays.asList(fields));
	}

	/**
	 * Gets the added fields.
	 *
	 * @return the addedFields
	 */
	public Set<String> getAddedFields() {
		return addedFields;
	}

	/**
	 * Gets the all new data.
	 *
	 * @return the allNewData
	 */
	public Boolean getAllNewData() {
		return allNewData;
	}

	/**
	 * Gets the differences.
	 *
	 * @return the differences
	 */
	public ObjectNode getDifferences() {
		return differences;
	}

	/**
	 * Gets the fields with differences.
	 *
	 * @return the fieldsWithDifferences
	 */
	public Set<String> getFieldsWithDifferences() {
		return fieldsWithDifferences;
	}

	/**
	 * Gets the removed fields.
	 *
	 * @return the removedFields
	 */
	public Set<String> getRemovedFields() {
		return removedFields;
	}

	/**
	 * Gets the updated fields.
	 *
	 * @return the updatedFields
	 */
	public Set<String> getUpdatedFields() {
		return updatedFields;
	}

	/**
	 * Sets the added fields.
	 *
	 * @param addedFields the addedFields to set
	 * @return the compare result
	 */
	public CompareResult setAddedFields(Set<String> addedFields) {
		this.addedFields = addedFields;
		return this;
	}

	/**
	 * Sets the all new data.
	 *
	 * @param allNewData the allNewData to set
	 * @return the compare result
	 */
	public CompareResult setAllNewData(Boolean allNewData) {
		this.allNewData = allNewData;
		return this;
	}

	/**
	 * Sets the differences.
	 *
	 * @param differences the differences to set
	 * @return the compare result
	 */
	public CompareResult setDifferences(ObjectNode differences) {
		this.differences = differences;
		return this;
	}

	/**
	 * Sets the fields with differences.
	 *
	 * @param fieldsWithDifferences the fieldsWithDifferences to set
	 * @return the compare result
	 */
	public CompareResult setFieldsWithDifferences(Set<String> fieldsWithDifferences) {
		this.fieldsWithDifferences = fieldsWithDifferences;
		return this;
	}

	/**
	 * Sets the removed fields.
	 *
	 * @param removedFields the removedFields to set
	 * @return the compare result
	 */
	public CompareResult setRemovedFields(Set<String> removedFields) {
		this.removedFields = removedFields;
		return this;
	}

	/**
	 * Sets the updated fields.
	 *
	 * @param updatedFields the updatedFields to set
	 * @return the compare result
	 */
	public CompareResult setUpdatedFields(Set<String> updatedFields) {
		this.updatedFields = updatedFields;
		return this;
	}

	/**
	 * Test all new data.
	 *
	 * @return true, if successful
	 */
	public boolean testAllNewData() {
		return (allNewData != null) && allNewData.booleanValue();
	}

	/**
	 * Adds the to fields with differences.
	 *
	 * @param fields the fields
	 * @param getter the getter
	 * @param setter the setter
	 * @return the compare result
	 */
	private CompareResult addToFieldsWithDifferences(@Nonnull Collection<String> fields, @Nonnull Supplier<Set<String>> getter, @Nonnull Consumer<Set<String>> setter) {
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
