package com.github.lancethomps.lava.common.diff;

import static com.github.lancethomps.lava.common.Checks.isEmpty;
import static com.github.lancethomps.lava.common.Checks.isNotEmpty;
import static com.github.lancethomps.lava.common.ContextUtil.getCpFile;
import static com.github.lancethomps.lava.common.collections.MapUtil.addToMap;
import static com.github.lancethomps.lava.common.diff.domain.DiffOutputFormat.JSON;
import static com.github.lancethomps.lava.common.ser.Serializer.toPrettyJson;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.tuple.Pair.of;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.CommonConstants;
import com.github.lancethomps.lava.common.diff.DiffMatchPatch.DiffOperation;
import com.github.lancethomps.lava.common.diff.domain.DiffBlock;
import com.github.lancethomps.lava.common.diff.domain.DiffFile;
import com.github.lancethomps.lava.common.diff.domain.DiffFileHtml;
import com.github.lancethomps.lava.common.diff.domain.DiffHighlight;
import com.github.lancethomps.lava.common.diff.domain.DiffHighlightBlock;
import com.github.lancethomps.lava.common.diff.domain.DiffLine;
import com.github.lancethomps.lava.common.diff.domain.DiffLineType;
import com.github.lancethomps.lava.common.diff.domain.DiffOutputDestination;
import com.github.lancethomps.lava.common.diff.domain.DiffOutputFormat;
import com.github.lancethomps.lava.common.file.FileUtil;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.os.OsUtil;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.string.StringUtil;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Lists;

public class DiffToHtml {

	public static final DiffMatchPatch DMP = new DiffMatchPatch();

	public static final Map<String, String> LINE_TYPE = Collections
		.unmodifiableMap(Arrays.stream(DiffLineType.values()).collect(Collectors.toMap(DiffLineType::name, DiffLineType::getValue)));

	private static final Pattern COMBINED_DELETED_FILE = Pattern.compile("^deleted file mode (\\d{6}),(\\d{6})");

	private static final Pattern COMBINED_INDEX = Pattern.compile("^index ([0-9a-z]+),([0-9a-z]+)\\.\\.([0-9a-z]+)");

	private static final Pattern COMBINED_MODE = Pattern.compile("^mode (\\d{6}),(\\d{6})\\.\\.(\\d{6})");

	private static final Pattern COMBINED_NEW_FILE = Pattern.compile("^new file mode (\\d{6})");

	private static final Pattern COMBINED_REGEX = Pattern.compile("^@@@ -(\\d+)(?:,\\d+)? -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@@.*");

	private static final Pattern COPY_FROM = Pattern.compile("^copy from \"?(.+)\"?");

	private static final Pattern COPY_TO = Pattern.compile("^copy to \"?(.+)\"?");

	private static final Pattern DEL_REGEX = Pattern.compile("(<del[^>]*>((.|\\n)*?)<\\/del>)");

	private static final Pattern DELETED_FILE_MODE = Pattern.compile("^deleted file mode (\\d{6})");

	private static final String DEST_FILE_PREFIX = "+++";

	private static final List<String> DEST_FILE_PREFIXES = Lists.newArrayList("b/", "i/", "w/", "c/", "o/");

	private static final Pattern DISSIMILARITY_INDEX = Pattern.compile("^dissimilarity index (\\d+)%");

	private static final Pattern INDEX = Pattern.compile("^index ([0-9a-z]+)\\.\\.([0-9a-z]+)\\s*(\\d{6})?");

	private static final Pattern INS_REGEX = Pattern.compile("(<ins[^>]*>((.|\\n)*?)<\\/ins>)");

	private static final Logger LOG = Logger.getLogger(DiffToHtml.class);

	private static final MustacheFactory MUSTACHE = new DefaultMustacheFactory();

	private static ConcurrentHashMap<String, Mustache> mustacheTemplates = new ConcurrentHashMap<>();

	private static final Pattern NEW_FILE_MODE = Pattern.compile("^new file mode (\\d{6})");

	private static final Pattern NEW_MODE = Pattern.compile("^new mode (\\d{6})");

	private static final Pattern NON_COMBINED_REGEX = Pattern.compile("^@@ -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@.*");

	private static final Pattern OLD_MODE = Pattern.compile("^old mode (\\d{6})");

	private static final Pattern RENAME_FROM = Pattern.compile("^rename from \"?(.+)\"?");

	private static final Pattern RENAME_TO = Pattern.compile("^rename to \"?(.+)\"?");

	private static final Pattern SIMILARITY_INDEX = Pattern.compile("^similarity index (\\d+)%");

	private static final String SRC_FILE_PREFIX = "---";

	private static final List<String> SRC_FILE_PREFIXES = Lists.newArrayList("a/", "i/", "w/", "c/", "o/");

