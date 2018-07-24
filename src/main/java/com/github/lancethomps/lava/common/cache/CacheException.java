package com.github.lancethomps.lava.common.cache;


/**
 * The Class CacheException.
 */
public class CacheException extends Exception {

	/** Generated serial version UID. */
	private static final long serialVersionUID = 1197603538578807474L;

	/** The name. */
	private String name;

	/**
	 * Instantiates a new cache exception.
	 */
	public CacheException() {
		super();
	}

	/**
	 * Instantiates a new cache exception.
	 * 
	 * @param message the message
	 */
	public CacheException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new cache exception.
	 * 
	 * @param message the message
	 * @param name the name
	 * @param t the t
	 */
	public CacheException(String message, String name, Throwable t) {
		super(message, t);
		this.name = name;
	}

	/**
	 * Instantiates a new cache exception.
	 * 
	 * @param message the message
	 * @param t the t
	 */
	public CacheException(String message, Throwable t) {
		super(message, t);
	}

	/**
	 * Instantiates a new cache exception.
	 * 
	 * @param t the t
	 */
	public CacheException(Throwable t) {
		super(t);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
