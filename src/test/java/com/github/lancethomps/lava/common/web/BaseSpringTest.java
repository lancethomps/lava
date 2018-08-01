package com.github.lancethomps.lava.common.web;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lancethomps.lava.common.ThreadNameTestWatcher;

/**
 * The Class BaseSpringWebTest.
 *
 * @author lancethomps
 */
@ActiveProfiles("unit_test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
	locations = {
		"classpath:spring/spring-test.xml"
	}
)
@Ignore
public abstract class BaseSpringTest {

	/** The thread name watcher. */
	@Rule
	public final ThreadNameTestWatcher threadNameWatcher = new ThreadNameTestWatcher();

	/**
	 * Inits the system properties.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@BeforeClass
	public static void initSystemProperties() throws IOException {

		// CONFIG REQUIRED EVERYWHERE FOR TESTING
	}
}
