package com.lancethomps.lava.common.metrics;

import java.util.List;
import java.util.Map;

import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.ser.OutputExpression;

import io.dropwizard.metrics5.health.HealthCheck.Result;

public class HierarchyTree extends ExternalizableBean {

  private Map<String, HierarchyTree> dependencies;

  private List<String> dependsOn;

  private List<String> failingDependencies;

  private List<OutputExpression> healthCheckExpressions;

  private Map<String, Object> healthCheckExpressionsOutput;

  private Result healthState;

  private Boolean healthy;

  private HierarchyTree hierarchy;

  private String name;

  private List<String> ownerIds;

  private List<HealthCheckOwner> owners;

  public HierarchyTree() {
    super();
  }

  @Override
  public void afterDeserialization() {
    ExprFactory.compileCreateExpressions(healthCheckExpressions, false, false, true);
  }

  public Map<String, HierarchyTree> getDependencies() {
    return dependencies;
  }

  public HierarchyTree setDependencies(Map<String, HierarchyTree> dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  public List<String> getDependsOn() {
    return dependsOn;
  }

  public HierarchyTree setDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn;
    return this;
  }

  public List<String> getFailingDependencies() {
    return failingDependencies;
  }

  public HierarchyTree setFailingDependencies(List<String> failingDependencies) {
    this.failingDependencies = failingDependencies;
    return this;
  }

  public List<OutputExpression> getHealthCheckExpressions() {
    return healthCheckExpressions;
  }

  public HierarchyTree setHealthCheckExpressions(List<OutputExpression> healthCheckExpressions) {
    this.healthCheckExpressions = healthCheckExpressions;
    return this;
  }

  public Map<String, Object> getHealthCheckExpressionsOutput() {
    return healthCheckExpressionsOutput;
  }

  public HierarchyTree setHealthCheckExpressionsOutput(Map<String, Object> healthCheckExpressionsOutput) {
    this.healthCheckExpressionsOutput = healthCheckExpressionsOutput;
    return this;
  }

  public Result getHealthState() {
    return healthState;
  }

  public HierarchyTree setHealthState(Result healthState) {
    this.healthState = healthState;
    return this;
  }

  public Boolean getHealthy() {
    return healthy;
  }

  public HierarchyTree setHealthy(Boolean healthy) {
    this.healthy = healthy;
    return this;
  }

  public HierarchyTree getHierarchy() {
    return hierarchy;
  }

  public HierarchyTree setHierarchy(HierarchyTree hierarchy) {
    this.hierarchy = hierarchy;
    return this;
  }

  public String getName() {
    return name;
  }

  public HierarchyTree setName(String name) {
    this.name = name;
    return this;
  }

  public List<String> getOwnerIds() {
    return ownerIds;
  }

  public HierarchyTree setOwnerIds(List<String> ownerIds) {
    this.ownerIds = ownerIds;
    return this;
  }

  public List<HealthCheckOwner> getOwners() {
    return owners;
  }

  public HierarchyTree setOwners(List<HealthCheckOwner> owners) {
    this.owners = owners;
    return this;
  }

  public boolean testHealthy() {
    return (healthy == null) || healthy.booleanValue();
  }

}
