package com.github.lancethomps.lava.common.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class MetricFactoryTest.
 */
public class MetricFactoryTest {

	/** The hierarchy map with cycle. */
	private Map<String, HierarchyTree> hierarchyMapWithCycle;

	/** The hierarchy map without cycle. */
	private Map<String, HierarchyTree> hierarchyMapWithoutCycle;

	/**
	 * Setups up the hierarchy maps/.
	 */
	@Before
	public void setUp() {
		hierarchyMapWithCycle = new HashMap<>();
		hierarchyMapWithoutCycle = new HashMap<>();
		HierarchyTree ht = new HierarchyTree();
		HierarchyTree ht2 = new HierarchyTree();
		HierarchyTree ht3 = new HierarchyTree();
		HierarchyTree ht4 = new HierarchyTree();
		ht.setDependsOn(Arrays.asList("B", "C"));
		ht2.setDependsOn(Arrays.asList("A"));
		ht3.setDependsOn(Arrays.asList("B", "C"));
		ht4.setDependsOn(Arrays.asList("D"));
		// Creates cycle as A dependsOn B and B dependsOn A
		hierarchyMapWithCycle.put("A", ht);
		hierarchyMapWithCycle.put("B", ht2);
		// No cycle
		hierarchyMapWithoutCycle.put("A", ht3);
		hierarchyMapWithoutCycle.put("B", ht4);
	}

	/**
	 * Should detect cycles.
	 */
	@Test
	public void shouldDetechCycles() {
		assertEquals(true, MetricFactory.checkCycles(hierarchyMapWithCycle).getLeft());

	}

	/**
	 * Should not detect cycles.
	 */
	@Test
	public void shouldNotDetectCycles() {
		assertEquals(false, MetricFactory.checkCycles(hierarchyMapWithoutCycle).getLeft());
	}
}
