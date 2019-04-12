package com.github.lancethomps.lava.common.concurrent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.lancethomps.lava.common.ser.Serializer;

public class ExecutorFactory {

  private static final ConcurrentHashMap<String, ThreadPoolExecutor> EXECUTORS = new ConcurrentHashMap<>();

  private static final AtomicInteger POOL_COUNT = new AtomicInteger(0);

  public static CachedAndQueuedThreadPoolExecutor getCachedThreadPool(int minSpareThreads, int maxThreads, String threadNamePrefix) {
    final TaskQueue queue = new TaskQueue(Integer.MAX_VALUE);
    final String execName = defaultIfBlank(threadNamePrefix, "pool-" + POOL_COUNT.incrementAndGet());
    final TaskThreadFactory tf = new TaskThreadFactory(execName, false, 5);
    final CachedAndQueuedThreadPoolExecutor exec =
      new CachedAndQueuedThreadPoolExecutor(minSpareThreads, maxThreads, 60000L, MILLISECONDS, queue, tf);
    exec.setThreadRenewalDelay(1000L);
    queue.setParent(exec);
    EXECUTORS.put(execName, exec);
    return exec;
  }

  public static Map<String, String> getInitializedExecutorsInfo() {
    return EXECUTORS.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toString(), (a, b) -> a, TreeMap::new));
  }

  public static Map<String, Object> getInitializedExecutorsStats() {
    return getInitializedExecutorsStats("");
  }

  public static Map<String, Object> getInitializedExecutorsStats(String prefix) {
    return EXECUTORS.entrySet().stream().flatMap(e -> {
      ThreadPoolExecutor exec = e.getValue();
      Map<String, Object> stats = Serializer.createJsonMap();
      addExecutorStat(prefix, e.getKey(), stats, null, exec.toString());
      addExecutorStat(prefix, e.getKey(), stats, "poolSize", exec.getPoolSize());
      addExecutorStat(prefix, e.getKey(), stats, "activeThreads", exec.getActiveCount());
      addExecutorStat(prefix, e.getKey(), stats, "queuedTasks", exec.getQueue().size());
      addExecutorStat(prefix, e.getKey(), stats, "completedTasks", exec.getCompletedTaskCount());
      return stats.entrySet().stream();
    }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public static ThreadPoolExecutor getReleasingFixedThreadPool(int maxThreads, String threadNamePrefix) {
    final String execName = defaultIfBlank(threadNamePrefix, "pool-" + POOL_COUNT.incrementAndGet());
    final CustomNamingThreadFactory threadFactory = new CustomNamingThreadFactory(execName, null, null);
    final ThreadPoolExecutor exec = new ThreadPoolExecutor(maxThreads, maxThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory);
    exec.allowCoreThreadTimeOut(true);
    EXECUTORS.put(execName, exec);
    return exec;
  }

  private static void addExecutorStat(String prefix, String name, Map<String, Object> stats, String key, Object val) {
    stats.put(prefix + name + (key != null ? ("_" + key) : ""), val);
  }

}
