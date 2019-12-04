package com.lancethomps.lava.common.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MetricFactoryTest {

  private Map<String, HierarchyTree> hierarchyMapWithCycle;

  private Map<String, HierarchyTree> hierarchyMapWithoutCycle;

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

    hierarchyMapWithCycle.put("A", ht);
    hierarchyMapWithCycle.put("B", ht2);

    hierarchyMapWithoutCycle.put("A", ht3);
    hierarchyMapWithoutCycle.put("B", ht4);
  }

  @Test
  public void shouldDetechCycles() {
    assertEquals(true, MetricFactory.checkCycles(hierarchyMapWithCycle).getLeft());

  }

  @Test
  public void shouldNotDetectCycles() {
    assertEquals(false, MetricFactory.checkCycles(hierarchyMapWithoutCycle).getLeft());
  }

}
