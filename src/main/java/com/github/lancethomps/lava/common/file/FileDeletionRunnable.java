package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class FileDeletionRunnable.
 */
public class FileDeletionRunnable extends TimerTask {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(FileDeletionRunnable.class);

	/** The path. */
	private String path;

	/** The keep file. */
	private String keepFile;

	/** The move to dir. */
	private String moveToDir;

	/**
	 * Instantiates a new file deletion runnable.
	 *
	 * @param path the path
	 */
	public FileDeletionRunnable(String path) {
		this(path, null, null);
	}

	/**
	 * Instantiates a new file deletion runnable.
	 *
	 * @param path the path
	 * @param keepFile the keep file
	 * @param moveToDir the move to dir
	 */
	public FileDeletionRunnable(String path, String keepFile, String moveToDir) {
		super();
		this.path = path;
		this.keepFile = keepFile;
		this.moveToDir = moveToDir;
	}

	@Override
	public void run() {
		if ((keepFile != null) && (moveToDir != null)) {
			File fullPermsFile = new File(keepFile);
			if (fullPermsFile.exists()) {
				File newFile = new File(moveToDir, fullPermsFile.getName());
				FileUtil.moveFile(fullPermsFile, newFile);
				FileUtil.setFullPermsRecursive(newFile, false);
			}
		}
		try {
			File file = new File(path);
			if (file.isDirectory()) {
				FileUtils.deleteDirectory(file);
			} else {
				Logs.logWarn(LOG, "File to delete is null or doesn't exist: file=%s", FileUtil.fullPath(file));
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Could not delete file at path [%s].", path);
		}
	}

}
