package com.lancethomps.lava.common.web.throttle;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.collect.Lists;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.web.BaseSpringTest;

public class RequestThrottleTest extends BaseSpringTest {

  private static final String BLACK_LISTED_URI = "/throttle-black-listed";

  private static final String DEFAULT_URI = "/throttle-test";

  private static final Logger LOG = LogManager.getLogger(RequestThrottleTest.class);

  private static final String WHITE_LISTED_URI = "/throttle-white-listed";

  @Autowired
  private RequestMappingHandlerAdapter handlerAdapter;

  @Autowired
  private RequestMappingHandlerMapping handlerMapping;

  private Long requestCountForLimit;

  @Autowired
  private RequestThrottle throttle;

  @Before
  public void setup() throws Exception {
    Logs.logInfo(LOG, "throttle %s", throttle);
    setupWhiteList();
    requestCountForLimit = (long) throttle.getConfig().getDefaultMaxRequests() + 2;
  }

  @Test
  public void testRequestThrottleBlackList() throws Exception {
    setupBlackList();
    RequestThrottleException err = null;
    try {
      testMultipleRequests(requestCountForLimit, BLACK_LISTED_URI);
    } catch (RequestThrottleException e) {
      err = e;
    } finally {
      removeBlackList();
    }

    Assert.assertNotNull(err);
  }

  @Test
  public void testRequestThrottleLimitIsHit() throws Exception {
    RequestThrottleException err = null;
    try {
      testMultipleRequests(requestCountForLimit, DEFAULT_URI);
    } catch (RequestThrottleException e) {
      err = e;
    }

    Assert.assertNotNull(err);
  }

  @Test
  public void testRequestThrottleNotInBlackList() throws Exception {
    removeWhiteList();
    setupBlackList();
    testMultipleRequests(requestCountForLimit, DEFAULT_URI);
    removeBlackList();
    setupWhiteList();
  }

  @Test
  public void testRequestThrottleWhiteList() throws Exception {
    testMultipleRequests(requestCountForLimit, WHITE_LISTED_URI);
  }

  private void removeBlackList() {
    throttle.getConfig().setBlackList(null);
  }

  private void removeWhiteList() {
    throttle.getConfig().setWhiteList(null);
  }

  private void setupBlackList() {
    throttle.getConfig().setBlackList(Lists.newArrayList(Pattern.compile('^' + BLACK_LISTED_URI + '$')));
  }

  private void setupWhiteList() {
    throttle.getConfig().setWhiteList(Lists.newArrayList(Pattern.compile('^' + WHITE_LISTED_URI + '$')));
  }

  private void testMultipleRequests(long count, String uri) throws Exception {
    List<Pair<MockHttpServletRequest, MockHttpServletResponse>> mockReqs = Stream
      .iterate(0, pos -> pos + 1)
      .limit(count)
      .map(pos -> Pair.of(new MockHttpServletRequest(HttpMethod.GET.name(), uri), new MockHttpServletResponse()))
      .collect(Collectors.toList());
    for (Pair<MockHttpServletRequest, MockHttpServletResponse> mockReq : mockReqs) {
      MockHttpServletRequest request = mockReq.getLeft();
      MockHttpServletResponse response = mockReq.getRight();

      HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
      HandlerInterceptor[] interceptors = handlerExecutionChain.getInterceptors();
      for (HandlerInterceptor interceptor : interceptors) {
        interceptor.preHandle(request, response, handlerExecutionChain.getHandler());
      }
    }
    for (Pair<MockHttpServletRequest, MockHttpServletResponse> mockReq : mockReqs) {
      MockHttpServletRequest request = mockReq.getLeft();
      MockHttpServletResponse response = mockReq.getRight();

      HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
      ModelAndView mav = handlerAdapter.handle(request, response, handlerExecutionChain.getHandler());
      HandlerInterceptor[] interceptors = handlerExecutionChain.getInterceptors();
      for (HandlerInterceptor interceptor : interceptors) {
        interceptor.postHandle(request, response, handlerExecutionChain.getHandler(), mav);
      }
    }
  }

}
