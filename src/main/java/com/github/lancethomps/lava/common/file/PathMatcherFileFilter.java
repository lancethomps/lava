package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * The Class PathMatcherFileFilter.
 */
public class PathMatcherFileFilter extends AbstractFileFilter {

	/** The base dir. */
	private final File baseDir;

	/** The black list. */
	private final Collection<String> blackList;

	/** The black list matchers. */
	private final List<PathMatcher> blackListMatchers;

	/** The has white list. */
	private final boolean hasWhiteList;

	/** The white list. */
	private final Collection<String> whiteList;

	/** The white list matchers. */
	private final List<PathMatcher> whiteListMatchers;

	/**
	 * Instantiates a new glob file filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 */
	public PathMatcherFileFilter(Collection<String> whiteList, Collection<String> blackList) {
		this(whiteList, blackList, null);
	}

	/**
	 * Instantiates a new glob file filter.
	 *
	 * @param whiteList the white list
	 * @param blackList the black list
	 * @param baseDir the base dir
	 */
	public PathMatcherFileFilter(Collection<String> whiteList, Collection<String> blackList, File baseDir) {
		super();
		this.whiteList = whiteList;
		this.blackList = blackList;
		this.baseDir = baseDir;

		whiteListMatchers = (whiteList == null) || whiteList.isEmpty() ? null
			: whiteList
				.stream()
				.map(val -> File.separatorChar != '/' ? StringUtils.replace(val, "/", File.separator) : val)
				.map(val -> {
					if (!StringUtils.startsWithAny(val, "glob:", "regex:")) {
						return "glob:" + val;
					}
					return val;
				})
				.map(FileSystems.getDefault()::getPathMatcher)
				.collect(Collectors.toList());
		blackListMatchers = (blackList == null) || blackList.isEmpty() ? null
			: blackList
				.stream()
				.map(val -> File.separatorChar != '/' ? StringUtils.replace(val, "/", File.separator) : val)
				.map(val -> {
					if (!StringUtils.startsWithAny(val, "glob:", "regex:")) {
						return "glob:" + val;
					}
					return val;
				})
				.map(FileSystems.getDefault()::getPathMatcher)
				.collect(Collectors.toList());

		hasWhiteList = (whiteListMatchers != null) && !whiteListMatchers.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.io.filefilter.AbstractFileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File file) {
		final Path path = baseDir == null ? file.toPath() : new File(FileUtil.getRelativePath(baseDir, file)).toPath();
		if (blackListMatchers != null) {
			for (PathMatcher matcher : blackListMatchers) {
				if (matcher.matches(path)) {
					return false;
				}
			}
		}
		if (hasWhiteList) {
			for (PathMatcher matcher : whiteListMatchers) {
				if (matcher.matches(path)) {
					return true;
				}
			}
		}
		return hasWhiteList ? false : true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.io.filefilter.AbstractFileFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(final File dir, final String name) {
		return accept(new File(dir, name));
	}

}
