package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import com.github.lancethomps.lava.common.Checks;

/**
 * The Class WhiteAndBlackListFileFilter.
 */
public class WhiteAndBlackListFileFilter extends AbstractFileFilter implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7539720980246540828L;

	/** The black list. */
	private final Collection<String> blackList;

	/** The black list patterns. */
	private final Collection<Pattern> blackListPatterns;

	/** The white list. */
	private final Collection<String> whiteList;

	/** The white list patterns. */
	private final Collection<Pattern> whiteListPatterns;

	/** The white list priority. */
	private final boolean whiteListPriority;

	/**
	 * Instantiates a new white and black list file filter.
	 *
	 * @param whiteListPatterns the white list patterns
	 * @param blackListPatterns the black list patterns
	 */
	public WhiteAndBlackListFileFilter(Collection<Pattern> whiteListPatterns, Collection<Pattern> blackListPatterns) {
		this(null, null, whiteListPatterns, blackListPatterns);
	}

	/**
	 * Instantiates a new white and black list file filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPatterns the white list patterns
	 * @param blackListPatterns the black list patterns
	 */
	public WhiteAndBlackListFileFilter(Collection<String> whiteList, Collection<String> blackList, Collection<Pattern> whiteListPatterns, Collection<Pattern> blackListPatterns) {
		this(whiteList, blackList, whiteListPatterns, blackListPatterns, false);
	}

	/**
	 * Instantiates a new white and black list file filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param whiteListPatterns the white list patterns
	 * @param blackListPatterns the black list patterns
	 * @param whiteListPriority the white list priority
	 */
	public WhiteAndBlackListFileFilter(Collection<String> whiteList, Collection<String> blackList, Collection<Pattern> whiteListPatterns, Collection<Pattern> blackListPatterns,
		boolean whiteListPriority) {
		super();
		this.whiteList = whiteList;
		this.blackList = blackList;
		this.whiteListPatterns = whiteListPatterns;
		this.blackListPatterns = blackListPatterns;
		this.whiteListPriority = whiteListPriority;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.io.filefilter.AbstractFileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File file) {
		return Checks.passesWhiteAndBlackListCheck(FileUtil.fullPath(file), whiteList, blackList, whiteListPatterns, blackListPatterns, whiteListPriority).getLeft();
	}

}
