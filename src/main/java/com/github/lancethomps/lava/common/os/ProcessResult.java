package com.github.lancethomps.lava.common.os;

import com.github.lancethomps.lava.common.SimpleDomainObject;

/**
 * The Class ProcessResult.
 *
 * @author lancethomps
 */
public class ProcessResult extends SimpleDomainObject {

	/** The error output. */
	private final String errorOutput;

	/** The exception. */
	private final Exception exception;

	/** The exit value. */
	private final Integer exitValue;

	/** The output. */
	private final String output;

	/** The time taken. */
	private final Long timeTaken;

	/**
	 * Instantiates a new process result.
	 *
	 * @param errorOutput the error output
	 * @param exception the exception
	 * @param exitValue the exit value
	 * @param output the output
	 * @param timeTaken the time taken
	 */
	public ProcessResult(String errorOutput, Exception exception, Integer exitValue, String output, Long timeTaken) {
		super();
		this.errorOutput = errorOutput;
		this.exception = exception;
		this.exitValue = exitValue;
		this.output = output;
		this.timeTaken = timeTaken;
	}

	/**
	 * @return the errorOutput
	 */
	public String getErrorOutput() {
		return errorOutput;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @return the exitValue
	 */
	public Integer getExitValue() {
		return exitValue;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @return the timeTaken
	 */
	public Long getTimeTaken() {
		return timeTaken;
	}

	/**
	 * Was successful.
	 *
	 * @return true, if successful
	 */
	public boolean wasSuccessful() {
		return (exitValue != null) && (exitValue.intValue() == 0);
	}

}
