package com.lancethomps.lava.common.os;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.lambda.Lambdas;
import com.lancethomps.lava.common.logging.Logs;

public class OsUtil {

  private static final List<String> GIT_COMMIT_COMMAND = Collections.unmodifiableList(Arrays.asList("git", "rev-parse", "HEAD"));

  private static final Logger LOG = LogManager.getLogger(OsUtil.class);

  private static final String WINDOWS_PROGRAM_FILES = "C:\\Program Files (x86)";

  public static boolean doesBinaryExist(final String binary) {
    try {
      return Lambdas.functionIfTrue(
        runAndGetOutput(asList("which", binary), false),
        StringUtils::isNotBlank,
        binaryPath -> new File(trim(binaryPath)).isFile(),
        bp -> false
      ).orElse(false);
    } catch (IOException e) {
      Logs.logError(LOG, e, "Error checking if binary [%s] exists", binary);
      return false;
    }
  }

  public static String getGitHeadCommit(final File gitRepo) {
    return getGitHeadCommit(gitRepo, true);
  }

  public static String getGitHeadCommit(final File gitRepo, final boolean tryGitBinary) {
    if (gitRepo.isDirectory()) {
      try {
        String commit = null;
        File headFile = new File(gitRepo, ".git/HEAD");
        while (headFile.isFile()) {
          commit = StringUtils.trim(FileUtil.readFile(headFile));
          if (contains(commit, "ref: ")) {
            headFile = new File(gitRepo, ".git/" + StringUtils.removeStart(commit, "ref: "));
          } else {
            break;
          }
        }
        if (isBlank(commit) && tryGitBinary && doesBinaryExist("git")) {
          commit = OsUtil.runAndGetOutput(GIT_COMMIT_COMMAND, false, gitRepo);
        }
        return commit;
      } catch (IOException e) {
        Logs.logError(LOG, e, "Issue checking git commit for directory [%s] and command [%s]", gitRepo, StringUtils.join(GIT_COMMIT_COMMAND, ' '));
      }
    }
    return null;
  }

  public static String getMacOsKeychainPassword(@Nonnull String domain) {
    return getMacOsKeychainPassword(domain, System.getProperty("user.name"));
  }

  public static String getMacOsKeychainPassword(@Nonnull String domain, @Nonnull String user) {
    return MacOs.getKeychainInternetPassword(domain, user, true);
  }

  public static String getProcessOutput(Process process) {
    if (process != null) {
      try (InputStream is = process.getInputStream()) {
        return IOUtils.toString(new InputStreamReader(is));
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Issue getting output for process [%s]", process);
      } finally {
        if (process.isAlive()) {
          process.destroyForcibly();
        }
      }
    }
    return null;
  }

  public static String getProcessOutputFromFile(Process process, File file) {
    if ((process != null) && (file != null)) {
      try {
        process.waitFor(5, TimeUnit.MINUTES);
        if (file.isFile()) {
          return FileUtil.readFile(file);
        }
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Issue getting output for process [%s]", process);
      } finally {
        if (process.isAlive()) {
          process.destroyForcibly();
        }
      }
    }
    return null;
  }

  public static String getQuotedCommand(List<String> commands) {
    List<String> quoted = commands.stream().map(arg -> {
      if (arg.startsWith("-")) {
        return arg;
      }
      return '\'' + StringUtils.replace(arg, "'", "'\"'\"'") + '\'';
    }).collect(Collectors.toList());
    quoted.remove(0);
    quoted.add(0, commands.get(0));
    return StringUtils.join(quoted, " ");
  }

  public static String getSharesRoot() {
    return !isMac() ? null : new File("/private/shares").isDirectory() ? "/private/shares" : "/Volumes";
  }

  public static boolean isLocal() {
    return isMac() || isWindows();
  }

  public static boolean isMac() {
    return StringUtils.contains(StringUtils.lowerCase(System.getProperty("os.name")), "mac");
  }

