package com.github.lancethomps.lava.common.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.lambda.Lambdas;

public final class MetricFactory {

  private static final Logger LOG = Logger.getLogger(MetricFactory.class);
  private static DependencyTreeConfigs dependecyTreeConfigs;
  private static Map<String, HierarchyTree> parsedDependencyTree;

  public static Pair<Boolean, String> checkCycles(Map<String, HierarchyTree> hierarchy) {
    for (Entry<String, HierarchyTree> entry : hierarchy.entrySet()) {
      if (checkIfCycleIsPresent(hierarchy, entry.getValue(), entry.getKey())) {
        return Pair.of(true, entry.getKey());
      }
    }
    return Pair.of(false, null);
  }

  public static DependencyTreeConfigs getDependecyTreeConfigs() {
    return dependecyTreeConfigs;
  }

  public static void setDependecyTreeConfigs(DependencyTreeConfigs dependecyTreeConfigs) {
    if ((dependecyTreeConfigs != null) && (dependecyTreeConfigs.getServices() != null)) {
      Pair<Boolean, String> cycleCheck = checkCycles(dependecyTreeConfigs.getServices());
      if (cycleCheck.getLeft()) {
        throw new IllegalArgumentException(String.format("Cyclic dependency detected for: %s", cycleCheck.getRight()));
      }
    }
    MetricFactory.dependecyTreeConfigs = dependecyTreeConfigs;
    parsedDependencyTree = null;
  }

  public static Map<String, HierarchyTree> getOrCreateHierarchyTree(@Nullable Function<String, HealthCheckOwner> userService) {
    if ((dependecyTreeConfigs != null) && (dependecyTreeConfigs.getServices() != null) && (parsedDependencyTree == null)) {
      Map<String, HierarchyTree> parsedDependencyTree = new HashMap<>();
      dependecyTreeConfigs.getServices().forEach(
        (k, v) -> {
          parsedDependencyTree.put(k, createHierarchyTree(k, parsedDependencyTree, v, userService));
        }
      );
      MetricFactory.parsedDependencyTree = parsedDependencyTree;
    }
    return parsedDependencyTree;
  }

  private static boolean checkIfCycleIsPresent(Map<String, HierarchyTree> hierarchyMap, HierarchyTree tree, String value) {
    if (Checks.isNotEmpty(tree.getDependsOn())) {
      if (tree.getDependsOn().contains(value)) {
        return true;
      }
      for (String id : tree.getDependsOn()) {
        if (hierarchyMap.containsKey(id)) {
          if (checkIfCycleIsPresent(hierarchyMap, hierarchyMap.get(id), value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static HierarchyTree createHierarchyTree(
    String name,
    Map<String, HierarchyTree> hierarchyMap,
    HierarchyTree tree,
    Function<String, HealthCheckOwner> userService
  ) {
    HierarchyTree hierarchyTree = new HierarchyTree();

    if (hierarchyMap.containsKey(name)) {
      return hierarchyMap.get(name);
    }
    hierarchyTree.setHealthCheckExpressions(tree.getHealthCheckExpressions());

    if (Checks.isNotEmpty(tree.getDependsOn())) {
      Map<String, HierarchyTree> resolvedDependencies = tree.getDependsOn().stream().collect(

        Collectors.toMap(
          ele -> ele,
          ele -> createHierarchyTree(ele, hierarchyMap, hierarchyMap.getOrDefault(ele, new HierarchyTree()), userService),
          Lambdas.getDefaultMergeFunction(),
          TreeMap::new
        )
      );
      hierarchyTree.setDependencies(resolvedDependencies);
    }
    hierarchyTree.setDependsOn(tree.getDependsOn());
    hierarchyTree.setName(name);
    hierarchyTree.setOwnerIds(tree.getOwnerIds());
    if (Checks.nonNull(hierarchyTree.getOwnerIds()) && (userService != null)) {
      List<HealthCheckOwner> owners = hierarchyTree
        .getOwnerIds()
        .stream()
        .map(ownerId -> userService.apply(ownerId))
        .filter(Checks::nonNull)
        .collect(Collectors.toList());
      hierarchyTree.setOwners(owners);
    }
    hierarchyMap.putIfAbsent(name, hierarchyTree);
    return hierarchyTree;
  }

}
