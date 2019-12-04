package com.lancethomps.lava.common.time;

import java.util.Map;

import javax.annotation.Nonnull;

public class TimerEnabledBeanPrefixWrapper implements TimerEnabledBean {

  private final TimerEnabledBean delegate;

  private final String prefix;

  public TimerEnabledBeanPrefixWrapper(@Nonnull TimerEnabledBean delegate, @Nonnull String prefix) {
    super();
    this.delegate = delegate;
    this.prefix = prefix;
  }

  @Override
  public <T extends TimerEnabledBean> T addTimersToBean(TimerHandlingBean bean) {
    return delegate.addTimersToBean(bean);
  }

  @Override
  public <T extends TimerEnabledBean> T addWatch(String key, Stopwatch watch) {
    return delegate.addWatch(prefix + key, watch);
  }

  @Override
  public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean) {
    return delegate.addWatchesFromOther(bean);
  }

  @Override
  public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override) {
    return delegate.addWatchesFromOther(bean, override);
  }

  @Override
  public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override, String prefix) {
    return delegate.addWatchesFromOther(bean, override, this.prefix + (prefix == null ? "" : prefix));
  }

  @Override
  public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, String prefix) {
    return delegate.addWatchesFromOther(bean, prefix);
  }

  @Override
  public <T extends TimerEnabledBean> T createAtomicWatch(String key) {
    return delegate.createAtomicWatch(prefix + key);
  }

  @Override
  public Long getTime(String key) {
    return delegate.getTime(prefix + key);
  }

  @Override
  public Stopwatch getWatch(String key) {
    return delegate.getWatch(prefix + key);
  }

  @Override
  public Stopwatch getWatchAddIfAbsent(String key) {
    return delegate.getWatchAddIfAbsent(prefix + key);
  }

  @Override
  public Map<String, Stopwatch> getWatches() {
    return delegate.getWatches();
  }

  @Override
  public <T extends TimerEnabledBean> T pause(String key) {
    return delegate.pause(prefix + key);
  }

  @Override
  public <T extends TimerEnabledBean> T pauseIfNeeded(String key) {
    return delegate.pauseIfNeeded(prefix + key);
  }

  @Override
  public <T extends TimerEnabledBean> T pauseSync(String key) {
    return delegate.pauseSync(prefix + key);
  }

  @Override
  public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
    return delegate.setWatches(watches);
  }

  @Override
  public TimerContext start(@Nonnull String key) {
    return delegate.start(prefix + key);
  }

  @Override
  public TimerContext startSync(String key) {
    return delegate.startSync(prefix + key);
  }

}