	private static final String TEMPLATES_ROOT = "templates/d2h/";

	private boolean charByChar;

	private boolean combined;

	private DiffBlock currentBlock;

	private DiffFile currentFile;

	private List<DiffFile> diffFiles = Lists.newArrayList();

	private String diffInput;

	private DiffOutputDestination diffOutputDestination = DiffOutputDestination.PREVIEW;

	private String diffOutputFile;

	private DiffOutputFormat diffOutputFormat = DiffOutputFormat.HTML;

	private String html;

	private String htmlTitle;

	private boolean ignoreAdditions;

	private boolean ignoreDeletions;

	private String matching;

	private int maxLineLengthForHighlight = Integer.MAX_VALUE;

	private AtomicInteger newLine;

	private AtomicInteger oldLine;

	private boolean parsed;

	private String singleLineContextHtml;

	private boolean unwrappedHtml;

	public static Mustache getMustacheTemplate(String relativePath) {
		String path = TEMPLATES_ROOT + relativePath;
		return mustacheTemplates.computeIfAbsent(
			path,
			k -> {
				File file = getCpFile(path);
				try {
					String fileContents;
					if (file != null && file.isFile()) {
						fileContents = readFileToString(file, UTF_8);
					} else {
						fileContents = IOUtils.toString(DiffToHtml.class.getResourceAsStream('/' + path), UTF_8);
					}
					return MUSTACHE.compile(new StringReader(fileContents), path);
				} catch (Throwable e) {
					throw new IllegalArgumentException(String.format("Error reading template file: relativePath=%s file=%s", relativePath, FileUtil.fullPath(file)), e);
				}
			}
		);
	}

	public static String getResourceFile(String relativePath) {
		return FileUtil.getFileContentsWithImports(CommonConstants.CLASSPATH_PREFIX + TEMPLATES_ROOT + relativePath);
	}

	public DiffToHtml generateHtml() throws Exception {
		if (!parsed) {
			parseDiff();
		}

		List<String> fileListHtml = Lists.newArrayList();
		List<String> diffHtml = Lists.newArrayList();
		for (DiffFile file : diffFiles) {
			Map<String, Object> templateData = getTemplateDataForFile(file);
			fileListHtml.add(renderTemplate("file-summary", "line", templateData));

			DiffFileHtml fileHtml = null;
			if (isNotEmpty(file.getBlocks())) {
				fileHtml = generateSideBySideFileHtml(file);
			} else {
				fileHtml = generateEmptyDiff();
			}
			templateData.put("diffs", fileHtml);
			diffHtml.add(renderTemplate("side-by-side", "file-diff", templateData));
		}
		String filesContent = StringUtils.join(fileListHtml, System.lineSeparator());
		String diffOutput = StringUtils.join(diffHtml, System.lineSeparator());
		String rawHtml = renderTemplate("file-summary", "wrapper", of("filesNumber", diffFiles.size()), of("files", filesContent)) +
			renderTemplate("generic", "wrapper", of("content", diffOutput));
		String css = getResourceFile("diff2html.css");
		String js = StringUtils.replace(getResourceFile("diff2html.js"), "</script", "</scr\\ipt");
		html = renderTemplate(
			"diff2html",
			unwrappedHtml ? "unwrapped" : null,
			of("pageTitle", defaultIfBlank(htmlTitle, "Diff to HTML")),
			of("diff", rawHtml),
			of("d2hCss", css),
			of("d2hJs", js)
		);

		return this;
	}

	public List<DiffFile> getDiffFiles() {
		return diffFiles;
	}

	public String getDiffInput() {
		return diffInput;
	}

	public DiffOutputDestination getDiffOutputDestination() {
		return diffOutputDestination;
	}

	public String getDiffOutputFile() {
		return diffOutputFile;
	}

	public DiffOutputFormat getDiffOutputFormat() {
		return diffOutputFormat;
	}

	public String getHtml() {
		return html;
	}

	public String getHtmlTitle() {
		return htmlTitle;
	}

	public String getMatching() {
		return matching;
	}

	public String getSingleLineContextHtml() {
		if (singleLineContextHtml == null) {
			singleLineContextHtml = renderTemplate(
				"generic",
				"line",
				addToMap(
					newHashMap(),
					of("type", DiffLineType.CONTEXT.getValue()),
					of("lineClass", "d2h-code-side-linenumber"),
					of("contentClass", "d2h-code-side-line"),
					of("prefix", ""),
					of("content", ""),
					of("lineNumber", "")
				)
			);
		}
		return singleLineContextHtml;
	}

	public boolean isCharByChar() {
		return charByChar;
	}

	public boolean isCombined() {
		return combined;
	}

