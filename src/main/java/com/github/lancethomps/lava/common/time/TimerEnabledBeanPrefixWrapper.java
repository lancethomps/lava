package com.github.lancethomps.lava.common.time;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * The Class TimerEnabledBeanPrefixWrapper.
 */
public class TimerEnabledBeanPrefixWrapper implements TimerEnabledBean {

	/** The delegate. */
	private final TimerEnabledBean delegate;

	/** The prefix. */
	private final String prefix;

	/**
	 * Instantiates a new timer enabled bean prefix wrapper.
	 *
	 * @param delegate the delegate
	 * @param prefix the prefix
	 */
	public TimerEnabledBeanPrefixWrapper(@Nonnull TimerEnabledBean delegate, @Nonnull String prefix) {
		super();
		this.delegate = delegate;
		this.prefix = prefix;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#addTimersToBean(com.github.lancethomps.lava.common.time.
	 * TimerHandlingBean)
	 */
	@Override
	public <T extends TimerEnabledBean> T addTimersToBean(TimerHandlingBean bean) {
		return delegate.addTimersToBean(bean);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#addWatch(java.lang.String,
	 * com.github.lancethomps.lava.common.time.Stopwatch)
	 */
	@Override
	public <T extends TimerEnabledBean> T addWatch(String key, Stopwatch watch) {
		return delegate.addWatch(prefix + key, watch);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.time.TimerEnabledBean#addWatchesFromOther(com.github.lancethomps.lava.common.time.
	 * TimerEnabledBean)
	 */
	@Override
	public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean) {
		return delegate.addWatchesFromOther(bean);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.time.TimerEnabledBean#addWatchesFromOther(com.github.lancethomps.lava.common.time.
	 * TimerEnabledBean, boolean)
	 */
	@Override
	public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override) {
		return delegate.addWatchesFromOther(bean, override);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.time.TimerEnabledBean#addWatchesFromOther(com.github.lancethomps.lava.common.time.
	 * TimerEnabledBean, boolean, java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, boolean override, String prefix) {
		return delegate.addWatchesFromOther(bean, override, this.prefix + (prefix == null ? "" : prefix));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.github.lancethomps.lava.common.time.TimerEnabledBean#addWatchesFromOther(com.github.lancethomps.lava.common.time.
	 * TimerEnabledBean, java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T addWatchesFromOther(TimerEnabledBean bean, String prefix) {
		return delegate.addWatchesFromOther(bean, prefix);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#createAtomicWatch(java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T createAtomicWatch(String key) {
		return delegate.createAtomicWatch(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#getTime(java.lang.String)
	 */
	@Override
	public Long getTime(String key) {
		return delegate.getTime(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#getWatch(java.lang.String)
	 */
	@Override
	public Stopwatch getWatch(String key) {
		return delegate.getWatch(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#getWatchAddIfAbsent(java.lang.String)
	 */
	@Override
	public Stopwatch getWatchAddIfAbsent(String key) {
		return delegate.getWatchAddIfAbsent(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#getWatches()
	 */
	@Override
	public Map<String, Stopwatch> getWatches() {
		return delegate.getWatches();
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#pause(java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T pause(String key) {
		return delegate.pause(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#pauseIfNeeded(java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T pauseIfNeeded(String key) {
		return delegate.pauseIfNeeded(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#pauseSync(java.lang.String)
	 */
	@Override
	public <T extends TimerEnabledBean> T pauseSync(String key) {
		return delegate.pauseSync(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#setWatches(java.util.Map)
	 */
	@Override
	public <T extends TimerEnabledBean> T setWatches(Map<String, Stopwatch> watches) {
		return delegate.setWatches(watches);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#start(java.lang.String)
	 */
	@Override
	public TimerContext start(@Nonnull String key) {
		return delegate.start(prefix + key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerEnabledBean#startSync(java.lang.String)
	 */
	@Override
	public TimerContext startSync(String key) {
		return delegate.startSync(prefix + key);
	}

}
