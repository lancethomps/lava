package com.github.lancethomps.lava.common.concurrent;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class TaskThread.
 */
public class TaskThread extends Thread {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(TaskThread.class);

	/** The creation time. */
	private final long creationTime = System.currentTimeMillis();

	/**
	 * Instantiates a new task thread.
	 *
	 * @param group the group
	 * @param target the target
	 * @param name the name
	 */
	public TaskThread(ThreadGroup group, Runnable target, String name) {
		super(group, new TaskThread.WrappingRunnable(target), name);
	}

	/**
	 * Instantiates a new task thread.
	 *
	 * @param group the group
	 * @param target the target
	 * @param name the name
	 * @param stackSize the stack size
	 */
	public TaskThread(ThreadGroup group, Runnable target, String name, long stackSize) {
		super(group, new TaskThread.WrappingRunnable(target), name, stackSize);
	}

	/**
	 * Gets the creation time.
	 *
	 * @return the creation time
	 */
	public final long getCreationTime() {
		return creationTime;
	}

	/**
	 * The Class WrappingRunnable.
	 */
	private static class WrappingRunnable implements Runnable {

		/** The wrapped runnable. */
		private Runnable wrappedRunnable;

		/**
		 * Instantiates a new wrapping runnable.
		 *
		 * @param wrappedRunnable the wrapped runnable
		 */
		WrappingRunnable(Runnable wrappedRunnable) {
			this.wrappedRunnable = wrappedRunnable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				wrappedRunnable.run();
			} catch (StopPooledThreadException arg1) {
				Logs.logDebug(LOG, "Thread exiting on purpose", arg1);
			}

		}
	}
}
