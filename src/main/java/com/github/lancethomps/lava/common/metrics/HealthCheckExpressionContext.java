package com.github.lancethomps.lava.common.metrics;

import java.util.Map;

import com.github.lancethomps.lava.common.expr.ExprContextRootWithResult;

import io.dropwizard.metrics5.health.HealthCheck.Result;

/**
 * The Class HealthCheckExpressionContext.
 */
public class HealthCheckExpressionContext extends ExprContextRootWithResult {

	/** The health check result. */
	private final Result healthCheckResult;

	/** The hierarchy tree. */
	private final HierarchyTree hierarchyTree;

	/** The results. */
	private final Map<String, Result> results;

	/** The trees. */
	private final Map<String, HierarchyTree> trees;

	/**
	 * Instantiates a new health check expression context.
	 */
	public HealthCheckExpressionContext() {
		this(null, null, null, null);
	}

	/**
	 * Instantiates a new health check expression context.
	 *
	 * @param results the results
	 * @param trees the trees
	 * @param hierarchyTree the hierarchy tree
	 * @param healthCheckResult the health check result
	 */
	public HealthCheckExpressionContext(Map<String, Result> results, Map<String, HierarchyTree> trees, HierarchyTree hierarchyTree, Result healthCheckResult) {
		super();
		this.results = results;
		this.trees = trees;
		this.hierarchyTree = hierarchyTree;
		this.healthCheckResult = healthCheckResult;
	}

	/**
	 * Gets the health check result.
	 *
	 * @return the health check result
	 */
	public Result getHealthCheckResult() {
		return healthCheckResult;
	}

	/**
	 * Gets the hierarchy tree.
	 *
	 * @return the hierarchyTree
	 */
	public HierarchyTree getHierarchyTree() {
		return hierarchyTree;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public Map<String, Result> getResults() {
		return results;
	}

	/**
	 * Gets the trees.
	 *
	 * @return the trees
	 */
	public Map<String, HierarchyTree> getTrees() {
		return trees;
	}

}
