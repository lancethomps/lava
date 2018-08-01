package com.github.lancethomps.lava.common.web.throttle;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
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

import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.web.BaseSpringTest;
import com.google.common.collect.Lists;

/**
 * The Class RequestThrottleTest.
 *
 * @author lancethomps
 */
public class RequestThrottleTest extends BaseSpringTest {

	/** The Constant BLACK_LISTED_URI. */
	private static final String BLACK_LISTED_URI = "/throttle-black-listed";

	/** The Constant DEFAULT_URI. */
	private static final String DEFAULT_URI = "/throttle-test";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestThrottleTest.class);

	/** The Constant WHITE_LISTED_URI. */
	private static final String WHITE_LISTED_URI = "/throttle-white-listed";

	/** The handler adapter. */
	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;

	/** The handler mapping. */
	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	/** The request count for limit. */
	private Long requestCountForLimit;

	/** The throttle. */
	@Autowired
	private RequestThrottle throttle;

	/**
	 * Setup.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setup() throws Exception {
		Logs.logInfo(LOG, "throttle %s", throttle);
		setupWhiteList();
		requestCountForLimit = (long) throttle.getConfig().getDefaultMaxRequests() + 2;
	}

	/**
	 * Test request throttle black list.
	 *
	 * @throws Exception the exception
	 */
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

	/**
	 * Test request throttle limit is hit.
	 *
	 * @throws Exception the exception
	 */
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

	/**
	 * Test request throttle not in black list.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRequestThrottleNotInBlackList() throws Exception {
		removeWhiteList();
		setupBlackList();
		testMultipleRequests(requestCountForLimit, DEFAULT_URI);
		removeBlackList();
		setupWhiteList();
	}

	/**
	 * Test request throttle white list.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRequestThrottleWhiteList() throws Exception {
		testMultipleRequests(requestCountForLimit, WHITE_LISTED_URI);
	}

	/**
	 * Removes the black list.
	 */
	private void removeBlackList() {
		throttle.getConfig().setBlackList(null);
	}

	/**
	 * Removes the white list.
	 */
	private void removeWhiteList() {
		throttle.getConfig().setWhiteList(null);
	}

	/**
	 * Setup black list.
	 */
	private void setupBlackList() {
		throttle.getConfig().setBlackList(Lists.newArrayList(Pattern.compile('^' + BLACK_LISTED_URI + '$')));
	}

	/**
	 * Setup white list.
	 */
	private void setupWhiteList() {
		throttle.getConfig().setWhiteList(Lists.newArrayList(Pattern.compile('^' + WHITE_LISTED_URI + '$')));
	}

	/**
	 * Test multiple requests.
	 *
	 * @param count the count
	 * @param uri the uri
	 * @throws Exception the exception
	 */
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
