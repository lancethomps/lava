package com.github.lancethomps.lava.common;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.internal.TextListener;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.github.lancethomps.lava.common.compare.Compare;
import com.github.lancethomps.lava.common.lambda.ThrowingBiConsumer;
import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.github.lancethomps.lava.common.lambda.ThrowingSupplier;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.OutputParams;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.sorting.Sorting;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.github.lancethomps.lava.common.time.GenericAtomicTimerHandlingBean;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.time.TimerHandlingBean;
import com.github.lancethomps.lava.common.time.Timing;

/**
 * The Class TestingCommon.
 */
public class TestingCommon {

	/** The Constant TIMERS. */
	public static final GenericAtomicTimerHandlingBean TIMERS = new GenericAtomicTimerHandlingBean();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(TestingCommon.class);

	/** The Constant WAIT_DEFAULT_INCREMENT. */
	private static long waitDefaultIncrement = 100L;

	/** The Constant WAIT_DEFAULT_TIME. */
	private static long waitDefaultTime = 60000L;

	/**
	 * Adds the timers.
	 *
	 * @param timers the timers
	 * @param id the id
	 * @param totalTimeWatch the total time watch
	 * @param addTimers the add timers
	 */
	public static void addTimers(@Nonnull TimerHandlingBean timers, @Nonnull String id, @Nullable Stopwatch totalTimeWatch, @Nullable TimerHandlingBean addTimers) {
		if (totalTimeWatch != null) {
			timers.addTimer(id, totalTimeWatch);
		}
		if (addTimers != null) {
			timers.addTimersFromOther(addTimers, id + '.');
		}
	}

	/**
	 * Assert equals via json diff.
	 *
	 * @param message the message
	 * @param expected the expected
	 * @param actual the actual
	 */
	public static void assertEqualsViaJsonDiff(String message, Object expected, Object actual) {
		assertEqualsViaJsonDiff(message, expected, actual, null);
	}

	/**
	 * Assert equals via json diff.
	 *
	 * @param message the message
	 * @param expected the obj 1
	 * @param actual the obj 2
	 * @param outputParams the output params
	 */
	public static void assertEqualsViaJsonDiff(String message, Object expected, Object actual, OutputParams outputParams) {
		String diff = "";
		if (outputParams == null) {
			diff = Compare.diffAsJson(expected, actual);
		} else {
			diff = Compare.diffSerialized(expected, actual, outputParams);
		}
		if (Checks.isBlank(diff)) {
			return;
		}
		String json1 = Serializer.output(expected, Compare.DEFAULT_DIFF_AS_JSON_PARAMS);
		String json2 = Serializer.output(actual, Compare.DEFAULT_DIFF_AS_JSON_PARAMS);
		Assert.assertEquals(message + '\n' + diff, json1, json2);
	}

	/**
	 * @return the waitDefaultIncrement
	 */
	public static long getWaitDefaultIncrement() {
		return waitDefaultIncrement;
	}

	/**
	 * @return the waitDefaultTime
	 */
	public static long getWaitDefaultTime() {
		return waitDefaultTime;
	}

	/**
	 * Log timers.
	 *
	 * @param timers the timers
	 */
	public static void logTimers(@Nonnull TimerHandlingBean timers) {
		logTimers(timers, null);
	}

	/**
	 * Log timers.
	 *
	 * @param timers the timers
	 * @param classId the class id
	 */
	public static void logTimers(@Nonnull TimerHandlingBean timers, @Nullable String classId) {
		logTimers(timers, classId, null);
	}

