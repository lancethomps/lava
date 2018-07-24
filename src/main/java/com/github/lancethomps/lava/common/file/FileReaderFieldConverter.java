package com.github.lancethomps.lava.common.file;

/**
 * The Interface FileReaderFieldConverter.
 */
public interface FileReaderFieldConverter {

	/**
	 * Convert.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the string
	 */
	Object convert(String key, Object value);
}
