package com.github.lancethomps.lava.common.spring;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.lancethomps.lava.common.expr.ExprFactory;
import com.github.lancethomps.lava.common.logging.Logs;
import com.github.lancethomps.lava.common.web.ResponseWrapper;
import com.github.lancethomps.lava.common.web.requests.RequestWrapper;
import com.google.common.collect.Maps;

/**
 * The Class SpringUtil.
 */
public final class SpringUtil {

	/** The Constant API_CLIENT_KEY. */
	public static final String API_CLIENT_KEY = "_zzApiClient";

	/** The Constant INTERNAL_USER_KEY. */
	public static final String INTERNAL_USER_KEY = "_zzInternalUser";

	/** The Constant USER_NAME_KEY. */
	public static final String USER_ID_KEY = "_zzUserId";

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(SpringUtil.class);

	/**
	 * Exec spring method reflection.
	 *
	 * @param springContext the spring context
	 * @param expr the expr
	 * @param args the args
	 * @return the object
	 */
	public static Object execSpringMethodReflection(ApplicationContext springContext, String expr, Object... args) {
		Object result = null;
		try {
			String beanName = StringUtils.substringBefore(expr, ".");
			String methodName = StringUtils.substringAfter(expr, ".");
			if (springContext == null) {
				springContext = ContextLoader.getCurrentWebApplicationContext();
			}
			Object bean = springContext.getBean(beanName);
			if (bean != null) {
				Pair<Boolean, String> methodExpr = ExprFactory.isSpelString(methodName);
				if (methodExpr.getLeft()) {
					Map<String, Object> parent = Maps.newHashMap();
					parent.put("bean", bean);
					parent.put("args", args);
					result = ExprFactory.getValueFromPath(parent, methodExpr.getRight(), false);
				} else {
					Method method = BeanUtils.findMethodWithMinimalParameters(bean.getClass(), methodName);
					if (method != null) {
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						result = method.invoke(bean, args);
					}
				}
			}
		} catch (Throwable e) {
			Logs.logError(LOG, e, "Issue executing Spring method reflection for [%s] with args [%s]", expr, args);
		}
		return result;
	}

	/**
	 * Gets the current http servlet request.
	 *
	 * @return the current http servlet request
	 */
	public static HttpServletRequest getCurrentHttpServletRequest() {
		RequestAttributes att = RequestContextHolder.getRequestAttributes();
		if ((att != null) && (att instanceof ServletRequestAttributes)) {
			return ((ServletRequestAttributes) att).getRequest();
		}
		return null;
	}

	/**
	 * Gets the current web application context.
	 *
	 * @return the current web application context
	 */
	public static WebApplicationContext getCurrentWebApplicationContext() {
		return ContextLoader.getCurrentWebApplicationContext();
	}

	/**
	 * Gets the http request.
	 *
	 * @return the http request
	 */
	public static HttpServletRequest getHttpRequest() {
		RequestAttributes att = RequestContextHolder.getRequestAttributes();
		if ((att != null) && (att instanceof ServletRequestAttributes)) {
			HttpServletRequest request = ((ServletRequestAttributes) att).getRequest();
			return request;
		}
		return null;
	}

	/**
	 * Gets the request attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the request attribute
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRequestAttribute(String name) {
		RequestAttributes att = RequestContextHolder.getRequestAttributes();
		if (att == null) {
			return null;
		}
		return (T) att.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
	}

	/**
	 * Gets the request wrapper.
	 *
	 * @param request the request
	 * @return the request wrapper
	 */
	public static RequestWrapper getRequestWrapper(ServletRequest request) {
		if (request instanceof RequestWrapper) {
			return (RequestWrapper) request;
		} else if (request instanceof ServletRequestWrapper) {
			return getRequestWrapper(((ServletRequestWrapper) request).getRequest());
		}
		return null;
	}

	/**
	 * Gets the response wrapper.
	 *
	 * @param response the response
	 * @return the response wrapper
	 */
	public static ResponseWrapper getResponseWrapper(ServletResponse response) {
		if (response instanceof ResponseWrapper) {
			return (ResponseWrapper) response;
		} else if (response instanceof ServletResponseWrapper) {
			return getResponseWrapper(((ServletResponseWrapper) response).getResponse());
		}
		return null;
	}

	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	public static HttpSession getSession() {
		HttpServletRequest request = getCurrentHttpServletRequest();
		if (request != null) {
			return request.getSession(false);
		}
		return null;
	}

	/**
	 * Gets the session attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the session attribute
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSessionAttribute(String name) {
		RequestAttributes att = RequestContextHolder.getRequestAttributes();
		if (att == null) {
			return null;
		}
		return (T) att.getAttribute(name, RequestAttributes.SCOPE_SESSION);
	}

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public static String getSessionId() {
		return getSessionId(false);
	}

	/**
	 * Gets the session id.
	 *
	 * @param allowCreate the allow create
	 * @return the session id
	 */
	public static String getSessionId(boolean allowCreate) {
		HttpSession session = getSession();
		if (session != null) {
			return session.getId();
		}
		RequestAttributes att = RequestContextHolder.getRequestAttributes();
		if (allowCreate && (att != null)) {
			return att.getSessionId();
		}
		return null;
	}

	/**
	 * Gets the request api client.
	 *
	 * @return the request api client
	 */
	public static String getSpringApiClient() {
		return getSessionAttribute(API_CLIENT_KEY);
	}

	/**
	 * Gets the request from internal user.
	 *
	 * @return the request from internal user
	 */
	public static Boolean getSpringFromInternalUser() {
		return getSessionAttribute(INTERNAL_USER_KEY);
	}

	/**
	 * Gets the request user id.
	 *
	 * @return the request user id
	 */
	public static String getSpringUserId() {
		return getSessionAttribute(USER_ID_KEY);
	}

	/**
	 * Checks for request attributes.
	 *
	 * @return true, if successful
	 */
	public static boolean hasRequestAttributes() {
		return RequestContextHolder.getRequestAttributes() != null;
	}

	/**
	 * Checks for session.
	 *
	 * @return true, if successful
	 */
	public static boolean hasSession() {
		HttpSession session = getSession();
		return (session != null) && (session.getId() != null);
	}

	/**
	 * Process injection.
	 *
	 * @param <T> the generic type
	 * @param target the target
	 * @param context the context
	 * @return the t
	 */
	public static <T> T processInjection(T target, ApplicationContext context) {
		if (context != null) {
			AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
			beanFactory.autowireBean(target);
		}
		return target;
	}

	/**
	 * Process autowiring.
	 *
	 * @param <T> the generic type
	 * @param target the target
	 * @return the t
	 */
	public static <T> T processInjectionBasedOnCurrentContext(T target) {
		return processInjection(target, ContextLoader.getCurrentWebApplicationContext());
	}
}