	/**
	 * Log timers.
	 *
	 * @param timers the timers
	 * @param classId the class id
	 * @param logger the logger
	 */
	public static void logTimers(@Nonnull TimerHandlingBean timers, @Nullable String classId, @Nullable Logger logger) {
		if (!timers.getTimerLogs().isEmpty()) {
			String header = "TIMERS" + (Checks.isBlank(classId) ? "" : (" - " + classId));
			if (logger != null) {
				Testing.logWithSeparator(logger, header);
			} else {
				Testing.printlnWithSeparator(header);
			}
			int maxLength = timers.getTimerLogs().keySet().stream().mapToInt(String::length).max().getAsInt();
			Sorting.sortMapByValue(timers.getTimerLogs()).forEach((key, timer) -> {
				String msg = String.format("%s  ==> %s", StringUtil.padRight(key, maxLength), StringUtil.padLeft(Timing.formatInterval(timer), 15));
				if (logger != null) {
					Logs.logInfo(logger, msg);
				} else {
					Logs.println(msg);
				}
			});
		}
	}

	/**
	 * Perform action and wait if needed.
	 *
	 * @param <T> the generic type
	 * @param action the action
	 * @param waitLongerTest the wait longer test
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T performActionAndWaitIfNeeded(@Nonnull ThrowingSupplier<T> action, @Nonnull Predicate<T> waitLongerTest) throws Exception {
		return performActionAndWaitIfNeeded(action, waitLongerTest, waitDefaultTime);
	}

	/**
	 * Perform action and wait if needed.
	 *
	 * @param <T> the generic type
	 * @param action the action
	 * @param waitLongerTest the wait longer test
	 * @param waitRemaining the wait remaining
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T performActionAndWaitIfNeeded(@Nonnull ThrowingSupplier<T> action, @Nonnull Predicate<T> waitLongerTest, @Nonnull AtomicLong waitRemaining) throws Exception {
		return performActionAndWaitIfNeeded(action, waitLongerTest, waitRemaining, waitDefaultIncrement);
	}

	/**
	 * Perform action and wait if needed.
	 *
	 * @param <T> the generic type
	 * @param action the action
	 * @param waitLongerTest the wait longer test
	 * @param waitRemaining the wait remaining
	 * @param waitIncrement the wait increment
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T performActionAndWaitIfNeeded(@Nonnull ThrowingSupplier<T> action, @Nonnull Predicate<T> waitLongerTest, @Nonnull AtomicLong waitRemaining, long waitIncrement)
		throws Exception {
		T result = action.get();
		if (waitLongerTest.test(result)) {
			if (waitRemaining.getAndAdd(-waitIncrement) > 0) {
				Logs.logWarn(LOG, "Sleeping %sms to allow for updates to propogate...", waitIncrement);
				waitForUpdates(waitIncrement, "performAction");
				return performActionAndWaitIfNeeded(action, waitLongerTest, waitRemaining, waitIncrement);
			}
			TimeoutException e = new TimeoutException();
			Logs.logError(LOG, e, "Cannot wait any longer for action.");
			throw e;
		}
		return result;
	}

	/**
	 * Perform action and wait if needed.
	 *
	 * @param <T> the generic type
	 * @param action the action
	 * @param waitLongerTest the wait longer test
	 * @param waitTime the wait time
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T performActionAndWaitIfNeeded(@Nonnull ThrowingSupplier<T> action, @Nonnull Predicate<T> waitLongerTest, long waitTime) throws Exception {
		return performActionAndWaitIfNeeded(action, waitLongerTest, new AtomicLong(waitTime));
	}

	/**
	 * Perform action and wait if needed.
	 *
	 * @param <T> the generic type
	 * @param action the action
	 * @param waitLongerTest the wait longer test
	 * @param waitTime the wait time
	 * @param waitIncrement the wait increment
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T performActionAndWaitIfNeeded(@Nonnull ThrowingSupplier<T> action, @Nonnull Predicate<T> waitLongerTest, long waitTime, long waitIncrement) throws Exception {
		return performActionAndWaitIfNeeded(action, waitLongerTest, new AtomicLong(waitTime), waitIncrement);
	}

	/**
	 * Run all tests.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the result
	 */
	public static <T> Result runAllTests(@Nonnull Class<T> type) {
		JUnitCore junit = new JUnitCore();
		junit.addListener(new TextListener(System.out));
		return junit.run(new Computer(), type);
	}

