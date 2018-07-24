package com.github.lancethomps.lava.common.os;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.file.FileUtil;
import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.time.Stopwatch;

/**
 * The Class Processes.
 *
 * @author lathomps
 */
public class Processes {

	/** The Constant DEFAULT_TIMEOUT_SECONDS. */
	public static final long DEFAULT_TIMEOUT_SECONDS = 60;

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Processes.class);

	/**
	 * Run.
	 *
	 * @param commands the commands
	 * @return the process result
	 */
	public static ProcessResult run(@Nonnull List<String> commands) {
		return run(commands, DEFAULT_TIMEOUT_SECONDS);
	}

	/**
	 * Run.
	 *
	 * @param commands the commands
	 * @param timeoutSeconds the timeout seconds
	 * @return the process result
	 */
	public static ProcessResult run(@Nonnull List<String> commands, long timeoutSeconds) {
		return run(commands, timeoutSeconds, null);
	}

	/**
	 * Run.
	 *
	 * @param commands the commands
	 * @param timeoutSeconds the timeout seconds
	 * @param settingsModifier the settings modifier
	 * @return the process result
	 */
	public static ProcessResult run(@Nonnull List<String> commands, long timeoutSeconds, @Nullable ThrowingConsumer<ProcessBuilder> settingsModifier) {
		Integer exitValue = null;
		String errorOutput = null;
		String output = null;
		Process process = null;
		Exception exception = null;
		Stopwatch watch = new Stopwatch(true);
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(commands);
			File outFile = File.createTempFile("java-process-out", ".log");
			processBuilder.redirectOutput(outFile);
			File errorFile = File.createTempFile("java-process-err", ".log");
			processBuilder.redirectError(errorFile);

			if (settingsModifier != null) {
				settingsModifier.accept(processBuilder);
			}

			process = processBuilder.start();
			if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
				exitValue = process.exitValue();
			}
			if (outFile.isFile()) {
				output = FileUtil.readFile(outFile);
				outFile.delete();
			}
			if (errorFile.isFile()) {
				errorOutput = FileUtil.readFile(errorFile);
				errorFile.delete();
			}
		} catch (Exception e) {
			exception = e;
			Logs.logError(LOG, e, "Issue getting output for process [%s]", process);
		}
		return new ProcessResult(errorOutput, exception, exitValue, output, watch.getTime());
	}

	/**
	 * Run.
	 *
	 * @param commands the commands
	 * @return the process result
	 */
	public static ProcessResult run(@Nonnull String... commands) {
		return run(Arrays.asList(commands), DEFAULT_TIMEOUT_SECONDS);
	}

}
