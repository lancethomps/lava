package com.lancethomps.lava.common.string;

import static com.lancethomps.lava.common.Checks.isNotEmpty;
import static com.lancethomps.lava.common.os.OsUtil.runAndGetOutput;
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

import com.google.common.collect.Lists;
import com.lancethomps.lava.common.CommonConstants;
import com.lancethomps.lava.common.lambda.Lambdas;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.sorting.CaseInsensitiveStringSort;

public class StringUtil {

  public static final Pattern ALPHA_NUMERIC = Pattern.compile("[A-Za-z0-9]");
  public static final Pattern CAMEL_CASE_CAP_CHARS = Pattern.compile("(?<=[a-z])(\\p{Lu})|(\\p{Lu})(?=[a-z])");
  public static final Pattern CAP_AFTER_LOWER = Pattern.compile("(?<=[a-z])(\\p{Lu})");
  public static final Pattern CAP_BEFORE_LOWER = Pattern.compile("(\\p{Lu})(?=[a-z])");
  public static final Comparator<String> CASE_INSENSITIVE_COMP = CaseInsensitiveStringSort.INSTANCE;
  public static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";
  public static final String CONSONANTS_ALL = CONSONANTS + CONSONANTS.toLowerCase();
  public static final Pattern CONTAINS_SPACES_REGEX = Pattern.compile("[\\s]+");
  public static final Pattern FIRST_CAMEL_PART_REGEX = Pattern.compile("([a-z]+).*");
  public static final Pattern LAST_CAMEL_PART_REGEX = Pattern.compile("\\w*([A-Z][^A-Z]+)$");
  public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("[\\r\\n]+");
  public static final Pattern NAME_FALLBACK_REGEX = Pattern.compile("(.)(\\p{Lu})");
  public static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^A-Za-z0-9]");
  public static final Pattern NON_ALPHA_NUMERIC_ALL = Pattern.compile("^[^A-Za-z0-9]+$");
  public static final Pattern NON_ALPHA_ONLY = Pattern.compile("[^A-Za-z]");
  public static final Pattern NUMERIC_ONLY = Pattern.compile("[^0-9]");
  public static final Pattern NUMERIC_STRING_REGEX = Pattern.compile("[0-9\\. \\-e]+", Pattern.CASE_INSENSITIVE);
  public static final Pattern TAB_PATTERN = Pattern.compile("(\\t)+");
  public static final String TRUNCATE_STRING_ELLIPSIS = "...";
  public static final String VOWELS = "AEIOU";
  public static final String VOWELS_ALL = VOWELS + VOWELS.toLowerCase();
  private static final Logger LOG = Logger.getLogger(StringUtil.class);
  private static final String RANDOM_ALPHA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final Random RANDOM_GEN;
  private static final String RANDOM_ID_CHARS = RANDOM_ALPHA_CHARS + "0123456789";
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

  public static final String removeAllLineBreaks(String str) {
    return replaceAllLineBreaks(str, EMPTY);
  }

  public static final String replaceAllLineBreaks(String str, String replacement) {
    return LINE_BREAK_PATTERN.matcher(str).replaceAll(replacement);
  }

  public static final String replaceAllTabs(String str, String replacement) {
    return TAB_PATTERN.matcher(str).replaceAll(replacement);
  }

  public static final List<String> splitLines(String str) {
    return Lists.newArrayList(LINE_BREAK_PATTERN.split(str));
  }

  public static final String toCommaSeparatedString(int[] inputList) {
    return toCommaSeparatedString(inputList, "");
  }

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

  public static final String toCommaSeparatedString(List<String> inputList) {
    return toCommaSeparatedString(inputList, "");
  }

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

  public static final String toCommaSeparatedString(Set<String> inputList) {
    return toCommaSeparatedString(inputList, "");
  }

  public static final String toCommaSeparatedString(Set<String> inputList, String toRemove) {
    if (inputList != null) {
      return toCommaSeparatedString(new ArrayList<String>(inputList), toRemove);
    }
    return "";
  }

  public static final String toCommaSeparatedString(String[] inputList) {
    return toCommaSeparatedString(inputList, "");
  }

  public static final String toCommaSeparatedString(String[] inputList, String toRemove) {
    if (inputList != null) {
      return toCommaSeparatedString(asList(inputList), toRemove);
    }
    return "";
  }

  public static StringBuilder append(StringBuilder sb, String str) {
    return append(sb, str, true);
  }

  public static StringBuilder append(StringBuilder sb, String str, boolean withLineBreak) {
    sb.append(str);
    if (withLineBreak) {
      sb.append(System.lineSeparator());
    }
    return sb;
  }

  public static String base64Encode(String str) {
    return Base64.encodeBase64String(str.getBytes(StandardCharsets.UTF_8));
  }

  public static String camelCaseToSnakeCase(String str) {
    if (str == null) {
      return str;
    }
    return camelToLowerWithSep(str, "_").toLowerCase();
  }

  public static String camelToLowerWithSep(String str, String sep) {
    return isBlank(str) ? str : str.substring(0, 1).toLowerCase() +
      NAME_FALLBACK_REGEX.matcher(str).replaceAll("$1" + Matcher.quoteReplacement(sep) + "$2").substring(1).toLowerCase();
  }

  public static int countMatchesAtStart(String line, char matchChar) {
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != matchChar) {
        return i;
      }
    }
    return 0;
  }

  public static String createRandomString(int length) {
    return RandomStringUtils.randomAlphanumeric(length);
  }

  public static String fixCamelCase(String str) {
    if (NON_ALPHA_NUMERIC_ALL.matcher(str).matches()) {
      return str;
    }
    return toCamelCase(trim(CAMEL_CASE_CAP_CHARS.matcher(NON_ALPHA_NUMERIC.matcher(str).replaceAll(" ")).replaceAll(" $1$2")), " ");
  }

  public static String format(String str, Object... formatArgs) {
    return (formatArgs != null) && (formatArgs.length > 0) ? String.format(str, formatArgs) : str;
  }

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

  public static String generateRandomWord() {
    try {
      return trim(runAndGetOutput(asList(
        "perl",
        "-e",
        "$dict = \"/usr/share/dict/words\"; $bytes= -s $dict; open IN, $dict;seek(IN,rand($bytes-11),0);$_=<IN>;$_=<IN>;print"
      ), true));
    } catch (IOException e) {
      Logs.logError(LOG, e, "Issue generating random word!");
      return null;
    }
  }

  public static List<String> generateRandomWords(long size) {
    return Lambdas.iterate(size, StringUtil::generateRandomWord).collect(Collectors.toList());
  }

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

  public static String generateUniqueId(int length) {
    return RandomStringUtils.randomAlphanumeric(length);
  }

  public static String generateUniqueId(int length, Set<String> currentIds) {
    String id = null;
    while ((id == null) || currentIds.contains(id)) {
      id = generateUniqueId(length);
    }
    return id;
  }

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

  public static boolean isNumeric(String val) {
    return isNotBlank(val) && NUMERIC_STRING_REGEX.matcher(val).matches();
  }

  public static String lastChar(String str) {
    return str == null ? null : String.valueOf(str.charAt(str.length() - 1));
  }

  public static String lowerFirst(String str) {
    if (StringUtils.isNotBlank(str)) {
      str = str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    return str;
  }

  public static String objectToString(Object obj) {
    return ToStringBuilder.reflectionToString(obj, CommonConstants.DEFAULT_TO_STRING_STYLE);
  }

  public static String padLeft(String str, int padding) {
    return String.format("%1$" + padding + 's', str);
  }

  public static String padRight(String str, int padding) {
    return String.format("%1$-" + padding + 's', str);
  }

  public static String replaceNonAlpha(String str) {
    return NON_ALPHA_ONLY.matcher(str).replaceAll(EMPTY);
  }

  public static String replaceNonAlphaNumeric(String str) {
    return NON_ALPHA_NUMERIC.matcher(str).replaceAll(EMPTY);
  }

  public static String replaceNonNumeric(String str) {
    return NUMERIC_ONLY.matcher(str).replaceAll(EMPTY);
  }

  public static Pair<String, String> splitKeyValue(String line, String split) {
    line = line.trim();
    int index = line.indexOf(split);
    String key = line.substring(0, index);
    String value = line.substring(index + 1);
    return Pair.of(key, value);
  }

  public static boolean startsWith(String str, Collection<String> checks) {
    return (checks != null) && (str != null) && checks.stream().anyMatch(check -> str.startsWith(check));
  }

  public static String toCamelCase(String str) {
    return toCamelCase(str, "_");
  }

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

  public static String toNameFallback(String str) {
    return NAME_FALLBACK_REGEX.matcher(capitalize(str)).replaceAll("$1 $2");
  }

  public static String truncateStringIfNeeded(final String str, final int maxLength) {
    return truncateStringIfNeeded(str, maxLength, null);
  }

  public static String truncateStringIfNeeded(final String str, final int maxLength, @Nullable String appendSuffixIfTruncated) {
    return str == null ? null : str.length() > maxLength ? (str.substring(0, maxLength) + StringUtils.defaultString(appendSuffixIfTruncated)) : str;
  }

}
