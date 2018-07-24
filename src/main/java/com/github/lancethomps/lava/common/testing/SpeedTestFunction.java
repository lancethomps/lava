package com.github.lancethomps.lava.common.testing;

import com.github.lancethomps.lava.common.lambda.ThrowingFunction;

/**
 * The Class SpeedTestFunction.
 */
public class SpeedTestFunction {

	/** The function. */
	private ThrowingFunction<Integer, Long> function;

	/** The id. */
	private String id;

	/**
	 * Instantiates a new speed test function.
	 */
	public SpeedTestFunction() {
		super();
	}

	/**
	 * Instantiates a new speed test function.
	 *
	 * @param id the id
	 * @param function the function
	 */
	public SpeedTestFunction(String id, ThrowingFunction<Integer, Long> function) {
		super();
		this.id = id;
		this.function = function;
	}

	/**
	 * Instantiates a new speed test function.
	 *
	 * @param function the function
	 */
	public SpeedTestFunction(ThrowingFunction<Integer, Long> function) {
		super();
		this.function = function;
	}

	/**
	 * Instantiates a new speed test function.
	 *
	 * @param function the function
	 * @param id the id
	 */
	public SpeedTestFunction(ThrowingFunction<Integer, Long> function, String id) {
		super();
		this.function = function;
		this.id = id;
	}

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public ThrowingFunction<Integer, Long> getFunction() {
		return function;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the function.
	 *
	 * @param function the function to set
	 * @return the speed test function
	 */
	public SpeedTestFunction setFunction(ThrowingFunction<Integer, Long> function) {
		this.function = function;
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @return the speed test function
	 */
	public SpeedTestFunction setId(String id) {
		this.id = id;
		return this;
	}

}