  public static boolean isWindows() {
    return StringUtils.contains(StringUtils.lowerCase(System.getProperty("os.name")), "win");
  }

  public static void launchWindowsDefaultBrowser(String targetUrl) {
    try {
      Process process = new ProcessBuilder("cmd", "/c start iexplore " + targetUrl).redirectErrorStream(true).start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        Logs.logInfo(LOG, IOUtils.toString(reader));
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue launching Windows default browser for URL [%s]", targetUrl);
    }
  }

  public static void openFile(String filePath) {
    try {
      Process process = new ProcessBuilder("open", filePath).redirectErrorStream(true).start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        Logs.logInfo(LOG, IOUtils.toString(reader));
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue opening file [%s]", filePath);
    }
  }

  public static void openUrl(String targetUrl) {
    if (OsUtil.isWindows()) {
      if (!searchDefaultPath(targetUrl)) {
        Logs.logInfo(LOG, "Failed to find executable in the default installation path");
        launchWindowsDefaultBrowser(targetUrl);
      }
    } else if (OsUtil.isMac()) {
      openFile(targetUrl);
    } else {
      Logs.logError(LOG, new IllegalArgumentException(), "Cannot open URL [%s] on non-local machine!", targetUrl);
    }
  }

  public static Process run(List<String> commands) throws IOException {
    return run(commands, false);
  }

  public static Process run(List<String> commands, boolean redirectError) throws IOException {
    return run(commands, redirectError, null);
  }

  public static Process run(List<String> commands, boolean redirectError, File baseDir) throws IOException {
    if (LOG.isDebugEnabled()) {
      Logs.logDebug(LOG, "Running [%s]...", getQuotedCommand(commands));
    }
    return new ProcessBuilder(commands).directory(baseDir).redirectErrorStream(redirectError).start();
  }

  public static Process run(String... args) throws IOException {
    List<String> commands = Arrays.asList(args);
    return run(commands, false);
  }

  public static Pair<Integer, String> runAndGetFileOutput(@Nonnull List<String> commands, long timeoutSeconds) {
    Integer exitValue = null;
    String out = null;
    Process process = null;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(commands);
      File outFile = File.createTempFile("ts-out", ".log");
      processBuilder.redirectErrorStream(true);
      processBuilder.redirectOutput(outFile);

      process = processBuilder.start();
      if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        exitValue = process.exitValue();
      }
      if (outFile.isFile()) {
        out = FileUtil.readFile(outFile);
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting output for process [%s]", process);
    }
    return Pair.of(exitValue, out);
  }

  public static String runAndGetOutput(List<String> commands, boolean redirectError) throws IOException {
    return runAndGetOutput(commands, redirectError, null);
  }

  public static String runAndGetOutput(List<String> commands, boolean redirectError, File baseDir) throws IOException {
    return getProcessOutput(run(commands, redirectError, baseDir));
  }

  private static boolean searchDefaultPath(String targetUrl) {
    List<String> matchedPaths = new ArrayList<>();
    File folder = new File(WINDOWS_PROGRAM_FILES);
    FilenameFilter chromeDirfilter = new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().indexOf("chrome") > 0;
      }
    };
    FilenameFilter chromeExefilter = new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return name.equals("chrome.exe");
      }
    };
    String[] files = folder.list(chromeDirfilter);
    if (files != null) {
      for (String fileName : files) {
        File file = new File(WINDOWS_PROGRAM_FILES + "\\" + fileName);
        if (file.isDirectory()) {
          String[] matchedNames = file.list(chromeExefilter);
          if (matchedNames != null) {
            for (String name : matchedNames) {
              matchedPaths.add(file.getPath() + "\\" + name);
            }
          }
        }
      }
    }
    if (matchedPaths.size() > 0) {
      try {
        return run(matchedPaths.get(0), targetUrl) != null;
      } catch (Throwable e) {
        Logs.logError(LOG, e, "Error opening URL with command [%s]", matchedPaths.get(0));
        return false;
      }
    }
    return false;

  }

}
