// CHECKSTYLE.OFF: OpenCSV
package com.github.lancethomps.lava.common.file;

import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.ContextUtil.getFile;
import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static com.github.lancethomps.lava.common.logging.Logs.logTrace;
import static com.github.lancethomps.lava.common.ser.Serializer.fromCsv;
import static java.io.File.separator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.splitPreserveAllTokens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.Collect;
import com.github.lancethomps.lava.common.ContextUtil;
import com.github.lancethomps.lava.common.Patterns;
import com.github.lancethomps.lava.common.date.Dates;
import com.github.lancethomps.lava.common.lambda.ThrowingBiConsumer;
import com.github.lancethomps.lava.common.logging.LogLine;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.github.lancethomps.lava.common.time.Stopwatch;
import com.github.lancethomps.lava.common.time.Timing;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opencsv.CSVReader;

/**
 * The Class FileUtil.
 */
public class FileUtil {

	/** The Constant DATE_SUFFIX_REGEX. */
	public static final Pattern DATE_SUFFIX_REGEX = Pattern.compile(".*_(\\d+)(\\.[a-zA-Z]+){0,1}");

	/** The Constant DEFAULT_REMOVE_LINES_PREFIXES. */
	public static final Set<String> DEFAULT_REMOVE_LINES_PREFIXES = Sets.newHashSet("#", "//");

	/** The Constant INVALID_FILENAME. */
	public static final Pattern INVALID_FILENAME = compile("[^a-zA-Z0-9\\.\\-_@]");

	/** The Constant LATEST_FILE_DATE_SUFFIX_COMP. */
	public static final Comparator<File> LATEST_FILE_DATE_SUFFIX_COMP = (f1, f2) -> ofNullable(Dates.parseDate(Patterns.getGroup(DATE_SUFFIX_REGEX, f2.getName(), 1), false))
		.orElseGet(() -> LocalDate.of(1970, 1, 1))
		.compareTo(ofNullable(Dates.parseDate(Patterns.getGroup(DATE_SUFFIX_REGEX, f1.getName(), 1), false)).orElseGet(() -> LocalDate.of(1970, 1, 1)));

	/** The Constant POSSIBLE_SEP_CHARS. */
	public static final List<Character> POSSIBLE_SEP_CHARS = Collections.unmodifiableList(Lists.newArrayList('\t', '|', ','));

	/** The Constant IMPORTS_REGEX. */
	private static final Pattern IMPORTS_REGEX = Pattern.compile("^\\/\\*\\s*import:(.*?)\\s*\\*\\/$", Pattern.MULTILINE);

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(FileUtil.class);

	/** The Constant LOG4J_LOG. */
	private static final Pattern LOG4J_LOG = compile("^(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s+\\((.*?)\\)\\s+([^ ]*)\\s+\\[(.*?)\\]\\s+(.*)$");

	/** The Constant LOG4J_LOG_MULTILINE. */
	private static final Pattern LOG4J_LOG_MULTILINE = compile(
		"(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s+\\((.*?)\\)\\s+([^ ]*)\\s+\\[(.*?)\\]\\s+(.*?)(?=(\\n\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2},\\d{3})|$)",
		Pattern.DOTALL
	);

	/**
	 * Combine files based on first key.
	 *
	 * @param fileOne the file one
	 * @param fileTwo the file two
	 * @return the string builder
	 */
	public static StringBuilder combineFilesBasedOnFirstKey(File fileOne, File fileTwo) {
		try {
			Map<Integer, String> headers = getHeadersByPosition(fileOne);
			Map<Integer, String> headersTwo = getHeadersByPosition(fileTwo);
			String key = headers.get(0);
			List<Map<String, Object>> one = convertCsvToMap(fileOne);
			List<Map<String, Object>> two = convertCsvToMap(fileTwo);
			TreeMap<String, Map<String, Object>> combined = new TreeMap<>();
			for (Map<String, Object> data : one) {
				String dataKey = (String) data.get(key);
				if (StringUtils.isNotBlank(dataKey)) {
					combined.put(dataKey, data);
				}
			}
			for (Map<String, Object> data : two) {
				String dataKey = (String) data.get(key);
				if (StringUtils.isNotBlank(dataKey)) {
					combined.put(dataKey, data);
				}
			}
			Set<String> allHeaders = new HashSet<>(headers.values());
			allHeaders.addAll(headersTwo.values());
			allHeaders.remove(key);
			List<String> combinedHeaders = new ArrayList<>(allHeaders);
			Collections.sort(combinedHeaders);
			combinedHeaders.add(0, key);
			StringBuilder sb = convertToCsv(new ArrayList<>(combined.values()), combinedHeaders);
			return sb;
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue combining files at paths [%s] and [%s]!", fileOne.getPath(), fileTwo.getPath());
		}
		return null;
	}

	/**
	 * Convert csv to java.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param headerConverter the header converter
	 * @param fieldConverter the field converter
	 * @return the list
	 */
	public static <T> List<T> convertCsvToJava(File file, Class<T> type, CsvFieldConverter headerConverter, CsvFieldConverter fieldConverter) {
		return convertCsvToJava(file, type, headerConverter, fieldConverter, null);
	}

	/**
	 * Convert csv to java.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param headerConverter the header converter
	 * @param fieldConverter the field converter
	 * @param removeLinesWithPrefix the remove lines with prefix
	 * @return the list
	 */
	public static <T> List<T> convertCsvToJava(File file, Class<T> type, CsvFieldConverter headerConverter, CsvFieldConverter fieldConverter, String removeLinesWithPrefix) {
		return convertCsvToJava(
			file,
			type,
			new FileParserOptions().setHeaderConverter(headerConverter).setFieldConverter(fieldConverter).setRemoveLinesWithPrefixes(
				removeLinesWithPrefix == null ? null : Sets.newHashSet(removeLinesWithPrefix)
			)
		);
	}

	/**
	 * Convert csv to java.
	 *
	 * @param <T> the generic type
	 * @param file the file
	 * @param type the type
	 * @param options the options
	 * @return the list
	 */
	public static <T> List<T> convertCsvToJava(File file, Class<T> type, FileParserOptions options) {
		FileParser<T> parser = new FileParser<>(file, type, options);
		try {
			return parser.parseFile().getResultList();
		} catch (FileParsingException e) {
			Logs.logError(LOG, e, "Error converting CSV file to Java bean list: file=%s type=%s options=%s", file, type, options);
			return null;
		}
	}

	/**
	 * Convert csv to map.
	 *
	 * @param file the file
	 * @return the list
	 */
	public static List<Map<String, Object>> convertCsvToMap(File file) {
		return fromCsv(file, new FileParserOptions());
	}

	/**
	 * Convert csv to map.
	 *
	 * @param file the file
	 * @param headerConverter the header converter
	 * @param fieldConverter the field converter
	 * @return the list
	 */
	public static List<Map<String, Object>> convertCsvToMap(File file, CsvFieldConverter headerConverter, CsvFieldConverter fieldConverter) {
		return convertCsvToMap(file, headerConverter, fieldConverter, null);
	}

