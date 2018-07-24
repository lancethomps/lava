package com.github.lancethomps.lava.common.string;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.os.OsUtil.runAndGetOutput;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.CommonConstants;
import com.github.lancethomps.lava.common.lambda.Lambdas;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.sorting.CaseInsensitiveStringSort;
import com.google.common.collect.Lists;

/** Some useful methods to be used with Strings. */
public class StringUtil {

	/** The Constant ALPHA_NUMERIC. */
	public static final Pattern ALPHA_NUMERIC = Pattern.compile("[A-Za-z0-9]");

	/** The Constant CAMEL_CASE_CAP_CHARS. */
	// public static final Pattern CAMEL_CASE_CAP_CHARS =
	// Pattern.compile("(?<=[a-z])(\\p{Lu})|(\\p{Lu})(?=[a-z])|(?<=[^a-zA-Z0-9])(\\w)|([^a-zA-Z0-9])");
	public static final Pattern CAMEL_CASE_CAP_CHARS = Pattern.compile("(?<=[a-z])(\\p{Lu})|(\\p{Lu})(?=[a-z])");

	/** The Constant CAP_AFTER_LOWER. */
	public static final Pattern CAP_AFTER_LOWER = Pattern.compile("(?<=[a-z])(\\p{Lu})");

	/** The Constant CAP_BEFORE_LOWER. */
	public static final Pattern CAP_BEFORE_LOWER = Pattern.compile("(\\p{Lu})(?=[a-z])");

	/** The Constant CASE_INSENSITIVE_COMP. */
	public static final Comparator<String> CASE_INSENSITIVE_COMP = CaseInsensitiveStringSort.INSTANCE;

	/** The Constant CONSONANTS. */
	public static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";

	/** The Constant CONSONANTS_ALL. */
	public static final String CONSONANTS_ALL = CONSONANTS + CONSONANTS.toLowerCase();

	/** The Constant CONTAINS_SPACES_REGEX. */
	public static final Pattern CONTAINS_SPACES_REGEX = Pattern.compile("[\\s]+");

	/** The Constant FIRST_CAMEL_PART_REGEX. */
	public static final Pattern FIRST_CAMEL_PART_REGEX = Pattern.compile("([a-z]+).*");

	/** The Constant LAST_CAMEL_PART_REGEX. */
	public static final Pattern LAST_CAMEL_PART_REGEX = Pattern.compile("\\w*([A-Z][^A-Z]+)$");

	/** The Constant LINE_BR_MATCHER. */
	public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("[\\r\\n]+");

	/** The Constant NAME_FALLBACK_REGEX. */
	public static final Pattern NAME_FALLBACK_REGEX = Pattern.compile("(.)(\\p{Lu})");

