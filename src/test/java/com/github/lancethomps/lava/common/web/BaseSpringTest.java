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

@ActiveProfiles("unit_test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {
    "classpath:spring/spring-test.xml"
  }
)
@Ignore
public abstract class BaseSpringTest {

  @Rule
  public final ThreadNameTestWatcher threadNameWatcher = new ThreadNameTestWatcher();

  @BeforeClass
  public static void initSystemProperties() throws IOException {

  }

}
