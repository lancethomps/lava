package com.github.lancethomps.lava.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.github.lancethomps.lava.common.string.StringUtil;

/**
 * The Class Patterns.
 */
public class Patterns {

	/** The Constant COMBINED_ACRONYM_REGEX_SHELL. */
	private static final String COMBINED_ACRONYM_REGEX_SHELL = "((?<=[^\\w])([A-Z][\\da-z]*\\b(?:[\\s,]|&\\w+;|& |[Aa][Nn][Dd]|[Oo][Rr])*){${MIN_LENGTH},}|([A-Z]{${MIN_LENGTH},}))";

	/** The Constant GROUP_REPLACE_REGEX. */
	private static final Pattern GROUP_REPLACE_REGEX = Pattern.compile("(?<!\\\\)(\\$|\\\\)(\\d)");

	/** The Constant MIN_LENGTH_PLACEHOLDER. */
	private static final String MIN_LENGTH_PLACEHOLDER = "${MIN_LENGTH}";

	/** The Constant SIMPLE_ACRONYM_REGEX_SHELL. */
	private static final String SIMPLE_ACRONYM_REGEX_SHELL = "([A-Z]{${MIN_LENGTH},})";

	/**
	 * As options.
	 *
	 * @param flags the flags
	 * @return the int
	 */
	public static int asOptions(String flags) {
		int options = 0;
		if (flags != null) {
			if (flags.indexOf('i') != -1) {
				options |= Pattern.CASE_INSENSITIVE;
			}
			if (flags.indexOf('m') != -1) {
				options |= Pattern.MULTILINE;
			}
			if (flags.indexOf('s') != -1) {
				options |= Pattern.DOTALL;
			}
		}
		return options;
	}

	/**
	 * Creates the acronym search pattern.
	 *
	 * @param acronym the acronym
	 * @return the pattern
	 */
	public static final Pattern createAcronymSearchPattern(String acronym) {
		StringBuilder regex = new StringBuilder("(?<=[^\\w])(");
		String addOnRegex = "\\b(?:[\\s,]|&\\w+;|[Aa][Nn][Dd])*";
		String regexCharSuffix = "[\\da-z]*";
		for (int pos = 0; pos < acronym.length(); pos++) {
			String letter = String.valueOf(acronym.charAt(pos));
			if (pos > 0) {
				regex.append(addOnRegex);
			}
			regex.append('[').append(letter).append(']').append(regexCharSuffix);
		}
		regex.append(")");
		return Pattern.compile(regex.toString());
	}

	/**
	 * Find splunk log key value matches.
	 *
	 * @param extractor the extractor
	 * @param input the input
	 * @param keyGroups the key groups
	 * @param valueGroups the value groups
	 * @param valueConverter the value converter
	 * @return the map
	 */
	public static Map<String, String> findKeyValueMatches(
		@Nonnull Pattern extractor,
		@Nonnull String input,
		@Nonnull Collection<Integer> keyGroups,
		@Nonnull Collection<Integer> valueGroups,
		@Nullable Function<String, String> valueConverter
	) {
		final Map<String, String> matches = new LinkedHashMap<>();
		final Matcher matcher = extractor.matcher(input);
		while (matcher.find()) {
			final String foundKey = keyGroups.stream().mapToInt(Integer::intValue).mapToObj(matcher::group).filter(Checks::isNotBlank).findFirst().orElse("");
			final String value = valueGroups
				.stream()
				.mapToInt(Integer::intValue)
				.mapToObj(matcher::group)
				.filter(Checks::isNotBlank)
				.map(v -> valueConverter == null ? v : valueConverter.apply(v))
				.findFirst()
				.orElse("");
			String key = foundKey;
			int append = 1;
			String existingValue;
			while ((existingValue = matches.get(key)) != null) {
				if (existingValue.equals(value)) {
					break;
				}
				key = foundKey + '.' + append;
				append++;
			}
			matches.put(key, value);
		}
		return matches;
	}

	/**
	 * Find matches.
	 *
	 * @param regex the regex
	 * @param inputs the inputs
	 * @return the list
	 */
	public static List<Matcher> findMatches(Pattern regex, Collection<String> inputs) {
		List<Matcher> matches = new ArrayList<>();
		for (String input : inputs) {
			Matcher matcher = regex.matcher(input);
			if (matcher.find()) {
				matches.add(matcher);
			}
		}
		return matches;
	}

	/**
	 * Find matches and extract.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @return the list
	 */
	public static List<String> findMatchesAndExtract(@Nonnull Pattern regex, String input) {
		return findMatchesAndExtract(regex, input, null, null);
	}

	/**
	 * Find matches and extract.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param group the group
	 * @return the list
	 */
	public static List<String> findMatchesAndExtract(@Nonnull Pattern regex, String input, @Nullable Integer group) {
		return findMatchesAndExtract(regex, input, group, null);
	}

	/**
	 * Find matches and extract.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param groupName the group name
	 * @return the list
	 */
	public static List<String> findMatchesAndExtract(@Nonnull Pattern regex, String input, @Nullable String groupName) {
		return findMatchesAndExtract(regex, input, null, groupName);
	}