	public boolean isIgnoreAdditions() {
		return ignoreAdditions;
	}

	public boolean isIgnoreDeletions() {
		return ignoreDeletions;
	}

	public boolean isParsed() {
		return parsed;
	}

	public boolean isUnwrappedHtml() {
		return unwrappedHtml;
	}

	public DiffToHtml parseDiff() throws Exception {
		List<String> diffLines = StringUtil.splitLines(StringUtils.replace(diffInput, " No newline at end of file", ""));
		for (String line : diffLines) {
			if (StringUtils.isBlank(line) || line.startsWith("*")) {
				continue;
			}
			if (line.startsWith("diff") || (currentFile == null)
				|| (((currentFile != null) && (isNotBlank(currentFile.getOldName()) && line.startsWith(SRC_FILE_PREFIX)))
					|| (isNotBlank(currentFile.getNewName()) && line.startsWith(DEST_FILE_PREFIX)))) {
				startFile();
			}
			if ((currentFile != null) && (currentFile.getOldName() == null) && line.startsWith(SRC_FILE_PREFIX) && (getFilename(SRC_FILE_PREFIX, line, SRC_FILE_PREFIXES) != null)) {
				currentFile.setOldName(getFilename(SRC_FILE_PREFIX, line, SRC_FILE_PREFIXES));
				currentFile.setLanguage(defaultIfBlank(getExtension(currentFile.getOldName()), currentFile.getLanguage()));
				continue;
			}

			if ((currentFile != null) && (currentFile.getNewName() == null) && line.startsWith(DEST_FILE_PREFIX) && (getFilename(DEST_FILE_PREFIX, line, DEST_FILE_PREFIXES) != null)) {
				currentFile.setNewName(getFilename(DEST_FILE_PREFIX, line, DEST_FILE_PREFIXES));
				currentFile.setLanguage(defaultIfBlank(getExtension(currentFile.getNewName()), currentFile.getLanguage()));
				continue;
			}

			if ((currentFile != null) && line.startsWith("@")) {
				startBlock(line);
				continue;
			}

			if ((currentBlock != null) && (line.startsWith("+") || line.startsWith("-") || line.startsWith(" "))) {
				createLine(line);
				continue;
			}

			if (((currentFile != null) && isNotEmpty(currentFile.getBlocks())) || ((currentBlock != null) && isNotEmpty(currentBlock.getLines()))) {
				startFile();
			}

			Matcher matcher = null;
			if ((matcher = OLD_MODE.matcher(line)).find()) {
				currentFile.setOldMode(matcher.group(1));
			} else if ((matcher = NEW_MODE.matcher(line)).find()) {
				currentFile.setNewMode(matcher.group(1));
			} else if ((matcher = DELETED_FILE_MODE.matcher(line)).find()) {
				currentFile.setDeletedFileMode(matcher.group(1));
				currentFile.setDeleted(true);
			} else if ((matcher = NEW_FILE_MODE.matcher(line)).find()) {
				currentFile.setNewFileMode(matcher.group(1));
				currentFile.setNewFile(true);
			} else if ((matcher = COPY_FROM.matcher(line)).find()) {
				currentFile.setOldName(matcher.group(1));
				currentFile.setCopy(true);
			} else if ((matcher = COPY_TO.matcher(line)).find()) {
				currentFile.setNewName(matcher.group(1));
				currentFile.setCopy(true);
			} else if ((matcher = RENAME_FROM.matcher(line)).find()) {
				currentFile.setOldName(matcher.group(1));
				currentFile.setRename(true);
			} else if ((matcher = RENAME_TO.matcher(line)).find()) {
				currentFile.setNewName(matcher.group(1));
				currentFile.setRename(true);
			} else if ((matcher = SIMILARITY_INDEX.matcher(line)).find()) {
				currentFile.setUnchangedPercentage(matcher.group(1));
			} else if ((matcher = DISSIMILARITY_INDEX.matcher(line)).find()) {
				currentFile.setChangedPercentage(matcher.group(1));
			} else if ((matcher = INDEX.matcher(line)).find()) {
				currentFile.setChecksumBefore(matcher.group(1));
				currentFile.setChecksumAfter(matcher.group(2));
				if (matcher.group(3) != null) {
					currentFile.setMode(matcher.group(3));
				}
			} else if ((matcher = COMBINED_INDEX.matcher(line)).find()) {
				currentFile.setChecksumBefore(matcher.group(2) + ',' + matcher.group(3));
				currentFile.setChecksumAfter(matcher.group(1));
			} else if ((matcher = COMBINED_MODE.matcher(line)).find()) {
				currentFile.setOldMode(matcher.group(2) + ',' + matcher.group(3));
				currentFile.setNewMode(matcher.group(1));
			} else if ((matcher = COMBINED_NEW_FILE.matcher(line)).find()) {
				currentFile.setNewFileMode(matcher.group(1));
				currentFile.setNewFile(true);
			} else if ((matcher = COMBINED_DELETED_FILE.matcher(line)).find()) {
				currentFile.setDeletedFileMode(matcher.group(1));
				currentFile.setDeleted(true);
			}
		}
		saveBlock();
		saveFile();
		parsed = true;
		return this;
	}