	/**
	 * Convert csv to map.
	 *
	 * @param file the file
	 * @param headerConverter the header converter
	 * @param fieldConverter the field converter
	 * @param removeLinesWithPrefix the remove lines with prefix
	 * @return the list
	 */
	public static List<Map<String, Object>> convertCsvToMap(File file, CsvFieldConverter headerConverter, CsvFieldConverter fieldConverter, String removeLinesWithPrefix) {
		return Serializer.fromCsv(
			file,
			new FileParserOptions().setHeaderConverter(headerConverter).setFieldConverter(fieldConverter).setRemoveLinesWithPrefixes(
				removeLinesWithPrefix == null ? null : Sets.newHashSet(removeLinesWithPrefix)
			)
		);
	}

	/**
	 * Convert csv to mapped first key.
	 *
	 * @param file the file
	 * @param headerConverter the header converter
	 * @param fieldConverter the field converter
	 * @return the map
	 */
	public static Map<String, Map<String, Object>> convertCsvToMappedFirstKey(File file, CsvFieldConverter headerConverter, CsvFieldConverter fieldConverter) {
		Map<String, Map<String, Object>> mappedData = new HashMap<>();
		BufferedReader br = null;
		CSVReader csv = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String headerLine = br.readLine();
			char sepChar = FileUtil.getSeparationChar(headerLine);
			Map<Integer, String> headersByPos = getHeadersByPosition(headerLine, sepChar, true);
			String mapKey = headersByPos.get(0);
			Map<String, Integer> headers = FileUtil.getHeaderPositionsPreserveCase(headerLine, sepChar);
			if (headerConverter != null) {
				Map<String, Integer> convertedHeaders = new HashMap<>();
				for (Entry<String, Integer> entry : headers.entrySet()) {
					String converted = (String) headerConverter.convertObject(entry.getKey(), null);
					convertedHeaders.put(converted, entry.getValue());
				}
				headers = convertedHeaders;
			}
			csv = new CSVReader(br, sepChar);
			String[] data = null;
			while ((data = csv.readNext()) != null) {
				Map<String, Object> dataMap = new TreeMap<>();
				try {
					int dataLength = data.length;
					for (Entry<String, Integer> entry : headers.entrySet()) {
						int pos = entry.getValue();
						if (pos < dataLength) {
							String header = entry.getKey();
							Object value = data[entry.getValue()];
							if (fieldConverter != null) {
								value = fieldConverter.convertObject(value, header);
							}
							dataMap.put(header, value);
						}
					}
					if (dataMap.get(mapKey) != null) {
						mappedData.put((String) dataMap.get(mapKey), dataMap);
					}
				} catch (Throwable e) {
					Logs.logError(LOG, e, "Error creating object from map [%s].", dataMap);
				}
			}
		} catch (Throwable e) {
			logError(LOG, e, "Issue getting data from file [%s].", file.getPath());
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(csv);
		}
		return mappedData;
	}

	/**
	 * Convert lines to log lines.
	 *
	 * @param lines the lines
	 * @return the list
	 */
	public static List<LogLine> convertLinesToLogLines(List<String> lines) {
		List<LogLine> logs = Lists.newArrayList();
		if (isNotEmpty(lines)) {
			Collections.reverse(lines);
			String allLines = StringUtils.join(lines, System.lineSeparator());
			Matcher matcher = LOG4J_LOG_MULTILINE.matcher(allLines);
			while (matcher.find()) {
				LogLine line = new LogLine();
				line.setRaw(matcher.group());
				try {
					line.setDate(LocalDateTime.from(Dates.LOG4J_FORMAT.parse(matcher.group(1) + ' ' + matcher.group(2))));
				} catch (DateTimeParseException e) {
					;
				} catch (Exception e) {
					Logs.logError(LOG, e, "Issue parsing log line!");
				}
				line.setThread(matcher.group(3));
				line.setPriority(matcher.group(4));
				line.setCategory(matcher.group(5));
				line.setMessage(matcher.group(6));
				logs.add(0, line);
			}
		}
		return logs;
	}

	/**
	 * Convert lines to log lines one by one.
	 *
	 * @param lines the lines
	 * @return the list
	 */
	public static List<LogLine> convertLinesToLogLinesOneByOne(List<String> lines) {
		List<LogLine> logs = Lists.newArrayList();
		for (String rawLine : lines) {
			rawLine = StringUtils.trim(rawLine);
			Matcher matcher = LOG4J_LOG.matcher(rawLine);
			if (!matcher.matches()) {
				continue;
			}
			LogLine line = new LogLine();
			line.setRaw(rawLine);
			try {
				line.setDate(LocalDateTime.from(Dates.LOG4J_FORMAT.parse(matcher.group(1) + ' ' + matcher.group(2))));
			} catch (DateTimeParseException e) {
				;
			} catch (Exception e) {
				Logs.logError(LOG, e, "Issue parsing log line!");
			}
			line.setThread(matcher.group(3));
			line.setPriority(matcher.group(4));
			line.setCategory(matcher.group(5));
			line.setMessage(matcher.group(6));
			logs.add(0, line);
		}
		return logs;
	}

	/**
	 * Convert to csv.
	 *
	 * @param raw the raw
	 * @param headerKeys the header keys
	 * @return the string builder
	 * @throws Exception the exception
	 */
	public static StringBuilder convertToCsv(List<Map<String, Object>> raw, List<String> headerKeys) throws Exception {
		if (headerKeys == null) {
			headerKeys = getHeaderKeys(raw);
		}
		StringBuilder csv = new StringBuilder();
		StringBuilder headers = new StringBuilder();
		for (String header : headerKeys) {
			if (headers.length() > 0) {
				headers.append(',');
			}
			headers.append(header);
		}
		for (Map<String, Object> map : raw) {
			boolean first = true;
			for (String header : headerKeys) {
				Object obj = map.get(header);
				if (!first) {
					csv.append(',');
				}
				csv.append(obj == null ? "" : escapeCsv(obj.toString()));
				first = false;
			}
			csv.append("\n");
		}
		headers.append("\n").append(csv);
		return headers;
	}

	/**
	 * Copy file.
	 *
	 * @param srcFile the src file
	 * @param destFile the dest file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void copyFile(File srcFile, File destFile) throws IOException {
		Logs.logInfo(LOG, "Copy [%s] ==> [%s]", srcFile, destFile);
		FileUtils.copyFile(srcFile, destFile);
	}

	/**
	 * Count lines.
	 *
	 * @param file the file
	 * @return the int
	 */
	public static int countLines(File file) {
		int count = 0;
		boolean empty = true;
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(openInputStream(file)))) {
				while (reader.readLine() != null) {
					empty = false;
					count++;
				}
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Error counting lines: file=%s", fullPath(file));
		}
		return ((count == 0) && !empty) ? 1 : count;
	}

	/**
	 * Count lines.
	 *
	 * @param path the path
	 * @return the int
	 */
	public static int countLines(String path) {
		return countLines(new File(path));
	}

	/**
	 * Creates the directory.
	 *
	 * @param dir the dir
	 * @param force the force
	 * @return the file
	 */
	public static File createDirectory(File dir, boolean force) {
		return createDirectory(dir, force, 1);
	}

	/**
	 * Creates the directory.
	 *
	 * @param dir the dir
	 * @param force the force
	 * @param tryCount the try count
	 * @return the file
	 */
	public static File createDirectory(File dir, boolean force, int tryCount) {
		if (dir == null) {
			return dir;
		} else if (dir.exists()) {
			return dir;
		}
		Throwable error = null;
		boolean success = false;
		try {
			success = dir.mkdirs();
		} catch (Throwable e) {
			success = false;
			error = e;
		}
		if (!success) {
			if (!force || (tryCount > 5)) {
				logError(LOG, new Exception("Could not create dir!", error), "Stopped trying to create dir at path [%s].", dir);
			} else {
				tryCount++;
				return createDirectory(new File(dir.getPath() + '-' + tryCount), force, tryCount);
			}
		}
		return dir;
	}

	/**
	 * Creates the file writer.
	 *
	 * @param file the file
	 * @return the prints the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static PrintWriter createFileWriter(@Nonnull File file) throws IOException {
		if (!file.isFile()) {
			FileUtils.touch(file);
		}
		return new PrintWriter(new FileWriter(file, true));
	}

	/**
	 * Delete dir.
	 *
	 * @param dir the dir
	 * @return true, if successful
	 */
	public static boolean deleteDir(File dir) {
		try {
			FileUtils.deleteDirectory(dir);
		} catch (Throwable e) {
			Logs.logWarn(LOG, e, "Error deleting directory [%s].", dir);
			return false;
		}
		return true;
	}

	/**
	 * Delete empty sub dirs.
	 *
	 * @param baseDir the base dir
	 * @param recursive the recursive
	 * @return true, if successful
	 */
	public static boolean deleteEmptySubDirs(File baseDir, boolean recursive) {
		try {
			boolean success = false;
			if (baseDir.isDirectory()) {
				success = true;
				FileFilter filter = (file) -> {
					if (file.isDirectory()) {
						if (Checks.isEmpty(file.listFiles())) {
							return true;
						} else if (recursive) {
							deleteEmptySubDirs(file, recursive);
							if (Checks.isEmpty(file.listFiles())) {
								return true;
							}
						}
					}
					return false;
				};
				for (File subDir : baseDir.listFiles(filter)) {
					Logs.logDebug(LOG, "Deleting empty sub-directory: dir=%s", fullPath(subDir));
					if (!deleteDir(subDir)) {
						success = false;
					}
				}
			}
			return success;
		} catch (Throwable e) {
			Logs.logWarn(LOG, e, "Error deleting empty sub-directories: dir=%s", fullPath(baseDir));
			return false;
		}
	}

	/**
	 * Delete file.
	 *
	 * @param file the file
	 * @return true, if successful
	 */
	public static boolean deleteFile(File file) {
		try {
			return file != null ? file.delete() : false;
		} catch (Throwable e) {
			logError(LOG, e, "Issue deleting file [%s].", fullPath(file));
		}
		return false;
	}

	/**
	 * Delete file.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return deleteFile(file);
		}
		return false;
	}

	/**
	 * Delete old files.
	 *
	 * @param dir the dir
	 * @param lengthOfTime the length of time
	 * @throws Exception the exception
	 */
	public static void deleteOldFiles(File dir, long lengthOfTime) throws Exception {
		deleteOldFiles(dir, lengthOfTime, false);
	}

	/**
	 * Delete old files.
	 *
	 * @param dir the dir
	 * @param lengthOfTime the length of time
	 * @param fileOnly the file only
	 * @throws Exception the exception
	 */
	public static void deleteOldFiles(File dir, long lengthOfTime, boolean fileOnly) throws Exception {
		long currentTime = System.currentTimeMillis();
		Collection<File> files = (dir == null) || !dir.isDirectory() ? null : FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
		if (files == null) {
			return;
		}
		for (File file : files) {
			long diff = currentTime - file.lastModified();
			if (diff > lengthOfTime) {
				if (file.isDirectory()) {
					if (!fileOnly) {
						FileUtils.deleteDirectory(file);
					}
				} else {
					FileUtils.deleteQuietly(file);
				}
			}
		}
	}

	/**
	 * Full path.
	 *
	 * @param file the file
	 * @return the string
	 */
	public static String fullPath(File file) {
		if (file != null) {
			try {
				return file.getCanonicalPath();
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue getting canonical path for file [%s] - returning absolute path.", file);
				return file.getAbsolutePath();
			}
		}
		return null;
	}

	/**
	 * Full path.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String fullPath(String path) {
		return fullPath(new File(path));
	}

	/**
	 * Gets the converted headers.
	 *
	 * @param headers the headers
	 * @param headerConverter the header converter
	 * @return the converted headers
	 * @throws Exception the exception
	 */
	public static Map<String, Integer> getConvertedHeaders(Map<String, Integer> headers, CsvFieldConverter headerConverter) throws Exception {
		if (headerConverter != null) {
			Map<String, Integer> convertedHeaders = new HashMap<>();
			for (Entry<String, Integer> entry : headers.entrySet()) {
				String converted = (String) headerConverter.convertObject(entry.getKey(), null);
				if (StringUtils.isNotBlank(converted)) {
					convertedHeaders.put(converted, entry.getValue());
				}
			}
			headers = convertedHeaders;
		}
		return headers;
	}

	/**
	 * Gets the distinct relative paths.
	 *
	 * @param recursive the recursive
	 * @param baseDirectories the base directories
	 * @return the distinct relative paths
	 */
	public static Set<String> getDistinctRelativePaths(boolean recursive, File... baseDirectories) {
		Set<String> paths = new HashSet<>();
		for (File dir : baseDirectories) {
			if ((dir != null) && dir.isDirectory()) {
				for (File file : FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(), recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter())) {
					if (file.isFile()) {
						String path = StringUtils.removeStart(fullPath(file), fullPath(dir));
						paths.add(path);
					}
				}
			}
		}
		return paths;
	}

	/**
	 * Gets the file contents with imports.
	 *
	 * @param path the path
	 * @return the file contents with imports
	 */
	public static String getFileContentsWithImports(String path) {
		String data = null;
		try {
			File file = ContextUtil.getFile(path);
			if ((file != null) && file.isFile()) {
				String basePath = fullPath(file.getParentFile()) + File.separatorChar;
				Matcher imports = IMPORTS_REGEX.matcher(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
				StringBuffer sb = new StringBuffer();
				while (imports.find()) {
					String importData = Collect.splitCsvAsList(imports.group(1)).stream().map(basePath::concat).map(FileUtil::getFileContentsWithImports).reduce("", String::concat);
					imports.appendReplacement(sb, Matcher.quoteReplacement(importData));
				}
				data = imports.appendTail(sb).toString();
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue reading file at path [%s]", path);
		}
		return data != null ? data + System.lineSeparator() : data;
	}

	/**
	 * Gets the file lines.
	 *
	 * @param file the file
	 * @return the file lines
	 */
	public static List<String> getFileLines(File file) {
		return getFileLines(file, null);
	}

	/**
	 * Gets the file lines.
	 *
	 * @param file the file
	 * @param options the options
	 * @return the file lines
	 */
	public static List<String> getFileLines(File file, @Nullable FileParserOptions options) {
		if (file.exists()) {
			FileParser<String> parser = new FileParser<>(file, String.class, Checks.defaultIfNull(options, FileParserOptions::new));
			return parser.parseFileToList().getResultList();
		}
		return null;
	}

	/**
	 * Gets the file list checksum.
	 *
	 * @param files the files
	 * @return the file list checksum
	 */
	public static String getFileListChecksum(Collection<File> files) {
		String filesInfo = files.stream().map(file -> FileUtil.fullPath(file) + file.lastModified()).reduce("", String::concat);
		Logs.logTrace(LOG, "Info for checksum using files %s is [%s]", files, filesInfo);
		return StringUtil.generateMd5(filesInfo);
	}

	/**
	 * Gets the file list checksum.
	 *
	 * @param rootDir the root dir
	 * @return the file list checksum
	 */
	public static String getFileListChecksum(File rootDir) {
		return getFileListChecksum(rootDir, trueFileFilter());
	}

	/**
	 * Gets the file list checksum.
	 *
	 * @param rootDir the root dir
	 * @param fileFilter the file filter
	 * @return the file list checksum
	 */
	public static String getFileListChecksum(File rootDir, IOFileFilter fileFilter) {
		return getFileListChecksum(rootDir, fileFilter, trueFileFilter());
	}

	/**
	 * Gets the file list checksum.
	 *
	 * @param rootDir the root dir
	 * @param fileFilter the file filter
	 * @param dirFilter the dir filter
	 * @return the file list checksum
	 */
	public static String getFileListChecksum(File rootDir, IOFileFilter fileFilter, IOFileFilter dirFilter) {
		return getFileListChecksum(FileUtils.listFiles(rootDir, fileFilter, dirFilter));
	}

	/**
	 * Gets the file list checksum.
	 *
	 * @param rootDir the root dir
	 * @param wildcards the wildcards
	 * @return the file list checksum
	 */
	public static String getFileListChecksum(File rootDir, String... wildcards) {
		return getFileListChecksum(FileUtils.listFiles(rootDir, new WildcardFileFilter(wildcards), trueFileFilter()));
	}

	/**
	 * Gets the file list checksum by regex.
	 *
	 * @param rootDir the root dir
	 * @param filePattern the file pattern
	 * @return the file list checksum by regex
	 */
	public static String getFileListChecksumByRegex(File rootDir, String filePattern) {
		return getFileListChecksum(FileUtils.listFiles(rootDir, new RegexFileFilter(filePattern), trueFileFilter()));
	}

	/**
	 * Gets the file reader.
	 *
	 * @param file the file
	 * @return the file reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static LineNumberReader getFileReader(File file) throws IOException {
		LineNumberReader br = null;
		if (file.getPath().endsWith(".gz")) {
			br = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
		} else {
			br = new LineNumberReader(new FileReader(file));
		}
		return br;
	}

	/**
	 * Gets the files older than.
	 *
	 * @param dir the dir
	 * @param cutoff the cutoff
	 * @return the files older than
	 */
	public static Collection<File> getFilesOlderThan(@Nonnull File dir, @Nonnull LocalDateTime cutoff) {
		AgeFileFilter filter = new AgeFileFilter(Dates.toOldDate(cutoff));
		return FileUtils.listFiles(dir, filter, FileFilterUtils.trueFileFilter());
	}

	/**
	 * Gets the path with default.
	 *
	 * @param paths the paths
	 * @return the path with default
	 */
	public static File getFirstAvailableDir(List<String> paths) {
		return paths.stream().filter(Checks::isNotBlank).map(File::new).filter(File::isDirectory).findFirst().orElse(null);
	}

	/**
	 * Gets the first available dir.
	 *
	 * @param paths the paths
	 * @return the first available dir
	 */
	public static File getFirstAvailableDir(String... paths) {
		return Stream.of(paths).filter(Checks::isNotBlank).map(File::new).filter(File::isDirectory).findFirst().orElse(null);
	}

	/**
	 * Gets the first available dir path.
	 *
	 * @param paths the paths
	 * @return the first available dir path
	 */
	public static String getFirstAvailableDirPath(List<String> paths) {
		return StringUtils.removeEnd(Optional.ofNullable(getFirstAvailableDir(paths)).map(File::getPath).orElse(null), "/");
	}

	/**
	 * Gets the first available file.
	 *
	 * @param files the files
	 * @return the first available file
	 */
	public static File getFirstAvailableFile(File... files) {
		return Stream.of(files).filter(File::isFile).findFirst().orElse(null);
	}

	/**
	 * Gets the first available file.
	 *
	 * @param paths the paths
	 * @return the first available file
	 */
	public static File getFirstAvailableFile(String... paths) {
		return Stream.of(paths).filter(Checks::isNotBlank).map(File::new).filter(File::isFile).findFirst().orElse(null);
	}

	/**
	 * Gets the first lines of file.
	 *
	 * @param file the file
	 * @param totalLines the total lines
	 * @return the first lines of file
	 */
	public static List<String> getFirstLinesOfFile(File file, int totalLines) {
		if (file.exists() && (totalLines > 0)) {
			FileParser<String> parser = new FileParser<>(file, String.class, new FileParserOptions().setMaxLines(totalLines));
			return parser.parseFileToList().getResultList();
		}
		return null;
	}

	/**
	 * Gets the header keys.
	 *
	 * @param raw the raw
	 * @return the header keys
	 */
	public static List<String> getHeaderKeys(List<Map<String, Object>> raw) {
		List<String> headerKeys = new ArrayList<>();
		for (Map<String, Object> map : raw) {
			for (String key : map.keySet()) {
				if (!headerKeys.contains(key)) {
					headerKeys.add(key);
				}
			}
		}
		return headerKeys;
	}

	/**
	 * Gets the header positions.
	 *
	 * @param line the line
	 * @return the header positions
	 */
	public static Map<String, Integer> getHeaderPositions(String line) {
		char headerSepChar = getSeparationChar(line);
		return getHeaderPositions(line, headerSepChar);
	}

	/**
	 * Gets the header positions.
	 *
	 * @param line the line
	 * @param headerSepChar the header sep char
	 * @return the header positions
	 */
	public static Map<String, Integer> getHeaderPositions(String line, char headerSepChar) {
		Map<String, Integer> headers = new HashMap<>();
		try {
			String[] firstLine = splitPreserveAllTokens(line, headerSepChar);
			int i = 0;
			for (String header : firstLine) {
				headers.put(header.toLowerCase(), i);
				i++;
			}
		} catch (Throwable e) {
			logError(LOG, e, "Error creating header positions.");
		}
		return headers;
	}

	/**
	 * Gets the header positions preserve case.
	 *
	 * @param line the line
	 * @return the header positions preserve case
	 */
	public static Map<String, Integer> getHeaderPositionsPreserveCase(String line) {
		char headerSepChar = getSeparationChar(line);
		return getHeaderPositionsPreserveCase(line, headerSepChar);
	}

	/**
	 * Gets the header positions preserve case.
	 *
	 * @param line the line
	 * @param headerSepChar the header sep char
	 * @return the header positions preserve case
	 */
	public static Map<String, Integer> getHeaderPositionsPreserveCase(String line, char headerSepChar) {
		Map<String, Integer> headers = new HashMap<>();
		try {
			String[] firstLine = splitPreserveAllTokens(line, headerSepChar);
			int i = 0;
			for (String header : firstLine) {
				headers.put(header, i);
				i++;
			}
		} catch (Throwable e) {
			logError(LOG, e, "Error creating header positions.");
		}
		return headers;
	}

	/**
	 * Gets the header positions with original values.
	 *
	 * @param line the line
	 * @param headerSepChar the header sep char
	 * @return the header positions with original values
	 */
	public static Pair<Map<String, Integer>, Map<String, String>> getHeaderPositionsWithOriginalValues(String line, char headerSepChar) {
		Map<String, Integer> headers = new HashMap<>();
		Map<String, String> originalValues = new HashMap<>();
		try {
			String[] firstLine = splitPreserveAllTokens(line, headerSepChar);
			int i = 0;
			for (String header : firstLine) {
				headers.put(header.toLowerCase(), i);
				originalValues.put(header.toLowerCase(), header);
				i++;
			}
		} catch (Throwable e) {
			logError(LOG, e, "Error creating header positions.");
		}
		return Pair.of(headers, originalValues);
	}

	/**
	 * Gets the headers by position.
	 *
	 * @param file the file
	 * @return the headers by position
	 */
	public static Map<Integer, String> getHeadersByPosition(File file) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			return getHeadersByPosition(line, ',', true);
		} catch (Throwable e) {
			logError(LOG, e, "Issue getting data from file [%s].", file.getPath());
		} finally {
			try {
				br.close();
			} catch (Throwable e) {
				logError(LOG, e, "Reader is already closed for file [%s].", file.getPath());
			}
		}
		return null;
	}

	/**
	 * Gets the headers by position.
	 *
	 * @param line the line
	 * @return the headers by position
	 */
	public static Map<Integer, String> getHeadersByPosition(String line) {
		char headerSepChar = getSeparationChar(line);
		return getHeadersByPosition(line, headerSepChar, false);
	}

	/**
	 * Gets the headers by position.
	 *
	 * @param line the line
	 * @param headerSepChar the header sep char
	 * @param preserveCase the preserve case
	 * @return the headers by position
	 */
	public static Map<Integer, String> getHeadersByPosition(String line, char headerSepChar, boolean preserveCase) {
		Map<Integer, String> headers = new HashMap<>();
		try {
			String[] firstLine = splitPreserveAllTokens(line, headerSepChar);
			int i = 0;
			for (String header : firstLine) {
				String converted = header;
				if (!preserveCase) {
					converted = WordUtils.capitalizeFully(header);
					converted = header.substring(0, 1).toLowerCase();
					if (header.length() > 1) {
						converted = converted + header.substring(1).replace(" ", "");
					}
				}
				headers.put(i, converted);
				i++;
			}
		} catch (Throwable e) {
			logError(LOG, e, "Error creating header positions.");
		}
		return headers;
	}

	/**
	 * Gets the last lines of file.
	 *
	 * @param file the file
	 * @param totalLines the total lines
	 * @return the last lines of file
	 */
	public static List<String> getLastLinesOfFile(File file, int totalLines) {
		return getLastLinesOfFile(file, totalLines, null);
	}

	/**
	 * Gets the last lines of file.
	 *
	 * @param file the file
	 * @param totalLines the total lines
	 * @param matchRegex the match regex
	 * @return the last lines of file
	 */
	public static List<String> getLastLinesOfFile(File file, int totalLines, Pattern matchRegex) {
		if ((file != null) && file.isFile() && (totalLines > 0)) {
			List<String> lines = new ArrayList<>();
			int counter = 0;
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, UTF_8)) {
				String line = reader.readLine();
				while ((line != null) && (counter < totalLines)) {
					if (isNotBlank(line) && ((matchRegex == null) || matchRegex.matcher(line).find())) {
						lines.add(line);
						counter++;
					}
					line = reader.readLine();
				}
			} catch (Throwable e) {
				logError(LOG, e, "Error reading file [%s]!", fullPath(file));
			}
			return lines;
		}
		return null;
	}

	/**
	 * Gets the latest file.
	 *
	 * @param files the files
	 * @return the latest file
	 */
	public static File getLatestFile(File... files) {
		return getLatestFile((files == null) || (files.length == 0) ? Collections.emptyList() : Arrays.asList(files));
	}

	/**
	 * Gets the latest file.
	 *
	 * @param directory the directory
	 * @param regex the regex
	 * @return the latest file
	 */
	public static File getLatestFile(File directory, Pattern regex) {
		return getLatestFile(directory, regex, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
	}

	/**
	 * Gets the latest file.
	 *
	 * @param directory the directory
	 * @param regex the regex
	 * @param sort the sort
	 * @return the latest file
	 */
	public static File getLatestFile(File directory, Pattern regex, Comparator<File> sort) {
		File latestFile = null;
		if ((directory == null) || !directory.isDirectory()) {
			return null;
		}
		FileFilter fileFilter = new RegexFileFilter(regex);
		File[] files = directory.listFiles(fileFilter);
		if ((files != null) && (files.length > 0)) {
			Arrays.sort(files, sort);
			latestFile = files[0];
		}
		return latestFile;
	}

	/**
	 * Gets the latest file in a directory for a specific fileNamePattern.
	 *
	 * @param directory the directory
	 * @param pattern the file name pattern e.g. rCalcJsonGrid_*
	 * @return the latest file
	 */
	public static File getLatestFile(File directory, String pattern) {
		return getLatestFile(directory, pattern, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
	}

	/**
	 * Gets the latest file.
	 *
	 * @param directory the directory
	 * @param pattern the pattern
	 * @param sort the sort
	 * @return the latest file
	 */
	public static File getLatestFile(File directory, String pattern, Comparator<File> sort) {
		File latestFile = null;
		if ((directory == null) || !directory.isDirectory()) {
			return null;
		}
		FileFilter fileFilter = new WildcardFileFilter(pattern);
		File[] files = directory.listFiles(fileFilter);
		if ((files != null) && (files.length > 0)) {
			Arrays.sort(files, sort);
			latestFile = files[0];
		}
		return latestFile;
	}

	/**
	 * Gets the latest file.
	 *
	 * @param files the files
	 * @return the latest file
	 */
	public static File getLatestFile(List<File> files) {
		if (isNotEmpty(files)) {
			return files.stream().filter(Objects::nonNull).sorted((f1, f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified())).findFirst().orElse(null);
		}
		return null;
	}

	/**
	 * Gets the null safe file path.
	 *
	 * @param file the file
	 * @return the null safe file path
	 */
	public static String getNullSafeFilePath(File file) {
		return file == null ? null : file.getPath();
	}

	/**
	 * Gets the relative path.
	 *
	 * @param parent the parent
	 * @param child the child
	 * @return the relative path
	 */
	public static String getRelativePath(File parent, File child) {
		return StringUtils.substringAfter(fullPath(child), fullPath(parent) + File.separatorChar);
	}

	/**
	 * Gets the root path.
	 *
	 * @param context the context
	 * @return the root path
	 */
	public static String getRootPath(ServletContext context) {
		String root = context.getRealPath("/");
		if (!endsWith(root, "/") && !endsWith(root, "\\")) {
			root = root + '/';
		}
		return root;
	}

	/**
	 * Gets the root path.
	 *
	 * @param context the context
	 * @return the root path
	 */
	public static String getRootPath(WebApplicationContext context) {
		return getRootPath(context.getServletContext());
	}

	/**
	 * Gets the separation char.
	 *
	 * @param line the line
	 * @return the separation char
	 */
	public static char getSeparationChar(String line) {
		char sepChar = POSSIBLE_SEP_CHARS.get(0);
		int highestCount = 0;
		List<Integer> counts = new ArrayList<>();
		for (char poss : POSSIBLE_SEP_CHARS) {
			int count = countMatches(line, String.valueOf(poss));
			counts.add(count);
			if (count > highestCount) {
				highestCount = count;
				sepChar = poss;
			}
		}
		if (LOG.isTraceEnabled()) {
			logTrace(LOG, "Separation char counts for chars ['%s'] are %s. Seperation char is [%s].", StringUtils.join(POSSIBLE_SEP_CHARS, "', '"), counts, sepChar);
		}
		return sepChar;
	}

	/**
	 * Gets the unique file.
	 *
	 * @param absolutePath the absolute path
	 * @return the unique file
	 */
	public static synchronized File getUniqueFile(String absolutePath) {
		File file = new File(absolutePath);
		int count = 1;
		while (file.exists()) {
			file = new File(file.getParent() + '/' + getBaseName(file.getPath()) + '-' + count + defaultIfBlank(getExtension(file.getPath()), ""));
			count++;
		}
		return file;
	}

	/**
	 * Go to line.
	 *
	 * @param br the br
	 * @param lineNum the line num
	 * @return the line number reader
	 * @throws Exception the exception
	 */
	public static LineNumberReader goToLine(LineNumberReader br, int lineNum) throws Exception {
		int currentLine = br.getLineNumber();
		String line = currentLine < lineNum ? br.readLine() : null;
		while (StringUtils.isNotBlank(line) && (br.getLineNumber() < lineNum)) {
			br.readLine();
		}
		return br;
	}

	/**
	 * Checks for extension.
	 *
	 * @param fileName the file name
	 * @return true, if successful
	 */
	public static boolean hasExtension(String fileName) {
		return FilenameUtils.indexOfExtension(fileName) > -1;
	}

	/**
	 * Checks if is absolute path.
	 *
	 * @param path the path
	 * @return true, if is absolute path
	 */
	public static boolean isAbsolutePath(String path) {
		return path.startsWith("/") || path.startsWith("\\") || (path.charAt(1) == ':');
	}

	/**
	 * Checks if is completely written.
	 *
	 * @param file the file
	 * @return true, if is completely written
	 */
	public static boolean isCompletelyWritten(File file) {
		RandomAccessFile stream = null;
		try {
			stream = new RandomAccessFile(file, "rw");
			return true;
		} catch (Throwable e) {
			Logs.logWarn(LOG, "File [%s] is not completely written!", file.getPath());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					Logs.logError(LOG, e, "Exception closing stream for file [%s]!", file.getPath());
				}
			}
		}
		return false;
	}

	/**
	 * Load properties.
	 *
	 * @param file the file
	 * @return the properties
	 */
	public static Properties loadProperties(File file) {
		try (InputStream in = new FileInputStream(file)) {
			return loadProperties(in);
		} catch (Throwable e) {
			logError(LOG, e, "Could not load properties at path [%s].", file);
		}
		return null;
	}

	/**
	 * Load properties.
	 *
	 * @param in the in
	 * @return the properties
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Properties loadProperties(InputStream in) throws IOException {
		Properties props = new Properties();
		props.load(in);
		return props;
	}

	/**
	 * Load properties safe.
	 *
	 * @param in the in
	 * @return the properties
	 */
	public static Properties loadPropertiesSafe(InputStream in) {
		try {
			return loadProperties(in);
		} catch (Throwable e) {
			logError(LOG, e, "Could not load properties.");
			return null;
		}
	}

	/**
	 * Merge directories.
	 *
	 * @param srcDir the src dir
	 * @param destDir the dest dir
	 * @param move the move
	 * @param overwrite the overwrite
	 */
	public static void mergeDirectories(File srcDir, File destDir, boolean move, boolean overwrite) {
		if ((srcDir == null) || !srcDir.isDirectory()) {
			Logs.logError(LOG, new FileNotFoundException(), "Source directory [%s] does not exist or destination directory is null [%s].", srcDir, destDir);
			return;
		}
		Stopwatch watch = LOG.isDebugEnabled() ? Timing.getStopwatch() : null;
		Logs.logDebug(LOG, "Merging [%s] into [%s]", srcDir, destDir);
		final ThrowingBiConsumer<File, File> populatefunc = (src, dest) -> {
			boolean isDir = src.isDirectory();
			if (isDir && move) {
				FileUtils.moveDirectory(src, dest);
			} else if (isDir) {
				FileUtils.copyDirectory(src, dest);
			} else if (move) {
				if (!dest.exists() || dest.delete()) {
					FileUtils.moveFile(src, dest);
				}
			} else {
				FileUtils.copyFile(src, dest);
			}
		};
		populateFromOther(srcDir, destDir, true, overwrite, populatefunc);
		if (move) {
			try {
				FileUtils.deleteDirectory(srcDir);
			} catch (IOException e) {
				Logs.logError(LOG, e, "Issue deleting directory [%s]", srcDir);
			}
		}
		if (watch != null) {
			Logs.logTimer(LOG, watch, "Merge Directories");
		}
	}

	/**
	 * Mkdirs with perms.
	 *
	 * @param dir the dir
	 * @param ownerOnly the owner only
	 * @return true, if successful
	 */
	public static boolean mkdirsWithPerms(File dir, boolean ownerOnly) {
		if (dir.exists()) {
			return false;
		}
		if (dir.mkdir()) {
			setFullPerms(dir, ownerOnly);
			return true;
		}
		File canonFile = null;
		try {
			canonFile = dir.getCanonicalFile();
		} catch (IOException e) {
			return false;
		}

		File parent = canonFile.getParentFile();
		boolean success = ((parent != null) && (mkdirsWithPerms(parent, ownerOnly) || parent.exists()) && canonFile.mkdir());
		if (success) {
			setFullPerms(canonFile, ownerOnly);
		}
		return success;
	}

	/**
	 * Modify recursively.
	 *
	 * @param fileOrDir the file or dir
	 * @param consumer the consumer
	 */
	public static void modifyRecursively(final File fileOrDir, final Consumer<File> consumer) {
		if ((fileOrDir != null) && fileOrDir.exists()) {
			consumer.accept(fileOrDir);
			if (fileOrDir.isDirectory()) {
				for (File child : ofNullable(fileOrDir.listFiles()).orElseGet(() -> new File[0])) {
					modifyRecursively(child, consumer);
				}
			}
		}
	}

	/**
	 * Move file.
	 *
	 * @param srcFile the src file
	 * @param destFile the dest file
	 * @return true, if successful
	 */
	public static boolean moveFile(@Nonnull File srcFile, @Nonnull File destFile) {
		try {
			Logs.logDebug(LOG, "Moving file: src=%s dest=%s", fullPath(srcFile), fullPath(destFile));
			if (destFile.exists()) {
				destFile.delete();
			}
			boolean rename = srcFile.renameTo(destFile);
			if (!rename) {
				copyFile(srcFile, destFile);
				if (!srcFile.delete()) {
					FileUtils.deleteQuietly(srcFile);
				}
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue moving [%s] to [%s]!", srcFile, destFile);
			return false;
		}
		return true;
	}

	/**
	 * Open input stream.
	 *
	 * @param file the file
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InputStream openInputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return file.getPath().endsWith(".gz") ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file);
	}

	/**
	 * Open input stream safe.
	 *
	 * @param file the file
	 * @return the input stream
	 */
	public static InputStream openInputStreamSafe(final File file) {
		try {
			return openInputStream(file);
		} catch (IOException e) {
			Logs.logError(LOG, e, "Errror while opening stream for file [%s]", file);
			return null;
		}
	}

	/**
	 * Populate from other.
	 *
	 * @param src the src
	 * @param dest the dest
	 * @param recursive the recursive
	 * @param overwrite the overwrite
	 * @param populatefunc the populatefunc
	 */
	public static void populateFromOther(File src, File dest, final boolean recursive, final boolean overwrite, final ThrowingBiConsumer<File, File> populatefunc) {
		try {
			if (!dest.exists()) {
				populatefunc.accept(src, dest);
			} else if (!dest.isDirectory() && overwrite) {
				if (dest.delete()) {
					populatefunc.accept(src, dest);
				} else {
					Logs.logWarn(LOG, "Could not delete dest file [%s] - not overwriting with source [%s].", dest, src);
				}
			}
			if (src.isDirectory() && recursive) {
				Arrays.stream(src.listFiles()).forEach(subSrc -> populateFromOther(subSrc, new File(dest, subSrc.getName()), recursive, overwrite, populatefunc));
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue populating [%s] from [%s]", dest, src);
		}
	}

	/**
	 * Read base64 encoded file string.
	 *
	 * @param path the path
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String readBase64EncodedFileString(String path) throws Exception {
		File keyFile = new File(path);
		if (keyFile.exists()) {
			String key = FileUtils.readFileToString(keyFile, StandardCharsets.UTF_8);
			byte[] keyBytes = Base64.getDecoder().decode(key);
			return new String(keyBytes);
		}
		return null;
	}

	/**
	 * Read directory.
	 *
	 * @param path the path
	 * @param extensions the extensions
	 * @param excludes the excludes
	 * @param includes the includes
	 * @return the string[]
	 */
	public static String[] readDirectory(String path, String[] extensions, String[] excludes, String[] includes) {
		List<IOFileFilter> filters = new ArrayList<>();
		List<IOFileFilter> dirFilters = new ArrayList<>();
		if ((extensions != null) && (extensions.length > 0)) {
			filters.add(new SuffixFileFilter(extensions));
		}
		if ((excludes != null) && (excludes.length > 0)) {
			final Pattern regex = Pattern.compile(".*(" + StringUtils.join(excludes, "|") + ").*");
			FileFilter filter = (file) -> regex.matcher(file.getPath()).matches();
			IOFileFilter dirFilter = new NotFileFilter(FileFilterUtils.asFileFilter(filter));
			filters.add(dirFilter);
			dirFilters.add(dirFilter);
		} else {
			dirFilters.add(FileFilterUtils.trueFileFilter());
		}
		if ((includes != null) && (includes.length > 0)) {
			final Pattern regex = Pattern.compile(".*(" + StringUtils.join(includes, "|") + ").*");
			FileFilter filter = (file) -> regex.matcher(file.getPath()).matches();
			filters.add(FileFilterUtils.asFileFilter(filter));
		}
		IOFileFilter filter = new AndFileFilter(filters);
		Collection<File> allFiles = FileUtils.listFiles(new File(path), filter, new AndFileFilter(dirFilters));
		String[] files = allFiles == null ? null : allFiles.stream().map(FileUtil::fullPath).collect(Collectors.toList()).toArray(new String[] {});
		return files;
	}

	/**
	 * Read file.
	 *
	 * @param file the file
	 * @return the string
	 */
	public static String readFile(File file) {
		return readFile(file, UTF_8);
	}

	/**
	 * Read file safe.
	 *
	 * @param file the file
	 * @param encoding the encoding
	 * @return the string
	 */
	public static String readFile(File file, Charset encoding) {
		try (InputStream in = openInputStream(file)) {
			return IOUtils.toString(in, Charsets.toCharset(encoding));
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue reading file [%s] to string.", file);
		}
		return null;
	}

	/**
	 * Read file.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String readFile(String path) {
		return readFile(new File(path));
	}

	/**
	 * Read file as list.
	 *
	 * @param file the file
	 * @return the list
	 * @throws Exception the exception
	 */
	public static List<String> readFileAsList(File file) throws Exception {
		String data = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		if (StringUtils.isNotBlank(data)) {
			return new ArrayList<>(asList(split(data, ",")));
		}
		return new ArrayList<>();
	}

	/**
	 * Read file as list.
	 *
	 * @param path the path
	 * @return the list
	 * @throws Exception the exception
	 */
	public static List<String> readFileAsList(String path) throws Exception {
		return readFileAsList(getFile(path));
	}

	/**
	 * Read file line.
	 *
	 * @param file the file
	 * @param lineNum the line num
	 * @return the string
	 */
	public static String readFileLine(File file, int lineNum) {
		if ((file != null) && (lineNum > 0)) {
			LineNumberReader reader = null;
			try {
				reader = getFileReader(file);
				reader.setLineNumber(lineNum);
				return reader.readLine();
			} catch (Throwable e) {
				Logs.logError(LOG, e, "Issue reading line [%s] of file [%s].", lineNum, file);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		return null;
	}

	/**
	 * Read file to string.
	 *
	 * @param file the file
	 * @param removeLinesWithPrefixes the remove lines with prefixes
	 * @return the string
	 */
	public static String readFileToString(File file, Set<String> removeLinesWithPrefixes) {
		return new FileParser<>(file, null, new FileParserOptions().setRemoveLinesWithPrefixes(removeLinesWithPrefixes)).parseFileToString().getContents();
	}

	/**
	 * Removes the invalid chars for file name.
	 *
	 * @param name the name
	 * @return the string
	 */
	public static String removeInvalidCharsForFileName(String name) {
		return INVALID_FILENAME.matcher(name).replaceAll("");
	}

	/**
	 * Sets the apache perms.
	 *
	 * @param file the new apache perms
	 * @return true, if successful
	 */
	public static boolean setApachePerms(final File file) {
		return setPermsRecursive(file, true, false, true, true, false, false);
	}

	/**
	 * Sets the full perms.
	 *
	 * @param file the file
	 * @param ownerOnly the owner only
	 * @return true, if successful
	 */
	public static boolean setFullPerms(File file, boolean ownerOnly) {
		return setPerms(file, true, false, true, ownerOnly, true, ownerOnly);
	}

	/**
	 * Sets the full perms recursive.
	 *
	 * @param dir the dir
	 * @param ownerOnly the owner only
	 * @return true, if successful
	 */
	public static boolean setFullPermsRecursive(File dir, boolean ownerOnly) {
		return setPermsRecursive(dir, true, false, true, ownerOnly, true, ownerOnly);
	}

	/**
	 * Sets the perms.
	 *
	 * @param file the file
	 * @param read the read
	 * @param readOwnerOnly the read owner only
	 * @param write the write
	 * @param writeOwnerOnly the write owner only
	 * @param exe the exe
	 * @param exeOwnerOnly the exe owner only
	 * @return true, if successful
	 */
	public static boolean setPerms(
		final File file,
		final boolean read,
		final boolean readOwnerOnly,
		final boolean write,
		final boolean writeOwnerOnly,
		final boolean exe,
		final boolean exeOwnerOnly
	) {
		boolean success = (file != null);
		if (file == null) {
			return success;
		}
		try {
			success = file.setExecutable(exe, exeOwnerOnly) && success;
			success = file.setReadable(read, readOwnerOnly) && success;
			success = file.setWritable(write, writeOwnerOnly) && success;
		} catch (Throwable e) {
			Logs.logWarn(LOG, e, "Error setting perms [read:%s/%s - write:%s/%s - execute:%s/%s] for file [%s]!", read, readOwnerOnly, write, writeOwnerOnly, exe, exeOwnerOnly, file);
			success = false;
		}
		return success;
	}

	/**
	 * Sets the perms recursive.
	 *
	 * @param dir the dir
	 * @param read the read
	 * @param readOwnerOnly the read owner only
	 * @param write the write
	 * @param writeOwnerOnly the write owner only
	 * @param exe the exe
	 * @param exeOwnerOnly the exe owner only
	 * @return true, if successful
	 */
	public static final boolean setPermsRecursive(
		final File dir,
		final boolean read,
		final boolean readOwnerOnly,
		final boolean write,
		final boolean writeOwnerOnly,
		final boolean exe,
		final boolean exeOwnerOnly
	) {
		boolean success = (dir != null) && dir.exists();
		try {
			if (!success) {
				throw new FileNotFoundException(dir != null ? dir.getPath() : null);
			}
			success = setPerms(dir, read, readOwnerOnly, write, writeOwnerOnly, exe, exeOwnerOnly) && success;
			if (dir.isDirectory()) {
				for (File child : ofNullable(dir.listFiles()).orElseGet(() -> new File[0])) {
					if (child.isDirectory()) {
						success = setPermsRecursive(child, read, readOwnerOnly, write, writeOwnerOnly, exe, exeOwnerOnly) && success;
					} else {
						success = setPerms(child, read, readOwnerOnly, write, writeOwnerOnly, exe, exeOwnerOnly) && success;
					}
				}
			}
		} catch (Throwable e) {
			Logs.logWarn(
				LOG,
				e,
				"Error setting recursive perms [read:%s/%s - write:%s/%s - execute:%s/%s] for file [%s]!",
				read,
				readOwnerOnly,
				write,
				writeOwnerOnly,
				exe,
				exeOwnerOnly,
				dir
			);
			success = false;
		}
		return success;
	}

	/**
	 * Sets the perms recursive.
	 *
	 * @param fileOrDir the file or dir
	 * @param perms the perms
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void setPermsRecursive(@Nonnull final File fileOrDir, @Nonnull final Set<PosixFilePermission> perms) throws IOException {
		try {
			Files.setPosixFilePermissions(fileOrDir.toPath(), perms);
		} catch (FileSystemException e) {
			if (!StringUtils.contains(e.getMessage(), "not permitted")) {
				throw e;
			}
		}
		if (fileOrDir.isDirectory()) {
			for (File child : ofNullable(fileOrDir.listFiles()).orElseGet(() -> new File[0])) {
				setPermsRecursive(child, perms);
			}
		}
	}

	/**
	 * Sets the perms recursive.
	 *
	 * @param fileOrDir the file or dir
	 * @param permsPosix the perms posix
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void setPermsRecursive(@Nonnull final File fileOrDir, @Nonnull final String permsPosix) throws IOException {
		setPermsRecursive(fileOrDir, PosixFilePermissions.fromString(permsPosix));
	}

	/**
	 * Unzip file to destination path.
	 *
	 * @param srcFile the source directory where the zip file is present.
	 * @param destDir the destination path.
	 */
	@SuppressWarnings("resource")
	public static void unzip(final File srcFile, final File destDir) {
		if (srcFile == null) {
			throw new IllegalArgumentException("Source must not be null");
		}
		if (destDir == null) {
			throw new IllegalArgumentException("Destination dir must not be null");
		}
		if (!destDir.exists()) {
			createDirectory(destDir, false);
		}
		try (ZipFile zipFile = new ZipFile(srcFile)) {
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				String name = zipEntry.getName();
				Logs.logDebug(LOG, "name: %-20s | size: %6d \n", name, zipEntry.getSize());
				File file = new File(destDir + File.separator + name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
				}
				is.close();
				fos.close();

			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use correct file seps.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String useCorrectFileSeps(String path) {
		String toReplace = "\\";
		if (separator.equalsIgnoreCase("\\")) {
			toReplace = "/";
		}
		return StringUtils.replace(path, toReplace, separator);
	}

	/**
	 * Write file.
	 *
	 * @param file the file
	 * @param data the data
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final byte[] data) {
		try {
			FileUtils.writeByteArrayToFile(file, data);
			return true;
		} catch (IOException e) {
			Logs.logError(LOG, e, "Issue writing byte array to file: file=%s", fullPath(file));
			return false;
		}
	}

	/**
	 * Write file safe.
	 *
	 * @param file the file
	 * @param data the data
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final String data) {
		return writeFile(file, data, false);
	}

	/**
	 * Write file safe.
	 *
	 * @param file the file
	 * @param data the data
	 * @param append the append
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final String data, final boolean append) {
		return writeFile(file, data, UTF_8, append);
	}

	/**
	 * Write file safe.
	 *
	 * @param file the file
	 * @param data the data
	 * @param encoding the encoding
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final String data, final Charset encoding) {
		return writeFile(file, data, encoding, false);
	}

	/**
	 * Write file safe.
	 *
	 * @param file the file
	 * @param data the data
	 * @param encoding the encoding
	 * @param append the append
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final String data, final Charset encoding, final boolean append) {
		try {
			FileUtils.writeStringToFile(file, data, encoding, append);
			return true;
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue writing [%s] to file [%s].", data == null ? null : data.length() < 2000 ? data : "PLACEHOLDER: Too big to log.", file);
			return false;
		}
	}

	/**
	 * Write file.
	 *
	 * @param file the file
	 * @param data the data
	 * @param encoding the encoding
	 * @return true, if successful
	 */
	public static boolean writeFile(final File file, final String data, final String encoding) {
		return writeFile(file, data, encoding == null ? null : Charsets.toCharset(encoding), false);
	}

}
