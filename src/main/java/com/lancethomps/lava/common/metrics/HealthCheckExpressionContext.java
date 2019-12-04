package com.lancethomps.lava.common.metrics;

import java.util.Map;

import com.lancethomps.lava.common.expr.ExprContextRootWithResult;

import io.dropwizard.metrics5.health.HealthCheck.Result;

public class HealthCheckExpressionContext extends ExprContextRootWithResult {

  private final Result healthCheckResult;

  private final HierarchyTree hierarchyTree;

  private final Map<String, Result> results;

  private final Map<String, HierarchyTree> trees;

  public HealthCheckExpressionContext() {
    this(null, null, null, null);
  }

  public HealthCheckExpressionContext(
    Map<String, Result> results,
    Map<String, HierarchyTree> trees,
    HierarchyTree hierarchyTree,
    Result healthCheckResult
  ) {
    super();
    this.results = results;
    this.trees = trees;
    this.hierarchyTree = hierarchyTree;
    this.healthCheckResult = healthCheckResult;
  }

  public Result getHealthCheckResult() {
    return healthCheckResult;
  }

  public HierarchyTree getHierarchyTree() {
    return hierarchyTree;
  }

  public Map<String, Result> getResults() {
    return results;
  }

  public Map<String, HierarchyTree> getTrees() {
    return trees;
  }

}
