package com.github.lancethomps.lava.common.file;

import static com.github.lancethomps.lava.common.logging.Logs.logWarn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class AbstractFileListener.
 */
public abstract class AbstractFileListener implements Listener {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(AbstractFileListener.class);

	/** The root dir. */
	private String baseDir;

	/** The file load callbacks. */
	private List<ThrowingConsumer<File>> fileLoadCallbacks = new ArrayList<>();

	/** The config. */
	private ListenerConfiguration listenerConfiguration;

	/**
	 * Adds the file load callback.
	 *
	 * @param callback the callback
	 */
	public void addFileLoadCallback(final ThrowingConsumer<File> callback) {
		synchronized (fileLoadCallbacks) {
			fileLoadCallbacks.add(callback);
		}
	}

	/**
	 * After properties set.
	 *
	 * @throws Exception the exception
	 */
	public void afterBaseDirSet() throws Exception {
		// Override this for PostConstruct logic
	}

	/**
	 * @return the baseDir
	 */
	public String getBaseDir() {
		return baseDir;
	}

	@Override
	public List<ThrowingConsumer<File>> getFileLoadCallbacks() {
		return fileLoadCallbacks;
	}

	/**
	 * @return the config
	 */
	public ListenerConfiguration getListenerConfiguration() {
		return listenerConfiguration;
	}

	@Override
	public void handleFileDelete(File file) {
	}

	@Override
	public void handleFileLoad(File file) {
		Logs.logError(LOG, new Exception(), "Listeners should overwrite the handleFileLoad method! The file [%s] was not loaded due to no available method.", file);
	}

	/**
	 * Load all files in dir.
	 *
	 * @param dir the dir
	 */
	public void loadAllFilesInDir(File dir) {
		if ((dir != null) && dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if ((files != null) && (files.length > 0)) {
				Stream.of(files).filter(File::isFile).peek(file -> logWarn(LOG, "Loading file [%s] with listener [%s]", getRelativePathForFile(file), getClass().getSimpleName())).forEach(
					this::handleFileLoad
				);
				if (listenerConfiguration.isIncludeSubDirs()) {
					Stream.of(files).filter(File::isDirectory).forEach(this::loadAllFilesInDir);
				}
			}
		}
	}

	/**
	 * @param baseDir the baseDir to set
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Sets the listener configuration.
	 *
	 * @param listenerConfiguration the new listener configuration
	 */
	public void setListenerConfiguration(ListenerConfiguration listenerConfiguration) {
		this.listenerConfiguration = listenerConfiguration;
	}

	/**
	 * Gets the relative path for file.
	 *
	 * @param file the file
	 * @return the relative path for file
	 */
	private String getRelativePathForFile(File file) {
		if (Checks.isNotBlank(baseDir)) {
			return StringUtils.removeStart(FileUtil.fullPath(file), baseDir + File.separatorChar);
		}
		return file.getName();
	}

}
