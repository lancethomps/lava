package com.github.lancethomps.lava.common.file;


/**
 * The Class FileSecurity.
 */
public class FileSecurity {

	/**
	 * Checks if is valid file path.
	 * 
	 * @param path the path
	 * @return the string
	 */
	public static String ensureAllowedPath(String path) {
		if (path.contains("..")) {
			throw new SecurityException("Directory traversal attempt using '..' detected for path [" + path + ']');
		}

		return path;
	}

}