	public void run() {
		try {
			parseDiff();
			String output = diffOutputFormat == DiffOutputFormat.DEBUG ? toPrettyJson(generateHtml()) : diffOutputFormat == JSON ? toPrettyJson(getDiffFiles()) : generateHtml().getHtml();
			if (diffOutputDestination == DiffOutputDestination.PREVIEW) {
				boolean openOutput = true;
				if (isBlank(diffOutputFile)) {
					diffOutputFile = File.createTempFile("d2h-", diffOutputFormat == DiffOutputFormat.HTML ? ".html" : ".json").getPath();
				}
				File out = new File(diffOutputFile);
				FileUtil.writeFile(out, output);
				FileUtil.setPerms(out, true, false, true, true, true, false);
				Logs.println(out.getPath());
				if (openOutput && OsUtil.isLocal()) {
					OsUtil.openUrl(diffOutputFile);
				}
			} else {
				Logs.println(output);
			}
		} catch (Throwable e) {
			Logs.logFatal(LOG, e, "Error processing DiffToHtml [%s]", Serializer.toLogString(this));
		}
	}

	public DiffToHtml setCharByChar(boolean charByChar) {
		this.charByChar = charByChar;
		return this;
	}

	public DiffToHtml setDiffInput(String diffInput) {
		this.diffInput = diffInput;
		return this;
	}

	public DiffToHtml setDiffOutputDestination(DiffOutputDestination diffOutputDestination) {
		this.diffOutputDestination = diffOutputDestination;
		return this;
	}

	public DiffToHtml setDiffOutputFile(String diffOutput) {
		diffOutputFile = diffOutput;
		return this;
	}

	public DiffToHtml setDiffOutputFormat(DiffOutputFormat diffOutputFormat) {
		this.diffOutputFormat = diffOutputFormat;
		return this;
	}

	public DiffToHtml setHtmlTitle(String htmlTitle) {
		this.htmlTitle = htmlTitle;
		return this;
	}

	public DiffToHtml setIgnoreAdditions(boolean ignoreAdditions) {
		this.ignoreAdditions = ignoreAdditions;
		return this;
	}

	public DiffToHtml setIgnoreDeletions(boolean ignoreDeletions) {
		this.ignoreDeletions = ignoreDeletions;
		return this;
	}

	public DiffToHtml setMatching(String matching) {
		this.matching = matching;
		return this;
	}

	public DiffToHtml setUnwrappedHtml(boolean unwrappedHtml) {
		this.unwrappedHtml = unwrappedHtml;
		return this;
	}

	private void createLine(String line) {
		DiffLine currentLine = new DiffLine();
		currentLine.setContent(line);

		List<String> newLinePrefixes = !currentFile.isCombined() ? Lists.newArrayList("+") : Lists.newArrayList("+", " +");
		List<String> delLinePrefixes = !currentFile.isCombined() ? Lists.newArrayList("-") : Lists.newArrayList("-", " -");

		if (newLinePrefixes.stream().anyMatch(line::startsWith)) {
			currentBlock.getAddedLines().incrementAndGet();
			currentFile.getAddedLines().incrementAndGet();
			currentLine.setType(DiffLineType.INSERTS).setOldNumber(null).setNewNumber(newLine.getAndIncrement());
		} else if (delLinePrefixes.stream().anyMatch(line::startsWith)) {
			currentBlock.getDeletedLines().incrementAndGet();
			currentFile.getDeletedLines().incrementAndGet();
			currentLine.setType(DiffLineType.DELETES).setOldNumber(oldLine.getAndIncrement()).setNewNumber(null);
		} else {
			currentLine.setType(DiffLineType.CONTEXT).setOldNumber(oldLine.getAndIncrement()).setNewNumber(newLine.getAndIncrement());
		}
		currentBlock.getLines().add(currentLine);
	}

