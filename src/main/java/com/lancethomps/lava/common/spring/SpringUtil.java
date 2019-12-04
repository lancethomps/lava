package com.lancethomps.lava.common.spring;

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

import com.lancethomps.lava.common.expr.ExprFactory;
import com.lancethomps.lava.common.logging.Logs;
import com.lancethomps.lava.common.web.ResponseWrapper;
import com.lancethomps.lava.common.web.requests.RequestWrapper;
import com.google.common.collect.Maps;

public final class SpringUtil {

  public static final String API_CLIENT_KEY = "_zzApiClient";

  public static final String INTERNAL_USER_KEY = "_zzInternalUser";

  public static final String USER_ID_KEY = "_zzUserId";

  private static final Logger LOG = Logger.getLogger(SpringUtil.class);

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

  public static HttpServletRequest getCurrentHttpServletRequest() {
    RequestAttributes att = RequestContextHolder.getRequestAttributes();
    if ((att != null) && (att instanceof ServletRequestAttributes)) {
      return ((ServletRequestAttributes) att).getRequest();
    }
    return null;
  }

  public static WebApplicationContext getCurrentWebApplicationContext() {
    return ContextLoader.getCurrentWebApplicationContext();
  }

  public static HttpServletRequest getHttpRequest() {
    RequestAttributes att = RequestContextHolder.getRequestAttributes();
    if ((att != null) && (att instanceof ServletRequestAttributes)) {
      HttpServletRequest request = ((ServletRequestAttributes) att).getRequest();
      return request;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getRequestAttribute(String name) {
    RequestAttributes att = RequestContextHolder.getRequestAttributes();
    if (att == null) {
      return null;
    }
    return (T) att.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

  public static RequestWrapper getRequestWrapper(ServletRequest request) {
    if (request instanceof RequestWrapper) {
      return (RequestWrapper) request;
    } else if (request instanceof ServletRequestWrapper) {
      return getRequestWrapper(((ServletRequestWrapper) request).getRequest());
    }
    return null;
  }

  public static ResponseWrapper getResponseWrapper(ServletResponse response) {
    if (response instanceof ResponseWrapper) {
      return (ResponseWrapper) response;
    } else if (response instanceof ServletResponseWrapper) {
      return getResponseWrapper(((ServletResponseWrapper) response).getResponse());
    }
    return null;
  }

  public static HttpSession getSession() {
    HttpServletRequest request = getCurrentHttpServletRequest();
    if (request != null) {
      return request.getSession(false);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getSessionAttribute(String name) {
    RequestAttributes att = RequestContextHolder.getRequestAttributes();
    if (att == null) {
      return null;
    }
    return (T) att.getAttribute(name, RequestAttributes.SCOPE_SESSION);
  }

  public static String getSessionId() {
    return getSessionId(false);
  }

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

  public static String getSpringApiClient() {
    return getSessionAttribute(API_CLIENT_KEY);
  }

  public static Boolean getSpringFromInternalUser() {
    return getSessionAttribute(INTERNAL_USER_KEY);
  }

  public static String getSpringUserId() {
    return getSessionAttribute(USER_ID_KEY);
  }

  public static boolean hasRequestAttributes() {
    return RequestContextHolder.getRequestAttributes() != null;
  }

  public static boolean hasSession() {
    HttpSession session = getSession();
    return (session != null) && (session.getId() != null);
  }

  public static <T> T processInjection(T target, ApplicationContext context) {
    if (context != null) {
      AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
      beanFactory.autowireBean(target);
    }
    return target;
  }

  public static <T> T processInjectionBasedOnCurrentContext(T target) {
    return processInjection(target, ContextLoader.getCurrentWebApplicationContext());
  }

}