	/** The Constant ALPHA_NUMERIC. */
	public static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^A-Za-z0-9]");

	/** The Constant NON_ALPHA_NUMERIC_ALL. */
	public static final Pattern NON_ALPHA_NUMERIC_ALL = Pattern.compile("^[^A-Za-z0-9]+$");

	/** The Constant ALPHA_ONLY. */
	public static final Pattern NON_ALPHA_ONLY = Pattern.compile("[^A-Za-z]");

	/** The Constant NUMERIC_ONLY. */
	public static final Pattern NUMERIC_ONLY = Pattern.compile("[^0-9]");

	/** The Constant NUMERIC_STRING_REGEX. */
	public static final Pattern NUMERIC_STRING_REGEX = Pattern.compile("[0-9\\. \\-e]+", Pattern.CASE_INSENSITIVE);

	/** The Constant TAB_PATTERN. */
	public static final Pattern TAB_PATTERN = Pattern.compile("(\\t)+");

	/** The Constant TRUNCATE_STRING_ELLIPSIS. */
	public static final String TRUNCATE_STRING_ELLIPSIS = "...";

	/** The Constant VOWELS. */
	public static final String VOWELS = "AEIOU";

	/** The Constant VOWELS_ALL. */
	public static final String VOWELS_ALL = VOWELS + VOWELS.toLowerCase();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(StringUtil.class);

	/** The Constant RANDOM_ALPHA_CHARS. */
	private static final String RANDOM_ALPHA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	/** The Constant RANDOM_GEN. */
	private static final Random RANDOM_GEN;

	/** The Constant RANDOM_ID_CHARS. */
	private static final String RANDOM_ID_CHARS = RANDOM_ALPHA_CHARS + "0123456789";

	/** The Constant RANDOM_NUMERIC_CHARS. */
	private static final String RANDOM_NUMERIC_CHARS = "0123456789";

	static {
		Random random = null;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			random = new Random();
		}
		RANDOM_GEN = random;
	}

	/**
	 * Append.
	 *
	 * @param sb the sb
	 * @param str the str
	 * @return the string builder
	 */
	public static StringBuilder append(StringBuilder sb, String str) {
		return append(sb, str, true);
	}

	/**
	 * Append.
	 *
	 * @param sb the sb
	 * @param str the str
	 * @param withLineBreak the with line break
	 * @return the string builder
	 */
	public static StringBuilder append(StringBuilder sb, String str, boolean withLineBreak) {
		sb.append(str);
		if (withLineBreak) {
			sb.append(System.lineSeparator());
		}
		return sb;
	}

	/**
	 * Base 64 encode.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String base64Encode(String str) {
		return Base64.encodeBase64String(str.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Camel case to snake case.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String camelCaseToSnakeCase(String str) {
		if (str == null) {
			return str;
		}
		return camelToLowerWithSep(str, "_").toLowerCase();
	}

	/**
	 * Camel to lower with sep.
	 *
	 * @param str the str
	 * @param sep the sep
	 * @return the string
	 */
	public static String camelToLowerWithSep(String str, String sep) {
		return isBlank(str) ? str : str.substring(0, 1).toLowerCase() + NAME_FALLBACK_REGEX.matcher(str).replaceAll("$1" + Matcher.quoteReplacement(sep) + "$2").substring(1).toLowerCase();
	}

	/**
	 * Find level.
	 *
	 * @param line the line
	 * @param matchChar the match char
	 * @return the int
	 */
	public static int countMatchesAtStart(String line, char matchChar) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) != matchChar) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Creates the random string.
	 *
	 * @param length the length
	 * @return the string
	 */
	public static String createRandomString(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	/**
	 * Fix camel case.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String fixCamelCase(String str) {
		if (NON_ALPHA_NUMERIC_ALL.matcher(str).matches()) {
			return str;
		}
		return toCamelCase(trim(CAMEL_CASE_CAP_CHARS.matcher(NON_ALPHA_NUMERIC.matcher(str).replaceAll(" ")).replaceAll(" $1$2")), " ");
	}

	/**
	 * Format.
	 *
	 * @param str the str
	 * @param formatArgs the format args
	 * @return the string
	 */
	public static String format(String str, Object... formatArgs) {
		return (formatArgs != null) && (formatArgs.length > 0) ? String.format(str, formatArgs) : str;
	}

	/**
	 * From input stream.
	 *
	 * @param stream the stream
	 * @return the string
	 */
	public static String fromInputStream(InputStream stream) {
		String str = null;
		try {
			str = IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Logs.logError(LOG, e, "Issue reading input stream to string!");
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return str;
	}

	/**
	 * Generate md 5.
	 *
	 * @param data the data
	 * @return the string
	 */
	public static String generateMd5(Object data) {
		try {
			String val = data.toString();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(val.getBytes(StandardCharsets.UTF_8), 0, val.length());
			return new BigInteger(1, messageDigest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			Logs.logError(LOG, e, "No MD5 algorithm found!");
			return null;
		}
	}

	/**
	 * Generate random word.
	 *
	 * @return the string
	 */
	public static String generateRandomWord() {
		try {
			return trim(runAndGetOutput(asList("perl", "-e", "$dict = \"/usr/share/dict/words\"; $bytes= -s $dict; open IN, $dict;seek(IN,rand($bytes-11),0);$_=<IN>;$_=<IN>;print"), true));
		} catch (IOException e) {
			Logs.logError(LOG, e, "Issue generating random word!");
			return null;
		}
	}

	/**
	 * Generate random words.
	 *
	 * @param size the size
	 * @return the list
	 */
	public static List<String> generateRandomWords(long size) {
		return Lambdas.iterate(size, StringUtil::generateRandomWord).collect(Collectors.toList());
	}

	/**
	 * Generate sha 1.
	 *
	 * @param data the data
	 * @return the string
	 */
	public static String generateSha1(Object data) {
		try {
			String val = data.toString();
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(val.getBytes(StandardCharsets.UTF_8), 0, val.length());
			return new BigInteger(1, messageDigest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			Logs.logError(LOG, e, "No MD5 algorithm found!");
			return null;
		}
	}

	/**
	 * Generate unique id.
	 *
	 * @param length the length
	 * @return the string
	 */
	public static String generateUniqueId(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	/**
	 * Generate unique id.
	 *
	 * @param length the length
	 * @param currentIds the current ids
	 * @return the string
	 */
	public static String generateUniqueId(int length, Set<String> currentIds) {
		String id = null;
		while ((id == null) || currentIds.contains(id)) {
			id = generateUniqueId(length);
		}
		return id;
	}

	/**
	 * Gets the random numeric string.
	 *
	 * @param length the length
	 * @return the random numeric string
	 */
	public static final String getRandomNumericString(long length) {
		String val = new Random()
			.ints(0, RANDOM_NUMERIC_CHARS.length())
			.mapToObj(i -> RANDOM_NUMERIC_CHARS.charAt(i))
			.limit(length)
			.collect(
				StringBuilder::new,
				StringBuilder::append,
				StringBuilder::append
			)
			.toString();
		if ("0".equalsIgnoreCase(val.substring(0, 1))) {
			val = getRandomNumericString(1) + val.substring(1);
		}
		return val;
	}

	/**
	 * Gets the random vowel consonant string.
	 *
	 * @param length the length
	 * @return the random vowel consonant string
	 */
	public static final String getRandomVowelConsonantString(long length) {
		StringBuilder str = new StringBuilder(RandomStringUtils.randomAlphabetic(1));
		Random random = new Random();
		while (str.length() < length) {
			if (CONSONANTS_ALL.contains(new Character(str.charAt(str.length() - 1)).toString())) {
				str.append(VOWELS_ALL.charAt(random.nextInt(VOWELS_ALL.length())));
			} else {
				str.append(CONSONANTS_ALL.charAt(random.nextInt(CONSONANTS_ALL.length())));
			}
		}
		return str.toString();
	}

	/**
	 * Insert string after chars.
	 *
	 * @param str the str
	 * @param insertStr the insert str
	 * @param pos the pos
	 * @return the string
	 */
	public static String insertStringAfterChars(String str, String insertStr, int pos) {
		if (isBlank(str)) {
			return str;
		}
		String regex = "([\\S\\s]{0," + pos + "})(\\s|$)";
		if (!CONTAINS_SPACES_REGEX.matcher(str).find() && (str.length() > pos)) {
			regex = "([\\S\\s]{" + Double.valueOf(pos * 0.8).intValue() + "})(\\s|\\S|$)";
		}
		return str.replaceAll(regex, "$1$2" + insertStr).trim();
	}

	/**
	 * Checks if is numeric.
	 *
	 * @param val the val
	 * @return true, if is numeric
	 */
	public static boolean isNumeric(String val) {
		return isNotBlank(val) && NUMERIC_STRING_REGEX.matcher(val).matches();
	}

	/**
	 * Last char.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String lastChar(String str) {
		return str == null ? null : String.valueOf(str.charAt(str.length() - 1));
	}

	/**
	 * Lower first.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String lowerFirst(String str) {
		if (StringUtils.isNotBlank(str)) {
			str = str.substring(0, 1).toLowerCase() + str.substring(1);
		}
		return str;
	}

	/**
	 * Object to string.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	public static String objectToString(Object obj) {
		return ToStringBuilder.reflectionToString(obj, CommonConstants.DEFAULT_TO_STRING_STYLE);
	}

	/**
	 * Pad left.
	 *
	 * @param str the str
	 * @param padding the padding
	 * @return the string
	 */
	public static String padLeft(String str, int padding) {
		return String.format("%1$" + padding + 's', str);
	}

	/**
	 * Pad right.
	 *
	 * @param str the str
	 * @param padding the padding
	 * @return the string
	 */
	public static String padRight(String str, int padding) {
		return String.format("%1$-" + padding + 's', str);
	}

	/**
	 * Removes the all line breaks.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static final String removeAllLineBreaks(String str) {
		return replaceAllLineBreaks(str, EMPTY);
	}

	/**
	 * Replace all line breaks.
	 *
	 * @param str the str
	 * @param replacement the replace with
	 * @return the string
	 */
	public static final String replaceAllLineBreaks(String str, String replacement) {
		return LINE_BREAK_PATTERN.matcher(str).replaceAll(replacement);
	}

	/**
	 * Replace all tabs.
	 *
	 * @param str the str
	 * @param replacement the replacement
	 * @return the string
	 */
	public static final String replaceAllTabs(String str, String replacement) {
		return TAB_PATTERN.matcher(str).replaceAll(replacement);
	}

	/**
	 * Replace non alpha.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String replaceNonAlpha(String str) {
		return NON_ALPHA_ONLY.matcher(str).replaceAll(EMPTY);
	}

	/**
	 * Replace non alpha numeric.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String replaceNonAlphaNumeric(String str) {
		return NON_ALPHA_NUMERIC.matcher(str).replaceAll(EMPTY);
	}

	/**
	 * Replace non numeric.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String replaceNonNumeric(String str) {
		return NUMERIC_ONLY.matcher(str).replaceAll(EMPTY);
	}

	/**
	 * Split key value.
	 *
	 * @param line the line
	 * @param split the split
	 * @return the pair
	 */
	public static Pair<String, String> splitKeyValue(String line, String split) {
		line = line.trim();
		int index = line.indexOf(split);
		String key = line.substring(0, index);
		String value = line.substring(index + 1);
		return Pair.of(key, value);
	}

	/**
	 * Split lines.
	 *
	 * @param str the str
	 * @return the list
	 */
	public static final List<String> splitLines(String str) {
		return Lists.newArrayList(LINE_BREAK_PATTERN.split(str));
	}

	/**
	 * Starts with.
	 *
	 * @param str the str
	 * @param checks the checks
	 * @return true, if successful
	 */
	public static boolean startsWith(String str, Collection<String> checks) {
		return (checks == null) || (str == null) ? false : checks.stream().anyMatch(check -> str.startsWith(check));
	}

	/**
	 * To camel case.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String toCamelCase(String str) {
		return toCamelCase(str, "_");
	}

	/**
	 * To camel case.
	 *
	 * @param str the str
	 * @param replace the replace
	 * @return the string
	 */
	public static String toCamelCase(String str, String replace) {
		if (isNotBlank(str)) {
			if (str.contains(replace)) {
				str = str.toLowerCase().replace(replace, " ");
				String start = substringBefore(str, " ");
				String end = substringAfter(str, " ");
				end = WordUtils.capitalizeFully(end);
				str = start + end.replace(" ", "");
				str = str.trim();
			} else {
				str = str.toLowerCase().trim();
			}
		}
		return str;
	}

	/**
	 * To comma separated string.
	 *
	 * @param inputList the input list
	 * @return the string
	 */
	public static final String toCommaSeparatedString(int[] inputList) {
		return toCommaSeparatedString(inputList, "");
	}

	/**
	 * To comma separated string.
	 *
	 * @param inputList the input list
	 * @param toRemove the to remove
	 * @return the string
	 */
	public static final String toCommaSeparatedString(int[] inputList, String toRemove) {
		if (isNotEmpty(inputList)) {
			final String[] stringArr = new String[inputList.length];
			for (int i = 0; i < inputList.length; i++) {
				stringArr[i] = String.valueOf(inputList[i]);
			}
			return toCommaSeparatedString(asList(stringArr), toRemove);
		}
		return "";
	}

	/**
	 * Converts List<String> to comma-separated String.
	 *
	 * @param inputList List<String>
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(List<String> inputList) {
		return toCommaSeparatedString(inputList, "");
	}

	/**
	 * Converts List<String> to comma-separated String, and removes Not Specified, if any.
	 *
	 * @param inputList List<String>
	 * @param toRemove the to remove
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(List<String> inputList, String toRemove) {
		if (isNotEmpty(inputList)) {
			StringBuilder builderString = new StringBuilder();
			String temp = null;
			boolean isAdded = false;
			int listSize = inputList.size();
			for (int i = 0; i < listSize; i++) {
				temp = String.valueOf(inputList.get(i));
				if (isBlank(temp) || temp.equals(toRemove)) {
					continue;
				}
				if (isAdded) {
					builderString.append(",");
				}
				builderString.append(temp);
				isAdded = true;
			}
			return builderString.toString();
		}
		return "";
	}

	/**
	 * Converts Set<String> to comma-separated String.
	 *
	 * @param inputList Set<String>
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(Set<String> inputList) {
		return toCommaSeparatedString(inputList, "");
	}

	/**
	 * Converts Set<String> to comma-separated String, and removes Not Specified, if any.
	 *
	 * @param inputList Set<String>
	 * @param toRemove the to remove
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(Set<String> inputList, String toRemove) {
		if (inputList != null) {
			return toCommaSeparatedString(new ArrayList<String>(inputList), toRemove);
		}
		return "";
	}

	/**
	 * Converts String[] to comma-separated String.
	 *
	 * @param inputList String[]
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(String[] inputList) {
		return toCommaSeparatedString(inputList, "");
	}

	/**
	 * Converts String[] to comma-separated String.
	 *
	 * @param inputList String[]
	 * @param toRemove the to remove
	 * @return String comma-separated list
	 */
	public static final String toCommaSeparatedString(String[] inputList, String toRemove) {
		if (inputList != null) {
			return toCommaSeparatedString(asList(inputList), toRemove);
		}
		return "";
	}

	/**
	 * To name fallback.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String toNameFallback(String str) {
		return NAME_FALLBACK_REGEX.matcher(capitalize(str)).replaceAll("$1 $2");
	}

	/**
	 * Truncate string if needed.
	 *
	 * @param str the str
	 * @param maxLength the max length
	 * @return the string
	 */
	public static String truncateStringIfNeeded(final String str, final int maxLength) {
		return truncateStringIfNeeded(str, maxLength, null);
	}

	/**
	 * Truncate string if needed.
	 *
	 * @param str the str
	 * @param maxLength the max length
	 * @param appendSuffixIfTruncated the append suffix if truncated
	 * @return the string
	 */
	public static String truncateStringIfNeeded(final String str, final int maxLength, @Nullable String appendSuffixIfTruncated) {
		return str == null ? null : str.length() > maxLength ? (str.substring(0, maxLength) + StringUtils.defaultString(appendSuffixIfTruncated)) : str;
	}

}