	private DiffHighlight diffHighlight(String diffLine1, String diffLine2) {
		int prefixSize = 1;
		if (combined) {
			prefixSize = 2;
		}
		boolean useOld = false;
		String linePrefix1 = diffLine1.substring(0, prefixSize);
		String linePrefix2 = diffLine2.substring(0, prefixSize);
		String unprefixedLine1 = diffLine1.substring(prefixSize);
		String unprefixedLine2 = diffLine2.substring(prefixSize);
		try {
			if ((maxLineLengthForHighlight > 0) && ((unprefixedLine1.length() > maxLineLengthForHighlight) || (unprefixedLine2.length() > maxLineLengthForHighlight))) {
				return new DiffHighlight(
					new DiffHighlightBlock(linePrefix1, unprefixedLine1),
					new DiffHighlightBlock(linePrefix2, unprefixedLine2)
				);
			}
			List<DiffMatchPatch.Diff> diff = DMP.diffMainAtWordLevel(unprefixedLine1, unprefixedLine2);
			StringBuilder highlightedLine = new StringBuilder();
			StringBuilder nonIns = new StringBuilder();
			StringBuilder nonDel = new StringBuilder();
			List<String> changedWords = Lists.newArrayList();
			if (!charByChar) {
				double threshold = 0.25;
			}
			for (DiffMatchPatch.Diff part : diff) {
				String addClass = changedWords.contains(part.getText()) ? " class=\"d2h-change\"" : "";
				String elemType = part.getOperation() != null ? part.getOperation().getHtmlTag() : null;
				String escapedValue = escapeHtml(part.getText());
				if (elemType != null) {
					if (useOld) {
						highlightedLine.append('<' + elemType + addClass + '>' + escapedValue + "</" + elemType + '>');
					} else {
						(part.getOperation() == DiffOperation.DELETE ? nonIns : nonDel).append('<' + elemType + addClass + '>' + escapedValue + "</" + elemType + '>');
					}
				} else if (useOld) {
					highlightedLine.append(escapedValue);
				} else {
					nonIns.append(escapedValue);
					nonDel.append(escapedValue);
				}
			}
			if (useOld) {
				String highlighted = highlightedLine.toString();
				Logs.logTrace(LOG, "Line to highlight is [%s]", highlighted);
				return new DiffHighlight(
					new DiffHighlightBlock(linePrefix1, INS_REGEX.matcher(highlighted).replaceAll("")),
					new DiffHighlightBlock(linePrefix2, DEL_REGEX.matcher(highlighted).replaceAll(""))
				);
			}
			return new DiffHighlight(
				new DiffHighlightBlock(linePrefix1, nonIns.toString()),
				new DiffHighlightBlock(linePrefix2, nonDel.toString())
			);
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue highlighting diff lines [%s] and [%s]", diffLine1, diffLine2);
			return new DiffHighlight(
				new DiffHighlightBlock(linePrefix1, unprefixedLine1),
				new DiffHighlightBlock(linePrefix2, unprefixedLine2)
			);
		}
	}

	private String escapeHtml(String str) {
		return StringUtils.replaceEach(str, new String[] { "&", "<", ">", "\t" }, new String[] { "&amp;", "&lt;", "&gt;", "    " });
	}

	private String generateBlockHtml(Map<String, Object> templateData, String blockHeader) {
		templateData.put("blockHeader", blockHeader);
		return renderTemplate("generic", "column-line-number", templateData);
	}

	private DiffFileHtml generateEmptyDiff() {
		DiffFileHtml fileHtml = new DiffFileHtml();
		fileHtml.setRight("");
		fileHtml.setLeft(
			getTemplate("generic", "empty-diff")
				.execute(
					new StringWriter(),
					addToMap(
						newHashMap(),
						Pair.of("contentClass", "d2h-code-side-line"),
						Pair.of("diffParser", this)
					)
				)
				.toString()
		);
		return fileHtml;
	}

	private DiffFileHtml generateSideBySideFileHtml(DiffFile file) {
		DiffFileHtml fileHtml = new DiffFileHtml();
		StringBuilder left = new StringBuilder();
		StringBuilder right = new StringBuilder();
		Map<String, Object> sharedTemplateData = new HashMap<>(file.getTemplateData());
		sharedTemplateData.put("lineClass", "d2h-code-side-linenumber");
		sharedTemplateData.put("contentClass", "d2h-code-side-line");
		for (DiffBlock block : file.getBlocks()) {
			Map<String, Object> blockTemplateData = new HashMap<>(sharedTemplateData);
			left.append(generateBlockHtml(blockTemplateData, block.getHeader()));
			right.append(generateBlockHtml(blockTemplateData, ""));
			List<DiffLine> oldLines = Lists.newArrayList();
			List<DiffLine> newLines = Lists.newArrayList();
			for (DiffLine line : block.getLines()) {
				Map<String, Object> lineTemplateData = getTemplateDataForLine(file, line, sharedTemplateData);
				if ((line.getType() != DiffLineType.INSERTS) && (isNotEmpty(newLines) || ((line.getType() != DiffLineType.DELETES) && isNotEmpty(oldLines)))) {
					processChangeBlock(file, left, right, oldLines, newLines);
				}
				if (line.getType() == DiffLineType.CONTEXT) {
					left.append(generateSingleLineHtml(lineTemplateData, line.getOldNumber()));
					right.append(generateSingleLineHtml(lineTemplateData, line.getNewNumber()));
				} else if ((line.getType() == DiffLineType.INSERTS) && isEmpty(oldLines)) {
					left.append(getSingleLineContextHtml());
					right.append(generateSingleLineHtml(lineTemplateData, line.getNewNumber()));
				} else if (line.getType() == DiffLineType.DELETES) {
					oldLines.add(line);
				} else if ((line.getType() == DiffLineType.INSERTS) && isNotEmpty(oldLines)) {
					newLines.add(line);
				} else {
					Logs.logError(LOG, new Exception(), "unknown state in html side-by-side generator");
					processChangeBlock(file, left, right, oldLines, newLines);
				}
			}
			processChangeBlock(file, left, right, oldLines, newLines);
		}
		fileHtml.setLeft(left.toString());
		fileHtml.setRight(right.toString());
		return fileHtml;
	}

