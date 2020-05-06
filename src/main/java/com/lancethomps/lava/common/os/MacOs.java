package com.lancethomps.lava.common.os;

import static com.google.common.base.Preconditions.checkArgument;
import static com.lancethomps.lava.common.os.OsUtil.isMac;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.lancethomps.lava.common.logging.Logs;

public class MacOs {

  private static final Logger LOG = LogManager.getLogger(MacOs.class);

  private static final String PASSWORD_GUI_PROMPT_MESSAGE = "Please enter your internet password for your account '%s' on the domain '%s'.\n\n" +
    "It will then be added to your keychain for future use (and this prompt will be skipped). You may delete or edit it later through the Keychain " +
    "Access application.";

  private static final String PASSWORD_GUI_PROMPT_OSASCRIPT = "tell application \"System Events\"" + System.lineSeparator() +
    "activate" + System.lineSeparator() +
    "text returned of (display dialog \"%s\" default answer \"\" with hidden answer)" + System.lineSeparator() +
    "end tell";

  private static final String GUI_PROMPT_OSASCRIPT = "tell application \"System Events\"" + System.lineSeparator() +
      "activate" + System.lineSeparator() +
      "text returned of (display dialog \"%s\" default answer \"\")" + System.lineSeparator() +
      "end tell";

  public static Pair<String, ProcessResult> addInternetPassword(String domain, String user) {
    String pass = getPasswordFromGui(getPasswordGuiPromptMessage(user, domain), 3);
    ProcessResult addResult = null;
    if (isNotBlank(pass)) {
      addResult = Processes.run("security", "add-internet-password", "-a", user, "-s", domain, "-t", "dflt", "-w", pass);
      if (!addResult.wasSuccessful()) {
        Logs.logWarn(
          LOG,
          "Adding internet password to macOS keychain for user [%s] on domain [%s] was not successful. Result: %s",
          user,
          domain,
          addResult
        );
      } else {
        Logs.logInfo(LOG, "Added internet password to macOS keychain for user [%s] on domain [%s].", user, domain);
      }
    }
    return Pair.of(pass, addResult);
  }

  public static List<String> getGuiPromptCommandWithHiddenResponse(@Nonnull String message) {
    return Arrays.asList("osascript", "-e", String.format(PASSWORD_GUI_PROMPT_OSASCRIPT, StringUtils.replace(message, "\"", "\\\"")));
  }

  public static List<String> getGuiPromptCommand(@Nonnull String message) {
    return Arrays.asList("osascript", "-e", String.format(GUI_PROMPT_OSASCRIPT, StringUtils.replace(message, "\"", "\\\"")));
  }

  public static ProcessResult getGuiPromptResultWithHiddenResponse(@Nonnull String message) {
    return Processes.run(getGuiPromptCommandWithHiddenResponse(message));
  }

  public static ProcessResult getGuiPromptResult(@Nonnull String message) {
    return Processes.run(getGuiPromptCommand(message));
  }

  public static String getKeychainInternetPassword(@Nonnull String domain) {
    return getKeychainInternetPassword(domain, System.getProperty("user.name"));
  }

  public static String getKeychainInternetPassword(@Nonnull String domain, @Nonnull String user) {
    return getKeychainInternetPassword(domain, user, true);
  }

  public static String getKeychainInternetPassword(@Nonnull String domain, @Nonnull String user, boolean askAndAddIfMissing) {
    if (!isMac()) {
      throw new RuntimeException("This method can only be called on macOS");
    }
    String pass = null;
    try {
      List<String> command = Lists.newArrayList("security", "find-internet-password", "-a", user, "-ws", domain, "-t", "dflt");
      pass = StringUtils.trim(OsUtil.runAndGetOutput(command, false));
      if (isBlank(pass)) {
        List<String> nonDefaultCommand = command.subList(0, command.size() - 2);
        pass = StringUtils.trim(OsUtil.runAndGetOutput(nonDefaultCommand, false));
      }
      if (isBlank(pass) && askAndAddIfMissing) {
        pass = addInternetPassword(domain, user).getLeft();
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting macOS Keychain password for [%s]", domain);
    }
    return pass;
  }

  public static String getKeychainGenericPassword(@Nonnull String id) {
    if (!isMac()) {
      throw new RuntimeException("This method can only be called on macOS");
    }
    String pass = null;
    try {
      List<String> command = Lists.newArrayList("security", "find-generic-password", "-ws", id);
      pass = StringUtils.trim(OsUtil.runAndGetOutput(command, false));
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue getting macOS Keychain password for [%s]", id);
    }
    return pass;
  }

  public static String askForInput(String message) {
    ProcessResult result = getGuiPromptResult(message);
    if (!result.wasSuccessful()) {
      return null;
    }
    return StringUtils.trim(result.getOutput());
  }

  public static String askForInput(String message, Function<String, Pair<Boolean, String>> validator, int maxTries) {
    checkArgument(maxTries > 0, "maxTries must be greater than 0");
    int tryCount = 0;
    String suffix = "";
    while (tryCount < maxTries) {
      ProcessResult result = getGuiPromptResult(message + suffix);
      if (!result.wasSuccessful()) {
        return null;
      }
      String response = StringUtils.trim(result.getOutput());
      Pair<Boolean, String> valid = validator.apply(response);
      if (valid.getLeft()) {
        return response;
      } else if (valid.getRight() != null) {
        suffix = "\n\n" + valid.getRight();
      }
      tryCount++;
    }
    Logs.logInfo(LOG, "User input on GUI max tries reached (%s)", maxTries);
    return null;
  }

  public static String getPasswordFromGui(String message, int maxTries) {
    checkArgument(maxTries > 0, "maxTries must be greater than 0");
    int tryCount = 0;
    while (tryCount < maxTries) {
      ProcessResult result1 = getGuiPromptResultWithHiddenResponse(message + (tryCount > 0 ? "\n\nYour passwords did not match. Please try again." : ""));
      String pass1 = StringUtils.trim(result1.getOutput());
      if (!result1.wasSuccessful() || isBlank(pass1)) {
        Logs.logInfo(LOG, "Password input on GUI canceled.");
        return null;
      }
      ProcessResult result2 = getGuiPromptResultWithHiddenResponse(message + "\n\nConfirm password.");
      String pass2 = StringUtils.trim(result2.getOutput());
      if (!result2.wasSuccessful() || isBlank(pass2)) {
        Logs.logInfo(LOG, "Password input on GUI canceled.");
        return null;
      }
      if (pass1.equals(pass2)) {
        return pass1;
      }
      Logs.logInfo(LOG, "Password inputs on GUI did not match. Try %s of %s", tryCount, maxTries);
      tryCount++;
    }
    Logs.logInfo(LOG, "Password inputs on GUI max tries reached (%s)", maxTries);
    return null;
  }

  public static String getPasswordGuiPromptMessage(String user, String domain) {
    return String.format(PASSWORD_GUI_PROMPT_MESSAGE, user, domain);
  }

}
