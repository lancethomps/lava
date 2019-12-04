package com.lancethomps.lava.common.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.admin.CacheRegionInfo;
import org.apache.commons.jcs.admin.JCSAdminBean;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.stats.behavior.ICacheStats;
import org.apache.log4j.Logger;

import com.lancethomps.lava.common.logging.Logs;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricSet;

public class JcsCacheMetricSet implements MetricSet {

  private static final Logger LOG = Logger.getLogger(JcsCacheMetricSet.class);

  public static CacheRegionInfo getCacheRegion(String cacheName) {
    if (cacheName != null) {
      try {
        JCSAdminBean jcs = new JCSAdminBean();
        CompositeCache<?, ?> cache = CompositeCacheManager.getInstance().getCache(cacheName);
        CacheRegionInfo regionInfo = new CacheRegionInfo(
          cache.getCacheName(),
          cache.getSize(),
          cache.getStatus().toString(),
          cache.getStats(),
          cache.getHitCountRam(),
          cache.getHitCountAux(),
          cache.getMissCountNotFound(),
          cache.getMissCountExpired(),
          jcs.getByteCount(cache)
        );
        return regionInfo;
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Error getting JCS cache region info for [%s]", cacheName);
      }
    }
    return null;
  }

  public static ICacheStats getStatistics(String cacheName) {
    return JCS.getInstance(cacheName).getStatistics();
  }

  @Override
  public Map<MetricName, Metric> getMetrics() {

    try {
      JCSAdminBean jcs = new JCSAdminBean();
      CacheRegionInfo[] regions = jcs.buildCacheInfo();

      Map<MetricName, Metric> gauges = new HashMap<>(regions.length * 5);

      for (CacheRegionInfo region : regions) {
        String cacheName = region.getCacheName();
        gauges.put(MetricName.build(cacheName), new CacheRegionMetrics(cacheName));
      }

      return Collections.unmodifiableMap(gauges);
    } catch (Exception e) {
      Logs.logError(LOG, e, "Issue initializing JCS metrics.");
      return Collections.emptyMap();
    }
  }

  public static class CacheRegionMetrics implements Gauge<Map<String, Object>> {

    private final String regionName;

    public CacheRegionMetrics(String regionName) {
      super();
      this.regionName = regionName;
    }

    @Override
    public Map<String, Object> getValue() {
      Map<String, Object> value = new TreeMap<>();
      CacheRegionInfo region = getCacheRegion(regionName);
      if (region != null) {
        int hits = region.getHitCountAux() + region.getHitCountRam();
        int misses = region.getMissCountExpired() + region.getMissCountNotFound();
        int all = hits + misses;
        double hitRatio = all == 0 ? 0d : ((double) hits / (double) all);
        value.put("cache_size", region.getCacheSize());
        value.put("byte_count", region.getByteCount());
        value.put("hit_count", hits);
        value.put("hit_count_aux", region.getHitCountAux());
        value.put("hit_count_ram", region.getHitCountRam());
        value.put("miss_count", misses);
        value.put("miss_count_expired", region.getMissCountExpired());
        value.put("miss_count_not_found", region.getMissCountNotFound());
        value.put("hit_ratio", hitRatio);
      }
      return value;
    }

  }

}