	private String generateSingleLineHtml(Map<String, Object> templateData, Integer number) {
		templateData.put("lineNumber", number == null ? "" : number.toString());
		return renderTemplate("generic", "line", templateData);
	}

	private String generateSingleLineHtml(String type, Integer number, String content, String prefix) {
		return renderTemplate(
			"generic",
			"line",
			Pair.of("type", type),
			Pair.of("lineClass", "d2h-code-side-linenumber"),
			Pair.of("contentClass", "d2h-code-side-line"),
			Pair.of("prefix", prefix != null ? prefix.replaceAll(" ", "&nbsp;") : null),
			Pair.of("content", content != null ? content.replaceAll(" ", "&nbsp;") : null),
			Pair.of("lineNumber", number == null ? "" : number.toString())
		);
	}

	private String getDiffName(DiffFile file) {
		String oldFilename = StringUtils.replace(file.getOldName(), "\\", "/");
		String newFilename = StringUtils.replace(file.getNewName(), "\\", "/");
		String separator = "/";

		if ((oldFilename != null) && (newFilename != null) && !oldFilename.equals(newFilename) && !StringUtils.contains(oldFilename, "dev/null")
			&& !StringUtils.contains(newFilename, "dev/null")) {
			List<String> prefixPaths = Lists.newArrayList();
			List<String> suffixPaths = Lists.newArrayList();

			String[] oldFilenameParts = oldFilename.split(separator);
			String[] newFilenameParts = newFilename.split(separator);

			int oldFilenamePartsSize = oldFilenameParts.length;
			int newFilenamePartsSize = newFilenameParts.length;

			int i = 0;
			int j = oldFilenamePartsSize - 1;
			int k = newFilenamePartsSize - 1;

			while ((i < j) && (i < k)) {
				if (oldFilenameParts[i].equals(newFilenameParts[i])) {
					prefixPaths.add(newFilenameParts[i]);
					i += 1;
				} else {
					break;
				}
			}

			while ((j > i) && (k > i)) {
				if (oldFilenameParts[j].equals(newFilenameParts[k])) {
					suffixPaths.add(0, newFilenameParts[k]);
					j -= 1;
					k -= 1;
				} else {
					break;
				}
			}

			String finalPrefix = join(prefixPaths, separator);
			String finalSuffix = StringUtils.join(suffixPaths, separator);

			String oldRemainingPath = join(Arrays.copyOfRange(oldFilenameParts, i, j + 1), separator);
			String newRemainingPath = join(Arrays.copyOfRange(newFilenameParts, i, k + 1), separator);

			if (isNotBlank(finalPrefix) && isNotBlank(finalSuffix)) {
				return finalPrefix + separator + '{' + oldRemainingPath + " → " + newRemainingPath + '}' + separator + finalSuffix;
			} else if (isNotBlank(finalPrefix)) {
				return finalPrefix + separator + '{' + oldRemainingPath + " → " + newRemainingPath + '}';
			} else if (isNotBlank(finalSuffix)) {
				return '{' + oldRemainingPath + " → " + newRemainingPath + '}' + separator + finalSuffix;
			}

			return oldFilename + " → " + newFilename;

		} else if ((newFilename != null) && !StringUtils.contains(newFilename, "dev/null")) {
			return newFilename;
		} else if (oldFilename != null) {
			return oldFilename;
		}

		return "unknown/file/path";
	}

	private String getFilename(String linePrefix, String line, List<String> prefixes) {
		Pattern regex = Pattern.compile('^' + Pattern.quote(linePrefix) + " \"?(.+?)\"?$");
		String filename = null;
		Matcher matcher = regex.matcher(line);
		if (matcher.find() && (matcher.group(1) != null)) {
			String filenameMatch = matcher.group(1);
			filename = prefixes.stream().filter(filenameMatch::startsWith).findFirst().map(prefix -> StringUtils.removeStart(filenameMatch, prefix)).orElse(filenameMatch);
		}
		return filename;
	}

