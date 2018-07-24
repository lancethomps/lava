package com.github.lancethomps.lava.common.metrics;

import java.util.List;
import java.util.Map;

import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.OutputExpression;

import io.dropwizard.metrics5.health.HealthCheck.Result;

/**
 * The Class HierarchyTree.
 */
public class HierarchyTree extends ExternalizableBean {

	/** The dependencies. */
	private Map<String, HierarchyTree> dependencies;

	/** The depends on. */
	private List<String> dependsOn;

	/** The failing dependencies. */
	private List<String> failingDependencies;

	/** The health check expressions. */
	private List<OutputExpression> healthCheckExpressions;

	/** The health check expressions output. */
	private Map<String, Object> healthCheckExpressionsOutput;

	/** The health state. */
	private Result healthState;

	/** The healthy. */
	private Boolean healthy;

	/** The hierarchy tree. */
	private HierarchyTree hierarchy;

	/** The name. */
	private String name;

	/** The list of the Aladdin user IDs of the service owners (populates "owners"). */
	private List<String> ownerIds;

	/** The owners. */
	private List<HealthCheckOwner> owners;

	/**
	 * Instantiates a new hierarchy tree.
	 */
	public HierarchyTree() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.ser.PostConstructor#afterDeserialization()
	 */
	@Override
	public void afterDeserialization() {
		ExprFactory.compileCreateExpressions(healthCheckExpressions, false, false, true);
	}

	/**
	 * Gets the dependencies.
	 *
	 * @return the dependencies
	 */
	public Map<String, HierarchyTree> getDependencies() {
		return dependencies;
	}

	/**
	 * Gets the depends on.
	 *
	 * @return the depends on
	 */
	public List<String> getDependsOn() {
		return dependsOn;
	}

	/**
	 * Gets the failing dependencies.
	 *
	 * @return the failing dependencies
	 */
	public List<String> getFailingDependencies() {
		return failingDependencies;
	}

	/**
	 * @return the healthCheckExpressions
	 */
	public List<OutputExpression> getHealthCheckExpressions() {
		return healthCheckExpressions;
	}

	/**
	 * @return the healthCheckExpressionsOutput
	 */
	public Map<String, Object> getHealthCheckExpressionsOutput() {
		return healthCheckExpressionsOutput;
	}

	/**
	 * Gets the health state.
	 *
	 * @return the health state
	 */
	public Result getHealthState() {
		return healthState;
	}

	/**
	 * @return the healthy
	 */
	public Boolean getHealthy() {
		return healthy;
	}

	/**
	 * Gets the hierarchy.
	 *
	 * @return the hierarchy
	 */
	public HierarchyTree getHierarchy() {
		return hierarchy;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the owner.
	 *
	 * @return the owner
	 */
	public List<String> getOwnerIds() {
		return ownerIds;
	}

	/**
	 * Gets employee.
	 *
	 * @return the employee
	 */
	public List<HealthCheckOwner> getOwners() {
		return owners;
	}

	/**
	 * Sets the dependencies.
	 *
	 * @param dependencies the dependencies
	 * @return the hierarchy tree
	 */
	public HierarchyTree setDependencies(Map<String, HierarchyTree> dependencies) {
		this.dependencies = dependencies;
		return this;
	}

	/**
	 * Sets the depends on.
	 *
	 * @param dependsOn the new depends on
	 * @return the hierarchy tree
	 */
	public HierarchyTree setDependsOn(List<String> dependsOn) {
		this.dependsOn = dependsOn;
		return this;
	}

	/**
	 * Sets the failing dependencies.
	 *
	 * @param failingDependencies the new failing dependencies
	 * @return the hierarchy tree
	 */
	public HierarchyTree setFailingDependencies(List<String> failingDependencies) {
		this.failingDependencies = failingDependencies;
		return this;
	}

	/**
	 * Sets the health check expressions.
	 *
	 * @param healthCheckExpressions the healthCheckExpressions to set
	 * @return the hierarchy tree
	 */
	public HierarchyTree setHealthCheckExpressions(List<OutputExpression> healthCheckExpressions) {
		this.healthCheckExpressions = healthCheckExpressions;
		return this;
	}

	/**
	 * Sets the health check expressions output.
	 *
	 * @param healthCheckExpressionsOutput the healthCheckExpressionsOutput to set
	 * @return the hierarchy tree
	 */
	public HierarchyTree setHealthCheckExpressionsOutput(Map<String, Object> healthCheckExpressionsOutput) {
		this.healthCheckExpressionsOutput = healthCheckExpressionsOutput;
		return this;
	}

	/**
	 * Sets the health state.
	 *
	 * @param healthState the new health state
	 * @return the hierarchy tree
	 */
	public HierarchyTree setHealthState(Result healthState) {
		this.healthState = healthState;
		return this;
	}

	/**
	 * Sets the healthy.
	 *
	 * @param healthy the healthy to set
	 * @return the hierarchy tree
	 */
	public HierarchyTree setHealthy(Boolean healthy) {
		this.healthy = healthy;
		return this;
	}

	/**
	 * Sets the hierarchy.
	 *
	 * @param hierarchy the new hierarchy
	 * @return the hierarchy tree
	 */
	public HierarchyTree setHierarchy(HierarchyTree hierarchy) {
		this.hierarchy = hierarchy;
		return this;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 * @return the hierarchy tree
	 */
	public HierarchyTree setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the owner.
	 *
	 * @param ownerIds the new owner
	 * @return the hierarchy tree
	 */
	public HierarchyTree setOwnerIds(List<String> ownerIds) {
		this.ownerIds = ownerIds;
		return this;
	}

	/**
	 * Sets the employee.
	 *
	 * @param owners the new employee
	 * @return the hierarchy tree
	 */
	public HierarchyTree setOwners(List<HealthCheckOwner> owners) {
		this.owners = owners;
		return this;
	}

	/**
	 * Test healthy.
	 *
	 * @return true, if successful
	 */
	public boolean testHealthy() {
		return (healthy == null) || healthy.booleanValue();
	}

}
