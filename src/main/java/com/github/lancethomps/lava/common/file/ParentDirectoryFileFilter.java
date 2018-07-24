package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * The Class ParentDirectoryFileFilter.
 */
public class ParentDirectoryFileFilter extends AbstractFileFilter implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2169482506424144174L;

	/** The parent dir. */
	private File parentDir;

	/**
	 * Instantiates a new parent directory file filter.
	 *
	 * @param parentDir the parent dir
	 */
	public ParentDirectoryFileFilter(File parentDir) {
		this.parentDir = parentDir;
	}

	@Override
	public boolean accept(File file) {
		return FileUtil.fullPath(parentDir).equalsIgnoreCase(FileUtil.fullPath(file.getParentFile()));
	}
}