	private String getFileTypeIcon(DiffFile file) {
		String templateName = "file-changed";

		if (file.testRename()) {
			templateName = "file-renamed";
		} else if (file.testCopy()) {
			templateName = "file-renamed";
		} else if (file.testNewFile()) {
			templateName = "file-added";
		} else if (file.testDeleted()) {
			templateName = "file-deleted";
		} else if (!file.getNewName().equals(file.getOldName())) {
			templateName = "file-renamed";
		}

		return templateName;
	}

	private String getHtmlId(DiffFile file) {
		return "d2h-" + StringUtils.substring(String.valueOf(getDiffName(file).hashCode()), -6);
	}

	private Mustache getTemplate(String base, String name) {
		List<String> pathParts = Lists.newArrayList();
		if (isNotBlank(base)) {
			pathParts.add(base);
		}
		if (isNotBlank(name)) {
			pathParts.add(name);
		}
		String path = join(pathParts, "-") + ".html";
		return getMustacheTemplate(path);
	}

	private Map<String, Object> getTemplateDataForFile(DiffFile file) throws Exception {
		Map<String, Object> templateData = file.getTemplateData();
		String fileTypeName = getFileTypeIcon(file);
		String icon = renderTemplate("icon", fileTypeName);
		templateData.put("addedLines", file.getAddedLines().get());
		templateData.put("deletedLines", file.getDeletedLines().get());
		templateData.put("diffParser", this);
		templateData.put("fileHtmlId", getHtmlId(file));
		templateData.put("fileIcon", icon);
		templateData.put("fileName", getDiffName(file));
		templateData.put("file", file);
		templateData.put("fileHtmlId", getHtmlId(file));
		templateData.put("fileTag", renderTemplate("tag", getFileTypeIcon(file)));

		templateData.put("filePath", renderTemplate("generic", "file-path", templateData));
		return templateData;
	}

	private Map<String, Object> getTemplateDataForLine(DiffFile file, DiffLine line, Map<String, Object> sharedTemplateData) {
		Map<String, Object> templateData = new HashMap<>();
		templateData.putAll(sharedTemplateData);

		String prefix = String.valueOf(line.getContent().charAt(0));
		String escapedLine = escapeHtml(line.getContent().substring(1, line.getContent().length()));

		templateData.put("content", escapedLine != null ? escapedLine.replaceAll(" ", "&nbsp;") : null);
		templateData.put("prefix", prefix != null ? prefix.replaceAll(" ", "&nbsp;") : null);
		templateData.put("type", line.getType().getValue());
		return templateData;
	}

	private void processChangeBlock(DiffFile file, StringBuilder left, StringBuilder right, List<DiffLine> oldLines, List<DiffLine> newLines) {
		List<List<List<DiffLine>>> matches;
		String insertType;
		String deleteType;

		int comparisons = oldLines.size() * newLines.size();
		int maxComparisons = 2500;
		boolean doMatching = (comparisons < maxComparisons) && (matching != null) && (matching.equals("lines") ||
			matching.equals("words"));

		List<List<DiffLine>> inner = Lists.newArrayList();
		inner.add(oldLines);
		inner.add(newLines);
		if (doMatching) {
			matches = Lists.newArrayList();
			matches.add(inner);
			insertType = DiffLineType.INSERT_CHANGES.getValue();
			deleteType = DiffLineType.DELETE_CHANGES.getValue();
		} else {
			matches = Lists.newArrayList();
			matches.add(inner);
			insertType = DiffLineType.INSERTS.getValue();
			deleteType = DiffLineType.DELETES.getValue();
		}

		for (List<List<DiffLine>> match : matches) {
			oldLines = match.get(0);
			newLines = match.get(1);

			int common = Math.min(oldLines.size(), newLines.size());
			int max = Math.max(oldLines.size(), newLines.size());

			for (int j = 0; j < common; j++) {
				DiffLine oldLine = oldLines.get(j);
				DiffLine newLine = newLines.get(j);

				combined = file.isCombined();

				DiffHighlight diff = diffHighlight(oldLine.getContent(), newLine.getContent());

				left.append(
					generateSingleLineHtml(
						deleteType,
						oldLine.getOldNumber(),
						diff.getFirst().getLine(),
						diff.getFirst().getPrefix()
					)
				);
				right.append(
					generateSingleLineHtml(
						insertType,
						newLine.getNewNumber(),
						diff.getSecond().getLine(),
						diff.getSecond().getPrefix()
					)
				);
			}

			if (max > common) {
				List<DiffLine> oldSlice = oldLines.subList(common, oldLines.size());
				List<DiffLine> newSlice = newLines.subList(common, newLines.size());

				DiffFileHtml tmpHtml = processLines(oldSlice, newSlice);
				left.append(tmpHtml.getLeft());
				right.append(tmpHtml.getRight());
			}
		}
		;

		oldLines.clear();
		newLines.clear();
	}

