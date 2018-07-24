package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.util.List;

import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;

/**
 * The Interface Listener.
 */
public interface Listener {

	/**
	 * Gets the file load callbacks.
	 *
	 * @return the file load callbacks
	 */
	default List<ThrowingConsumer<File>> getFileLoadCallbacks() {
		return null;
	}

	/**
	 * Notify file delete.
	 *
	 * @param file the file
	 */
	void handleFileDelete(File file);

	/**
	 * Handle file load.
	 *
	 * @param file the file
	 */
	void handleFileLoad(File file);
}