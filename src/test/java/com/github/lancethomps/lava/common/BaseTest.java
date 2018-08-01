package com.github.lancethomps.lava.common;

import java.io.File;

import org.junit.Rule;

import com.github.lancethomps.lava.common.ser.jackson.CustomDeserializationProblemHandler;
import com.google.common.collect.Sets;

/**
 * The Class BaseTest.
 *
 * @author lancethomps
 */
public class BaseTest {

	/** The thread name watcher. */
	@Rule
	public final ThreadNameTestWatcher threadNameWatcher = new ThreadNameTestWatcher();

	static {
		if (System.getProperty("PROJ_DIR") != null) {
			Testing.setProjRootPath(System.getProperty("PROJ_DIR") + File.separatorChar);
		}
		if (Checks.isEmpty(CustomDeserializationProblemHandler.getIgnoreProperties())) {
			CustomDeserializationProblemHandler.setIgnoreProperties(Sets.newHashSet("@type"));
		}
	}

}
