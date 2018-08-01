package com.github.lancethomps.lava.common.file;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * The Class WildcardPathFileFilter.
 *
 * @author lancethomps
 */
public class WildcardPathFileFilter extends WildcardFileFilter {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6185684705917032266L;

	/** The case sensitivity. */
	private final IOCase caseSensitivity;

	/** The wildcards. */
	private final String[] wildcards;

	/**
	 * Instantiates a new wildcard path file filter.
	 *
	 * @param wildcards the wildcards
	 */
	public WildcardPathFileFilter(final List<String> wildcards) {
		this(wildcards, IOCase.SENSITIVE);
	}

	/**
		 * Instantiates a new wildcard path file filter.
		 *
		 * @param wildcards the wildcards
		 * @param caseSensitivity the case sensitivity
		 */
	public WildcardPathFileFilter(final List<String> wildcards, final IOCase caseSensitivity) {
		super(wildcards, caseSensitivity);
		if (wildcards == null) {
			throw new IllegalArgumentException("The wildcard list must not be null");
		}
		this.wildcards = wildcards.toArray(new String[wildcards.size()]);
		this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	}

	/**
		 * Instantiates a new wildcard path file filter.
		 *
		 * @param wildcard the wildcard
		 */
	public WildcardPathFileFilter(final String wildcard) {
		this(wildcard, IOCase.SENSITIVE);
	}

	/**
		 * Instantiates a new wildcard path file filter.
		 *
		 * @param wildcard the wildcard
		 * @param caseSensitivity the case sensitivity
		 */
	public WildcardPathFileFilter(final String wildcard, final IOCase caseSensitivity) {
		super(wildcard, caseSensitivity);
		if (wildcard == null) {
			throw new IllegalArgumentException("The wildcard must not be null");
		}
		wildcards = new String[] { wildcard };
		this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	}

	/**
		 * Instantiates a new wildcard path file filter.
		 *
		 * @param wildcards the wildcards
		 */
	public WildcardPathFileFilter(final String[] wildcards) {
		this(wildcards, IOCase.SENSITIVE);
	}

	/**
		 * Instantiates a new wildcard path file filter.
		 *
		 * @param wildcards the wildcards
		 * @param caseSensitivity the case sensitivity
		 */
	public WildcardPathFileFilter(final String[] wildcards, final IOCase caseSensitivity) {
		super(wildcards, caseSensitivity);
		if (wildcards == null) {
			throw new IllegalArgumentException("The wildcard array must not be null");
		}
		this.wildcards = new String[wildcards.length];
		System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
		this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.io.filefilter.AbstractFileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File file) {
		final String path = FileUtil.fullPath(file);
		for (final String wildcard : wildcards) {
			if (FilenameUtils.wildcardMatch(path, wildcard, caseSensitivity)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.io.filefilter.AbstractFileFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(final File dir, final String name) {
		return accept(new File(dir, name));
	}
}
