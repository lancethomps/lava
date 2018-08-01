package com.github.lancethomps.lava.common.os;

import static com.github.lancethomps.lava.common.os.OsUtil.isMac;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;
import com.google.common.collect.Lists;

/**
 * The Class MacOs.
 *
 * @author lancethomps
 */
public class MacOs {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(MacOs.class);

	/** The Constant PASSWORD_GUI_PROMPT_MESSAGE. */
	private static final String PASSWORD_GUI_PROMPT_MESSAGE = "Please enter your internet password for your account '%s' on the domain '%s'.\n\n" +
		"It will then be added to your keychain for future use (and this prompt will be skipped). You may delete or edit it later through the Keychain Access application.";

	/** The Constant GUI_PROMPT_OSASCRIPT. */
	private static final String PASSWORD_GUI_PROMPT_OSASCRIPT = "tell application \"System Events\"" + System.lineSeparator() +
		"activate" + System.lineSeparator() +
		"text returned of (display dialog \"%s\" default answer \"\" with hidden answer)" + System.lineSeparator() +
		"end tell";

	/**
	 * Adds the internet password.
	 *
	 * @param domain the domain
	 * @param user the user
	 * @return the pair
	 */
	public static Pair<String, ProcessResult> addInternetPassword(String domain, String user) {
		String pass = getPasswordFromGui(getPasswordGuiPromptMessage(user, domain), 3);
		ProcessResult addResult = null;
		if (isNotBlank(pass)) {
			addResult = Processes.run("security", "add-internet-password", "-a", user, "-s", domain, "-t", "dflt", "-w", pass);
			if (!addResult.wasSuccessful()) {
				Logs.logWarn(LOG, "Adding internet password to macOS keychain for user [%s] on domain [%s] was not successful. Result: %s", user, domain, addResult);
			} else {
				Logs.logInfo(LOG, "Added internet password to macOS keychain for user [%s] on domain [%s].", user, domain);
			}
		}
		return Pair.of(pass, addResult);
	}

	/**
	 * Gets the mac os gui prompt command.
	 *
	 * @param message the message
	 * @return the mac os gui prompt command
	 */
	public static List<String> getGuiPromptCommand(@Nonnull String message) {
		return Arrays.asList("osascript", "-e", String.format(PASSWORD_GUI_PROMPT_OSASCRIPT, StringUtils.replace(message, "\"", "\\\"")));
	}

	/**
	 * Gets the mac os gui prompt result.
	 *
	 * @param message the message
	 * @return the mac os gui prompt result
	 */
	public static ProcessResult getGuiPromptResult(@Nonnull String message) {
		return Processes.run(getGuiPromptCommand(message));
	}

	/**
	 * Gets the mac os keychain password.
	 *
	 * @param domain the domain
	 * @param user the user
	 * @param askAndAddIfMissing the ask if missing
	 * @return the mac os keychain password
	 */
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

	/**
	 * Gets the password from gui.
	 *
	 * @param message the message
	 * @param maxTries the max tries
	 * @return the password from gui
	 */
	public static String getPasswordFromGui(String message, int maxTries) {
		assert maxTries > 0;
		int tryCount = 0;
		while (tryCount < maxTries) {
			ProcessResult result1 = getGuiPromptResult(message + (tryCount > 0 ? "\n\nYour passwords did not match. Please try again." : ""));
			String pass1 = StringUtils.trim(result1.getOutput());
			if (!result1.wasSuccessful() || isBlank(pass1)) {
				Logs.logInfo(LOG, "Password input on GUI canceled.");
				return null;
			}
			ProcessResult result2 = getGuiPromptResult(message + "\n\nConfirm password.");
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

	/**
	 * Gets the mac os password gui prompt message.
	 *
	 * @param user the user
	 * @param domain the domain
	 * @return the mac os password gui prompt message
	 */
	public static String getPasswordGuiPromptMessage(String user, String domain) {
		return String.format(PASSWORD_GUI_PROMPT_MESSAGE, user, domain);
	}

}
