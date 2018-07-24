package com.github.lancethomps.lava.common.file;

/**
 * The Interface StringConverter.
 */
@FunctionalInterface
public interface CsvFieldConverter {

	/**
	 * Convert string.
	 *
	 * @param orig the orig
	 * @param description the description
	 * @return the object
	 * @throws Exception the exception
	 */
	Object convertObject(Object orig, String description) throws Exception;
}