	/**
	 * Find matches and replace.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param replacement the replacement
	 * @return the list
	 */
	public static List<String> findMatchesAndReplace(@Nonnull Pattern regex, String input, @Nonnull String replacement) {
		return findMatchesAndExtract(regex, input, null, null, replacement);
	}

	/**
	 * Find simple acronyms.
	 *
	 * @param minLength the min length
	 * @param searchString the search string
	 * @return the sets the
	 */
	@Nonnull
	public static Set<String> findSimpleAcronyms(int minLength, @Nonnull String searchString) {
		final Matcher acronymMatcher = Pattern.compile(StringUtils.replace(SIMPLE_ACRONYM_REGEX_SHELL, MIN_LENGTH_PLACEHOLDER, String.valueOf(minLength))).matcher(searchString);
		final Set<String> acronyms = new LinkedHashSet<>();
		while (acronymMatcher.find()) {
			acronyms.add(acronymMatcher.group(1));
		}
		return acronyms;
	}

	/**
	 * Find simple acronyms.
	 *
	 * @param searchString the search string
	 * @return the sets the
	 */
	@Nonnull
	public static Set<String> findSimpleAcronyms(@Nonnull String searchString) {
		return findSimpleAcronyms(3, searchString);
	}

	/**
	 * Find simple acronyms and possible acronyms.
	 *
	 * @param minLength the min length
	 * @param searchString the search string
	 * @return the sets the
	 */
	@Nonnull
	public static Set<String> findSimpleAcronymsAndPossibleAcronyms(int minLength, @Nonnull String searchString) {
		assert minLength > 0;
		final Matcher acronymMatcher = Pattern.compile(StringUtils.replace(COMBINED_ACRONYM_REGEX_SHELL, MIN_LENGTH_PLACEHOLDER, String.valueOf(minLength))).matcher(searchString);
		final Set<String> acronyms = new LinkedHashSet<>();
		while (acronymMatcher.find()) {
			String found = acronymMatcher.group(3); // NB: group 3 is the simple acronym
			if (Checks.isNotBlank(found)) {
				acronyms.add(found);
			} else {
				// NB: group 1 is the full possible acronym match
				acronyms.add(
					Stream
						.of(acronymMatcher.group(1).split(" "))
						.map(word -> StringUtils.trimToNull(StringUtil.NON_ALPHA_NUMERIC.matcher(word).replaceAll("")))
						.filter(Checks::nonNull)
						.map(word -> word.substring(0, 1))
						.reduce(String::concat)
						.get()
				);
			}
		}
		return acronyms;
	}

	/**
	 * Gets the group.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param group the group
	 * @return the group
	 */
	public static String getGroup(Pattern regex, String input, int group) {
		Matcher matcher = regex.matcher(input);
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}

	/**
	 * Checks if is valid regex.
	 *
	 * @param regex the regex
	 * @return the pair
	 */
	public static Pair<Boolean, String> isValidRegex(String regex) {
		try {
			Pattern.compile(regex);
			return Pair.of(true, null);
		} catch (PatternSyntaxException e) {
			return Pair.of(false, e.getMessage());
		}
	}

	/**
	 * Replace.
	 *
	 * @param matcher the matcher
	 * @param replacement the replacement
	 * @return the string
	 */
	public static String replace(@Nonnull Matcher matcher, @Nonnull String replacement) {
		boolean hasGroups = false;
		Matcher groupMatcher = GROUP_REPLACE_REGEX.matcher(replacement);
		StringBuffer sb = new StringBuffer();
		while (groupMatcher.find()) {
			hasGroups = true;
			int group = Integer.parseInt(groupMatcher.group(2));
			String groupReplacement = null;
			if (matcher.groupCount() >= group) {
				groupReplacement = matcher.group(group);
			}
			groupMatcher.appendReplacement(sb, Matcher.quoteReplacement(StringUtils.defaultString(groupReplacement)));
		}
		if (hasGroups) {
			return groupMatcher.appendTail(sb).toString();
		}
		return replacement;
	}

	/**
	 * Find matches and extract.
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param group the group
	 * @param groupName the group name
	 * @return the list
	 */
	private static List<String> findMatchesAndExtract(@Nonnull Pattern regex, String input, @Nullable Integer group, @Nullable String groupName) {
		return findMatchesAndExtract(regex, input, group, groupName, null);
	}

	/**
	 * Find matches and extract.
	 * <p>
	 * <b>Only one of {@code group} or {@code groupName} should be specified</b>
	 *
	 * @param regex the regex
	 * @param input the input
	 * @param group the group
	 * @param groupName the group name
	 * @param replacement the replacement
	 * @return the list
	 */
	private static List<String> findMatchesAndExtract(@Nonnull Pattern regex, String input, @Nullable Integer group, @Nullable String groupName, @Nullable String replacement) {
		if (input == null) {
			return null;
		}
		assert (group == null) || (groupName == null);
		List<String> matches = new ArrayList<>();
		Matcher matcher = regex.matcher(input);
		while (matcher.find()) {
			String match;
			if (replacement != null) {
				match = replace(matcher, replacement);
			} else if (group != null) {
				match = matcher.group(group);
			} else if (groupName != null) {
				match = matcher.group(groupName);
			} else {
				match = matcher.group();
			}
			matches.add(match);
		}
		return matches;
	}
}
