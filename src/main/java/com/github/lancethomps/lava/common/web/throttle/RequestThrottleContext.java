package com.github.lancethomps.lava.common.web.throttle;

/**
 * The Interface RequestThrottleContext.
 *
 * @author lathomps
 */
public interface RequestThrottleContext {

	/**
	 * Gets the resource identifier.
	 *
	 * @return the resource identifier
	 */
	String getResourceIdentifier();

	/**
	 * Gets the sender identifier.
	 *
	 * @return the sender identifier
	 */
	String getSenderIdentifier();
}