	/**
	 * Run test and cleanup.
	 *
	 * @param <T> the generic type
	 * @param testClass the test class
	 * @param testConsumer the test consumer
	 * @param cleanupMethod the cleanup method
	 * @param iterations the iterations
	 * @throws Exception the exception
	 */
	public static <T> void runTestAndCleanup(@Nonnull Class<T> testClass, @Nonnull ThrowingBiConsumer<T, Integer> testConsumer, @Nonnull ThrowingConsumer<T> cleanupMethod, int iterations)
		throws Exception {
		T test = testClass.newInstance();
		try {
			IntStream.range(0, iterations).forEach(idx -> testConsumer.acceptWithSneakyThrow(test, idx));
		} finally {
			cleanupMethod.accept(test);
		}
	}

	/**
	 * Run test and cleanup.
	 *
	 * @param <T> the generic type
	 * @param testClass the test class
	 * @param testConsumer the test consumer
	 * @param cleanupMethod the cleanup method
	 * @throws Exception the exception
	 */
	public static <T> void runTestAndCleanup(@Nonnull Class<T> testClass, @Nonnull ThrowingConsumer<T> testConsumer, @Nonnull ThrowingConsumer<T> cleanupMethod) throws Exception {
		runTestAndCleanup(testClass, testConsumer, cleanupMethod, 1);
	}

	/**
	 * Run test and cleanup.
	 *
	 * @param <T> the generic type
	 * @param testClass the test class
	 * @param testConsumer the test consumer
	 * @param cleanupMethod the cleanup method
	 * @param iterations the iterations
	 * @throws Exception the exception
	 */
	public static <T> void runTestAndCleanup(@Nonnull Class<T> testClass, @Nonnull ThrowingConsumer<T> testConsumer, @Nonnull ThrowingConsumer<T> cleanupMethod, int iterations)
		throws Exception {
		runTestAndCleanup(testClass, (test, idx) -> testConsumer.acceptWithSneakyThrow(test), cleanupMethod, iterations);
	}

	/**
	 * @param waitDefaultIncrement the waitDefaultIncrement to set
	 */
	public static void setWaitDefaultIncrement(long waitDefaultIncrement) {
		Assert.assertTrue("waitDefaultIncrement must be > 0", waitDefaultIncrement > 0);
		TestingCommon.waitDefaultIncrement = waitDefaultIncrement;
	}

	/**
	 * @param waitDefaultTime the waitDefaultTime to set
	 */
	public static void setWaitDefaultTime(long waitDefaultTime) {
		Assert.assertTrue("waitDefaultTime must be > 0", waitDefaultTime > 0);
		TestingCommon.waitDefaultTime = waitDefaultTime;
	}

	/**
	 * Test consumer.
	 *
	 * @param iterations the iterations
	 * @param function the function
	 */
	public static void testConsumer(long iterations, IntConsumer function) {
		IntStream.iterate(0, i -> i + 1).limit(iterations).forEach(function);
	}

	/**
	 * Test parallel consumer.
	 *
	 * @param threads the threads
	 * @param iterations the iterations
	 * @param function the function
	 */
	public static void testConsumer(long threads, long iterations, IntConsumer function) {
		IntStream.iterate(0, i -> i + 1).parallel().limit(threads).forEach(i -> {
			testConsumer(iterations, function);
		});
	}

	/**
	 * Wait for updates.
	 *
	 * @param sleepTime the sleep time
	 * @param timerId the timer id
	 * @throws InterruptedException the interrupted exception
	 */
	// CHECKSTYLE.OFF: ThreadSleep
	public static void waitForUpdates(long sleepTime, String timerId) throws InterruptedException {
		Thread.sleep(sleepTime);
		TIMERS.addTimer("waitForUpdates", sleepTime);
		if (timerId != null) {
			TIMERS.addTimer("waitForUpdates." + timerId, sleepTime);
		}
	}

}
