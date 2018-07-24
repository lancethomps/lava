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

/**
 * A factory for creating Metric objects.
 */
public final class MetricFactory {

	/** The dependecy tree configs. */
	private static DependencyTreeConfigs dependecyTreeConfigs;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(MetricFactory.class);

	/** The parsed dependency tree. */
	private static Map<String, HierarchyTree> parsedDependencyTree;

	/**
	 * Check cycles.
	 *
	 * @param hierarchy the hierarchy
	 * @return the pair
	 */
	public static Pair<Boolean, String> checkCycles(Map<String, HierarchyTree> hierarchy) {
		for (Entry<String, HierarchyTree> entry : hierarchy.entrySet()) {
			if (checkIfCycleIsPresent(hierarchy, entry.getValue(), entry.getKey())) {
				return Pair.of(true, entry.getKey());
			}
		}
		return Pair.of(false, null);
	}

	/**
	 * @return the dependecyTreeConfigs
	 */
	public static DependencyTreeConfigs getDependecyTreeConfigs() {
		return dependecyTreeConfigs;
	}

	/**
	 * Gets the or create hierarchy tree.
	 *
	 * @param userService the user service
	 * @return the or create hierarchy tree
	 */
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

	/**
	 * @param dependecyTreeConfigs the dependecyTreeConfigs to set
	 */
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

	/**
	 * Check if cycle is present.
	 *
	 * @param hierarchyMap the hierarchy map
	 * @param tree the tree
	 * @param value the value
	 * @return true, if successful
	 */
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

	/**
	 * Creates the hierarchy tree.
	 *
	 * @param name the name
	 * @param hierarchyMap the hierarchy map
	 * @param tree the tree
	 * @param userService the user service
	 * @return the hierarchy tree
	 */
	private static HierarchyTree createHierarchyTree(String name, Map<String, HierarchyTree> hierarchyMap, HierarchyTree tree, Function<String, HealthCheckOwner> userService) {
		HierarchyTree hierarchyTree = new HierarchyTree();
		// Re-use hierarchy tree if we've already built
		if (hierarchyMap.containsKey(name)) {
			return hierarchyMap.get(name);
		}
		hierarchyTree.setHealthCheckExpressions(tree.getHealthCheckExpressions());
		// Construct hierarchy tree for all dependencies recursively (depth-first)
		if (Checks.isNotEmpty(tree.getDependsOn())) {
			Map<String, HierarchyTree> resolvedDependencies = tree.getDependsOn().stream().collect(
				// Collect to map of service name and hierarchy tree
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