	private DiffFileHtml processLines(List<DiffLine> oldLines, List<DiffLine> newLines) {
		DiffFileHtml fileHtml = new DiffFileHtml();
		StringBuilder left = new StringBuilder();
		StringBuilder right = new StringBuilder();
		int maxLinesNumber = Math.max(oldLines.size(), newLines.size());
		for (int i = 0; i < maxLinesNumber; i++) {
			DiffLine oldLine = i < oldLines.size() ? oldLines.get(i) : null;
			DiffLine newLine = i < newLines.size() ? newLines.get(i) : null;
			String oldContent = null;
			String newContent = null;
			String oldPrefix = null;
			String newPrefix = null;

			if (oldLine != null) {
				oldContent = escapeHtml(oldLine.getContent().substring(1));
				oldPrefix = String.valueOf(oldLine.getContent().charAt(0));
			}

			if (newLine != null) {
				newContent = escapeHtml(newLine.getContent().substring(1));
				newPrefix = String.valueOf(newLine.getContent().charAt(0));
			}

			if ((oldLine != null) && (newLine != null)) {
				left.append(generateSingleLineHtml(oldLine.getType().getValue(), oldLine.getOldNumber(), oldContent, oldPrefix));
				right.append(generateSingleLineHtml(newLine.getType().getValue(), newLine.getNewNumber(), newContent, newPrefix));
			} else if (oldLine != null) {
				left.append(generateSingleLineHtml(oldLine.getType().getValue(), oldLine.getOldNumber(), oldContent, oldPrefix));
				right.append(getSingleLineContextHtml());
			} else if (newLine != null) {
				left.append(getSingleLineContextHtml());
				right.append(generateSingleLineHtml(newLine.getType().getValue(), newLine.getNewNumber(), newContent, newPrefix));
			} else {
				Logs.logError(LOG, new Exception(), "How did it get here?");
			}
		}
		fileHtml.setLeft(left.toString());
		fileHtml.setRight(right.toString());
		return fileHtml;
	}

	private String renderTemplate(String base, String name) {
		return getTemplate(base, name).execute(new StringWriter(), new HashMap<>()).toString();
	}

	private String renderTemplate(String base, String name, Map<String, Object> data) {
		return getTemplate(base, name).execute(new StringWriter(), data).toString();
	}

	@SafeVarargs
	private final String renderTemplate(String base, String name, final Pair<String, Object>... pairs) {
		return getTemplate(base, name).execute(new StringWriter(), addToMap(new HashMap<>(), pairs)).toString();
	}

	private void saveBlock() {
		if (currentBlock != null) {
			if (ignoreAdditions && (currentBlock.getDeletedLines().get() <= 0)) {
				return;
			}
			if (ignoreDeletions && (currentBlock.getAddedLines().get() <= 0)) {
				return;
			}
			currentFile.getBlocks().add(currentBlock);
			currentBlock = null;
		}
	}

	private void saveFile() {
		if ((currentFile != null) && (currentFile.getNewName() != null)) {
			diffFiles.add(currentFile);
			currentFile = null;
		}
	}

	private void startBlock(String line) {
		saveBlock();

		Matcher matcher = null;
		Integer oldLine2 = null;

		if ((matcher = NON_COMBINED_REGEX.matcher(line)).find()) {
			currentFile.setCombined(false);
			oldLine = new AtomicInteger(toInt(matcher.group(1)));
			newLine = new AtomicInteger(toInt(matcher.group(2)));
		} else if ((matcher = COMBINED_REGEX.matcher(line)).find()) {
			currentFile.setCombined(true);
			oldLine = new AtomicInteger(toInt(matcher.group(1)));
			oldLine2 = toInt(matcher.group(2));
			newLine = new AtomicInteger(toInt(matcher.group(3)));
		} else {
			Logs.logError(LOG, new Exception(), "Failed to parse lines, starting in 0!");
			oldLine = new AtomicInteger(0);
			newLine = new AtomicInteger(0);
			currentFile.setCombined(false);
		}
		currentBlock = new DiffBlock().setOldStartLine(oldLine.get()).setOldStartLine2(oldLine2).setNewStartLine(newLine.get()).setHeader(line);
	}

	private void startFile() {
		saveBlock();
		saveFile();

		currentFile = new DiffFile();
	}
}
