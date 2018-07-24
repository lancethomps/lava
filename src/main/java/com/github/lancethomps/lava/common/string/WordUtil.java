package com.github.lancethomps.lava.common.string;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class WordUtil.
 */
public class WordUtil {

	/**
	 * Gets the singular version of word.
	 *
	 * @param word the word
	 * @return the singular version of word
	 */
	public static String getSingularVersionOfWord(String word) {
		if (word.endsWith("s")) {
			return word.endsWith("ies") ? StringUtils.removeEnd(word, "ies") + 'y' : StringUtils.removeEnd(word, "s");
		}
		return null;
	}

	/**
	 * Upper case if lower.
	 *
	 * @param word the word
	 * @return the string
	 */
	public static String upperCaseIfLower(String word) {
		if (StringUtils.isNotBlank(word) && Character.isLowerCase(word.charAt(0))) {
			return StringUtils.upperCase(word);
		}
		return word;
	}
}
