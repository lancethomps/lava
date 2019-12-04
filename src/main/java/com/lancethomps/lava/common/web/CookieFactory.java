package com.lancethomps.lava.common.web;

import javax.servlet.http.Cookie;

public class CookieFactory {

  public static Cookie createCookie(String name) {
    return createCookie(name, name, -1);
  }

  public static Cookie createCookie(String name, int age) {
    return createCookie(name, name, age);
  }

  public static Cookie createCookie(String name, String value) {
    return createCookie(name, value, -1);
  }

  public static Cookie createCookie(String name, String value, int age) {
    return createCookie(name, value, age, 0);
  }

  public static Cookie createCookie(String name, String value, int age, int version) {
    Cookie cookie = new Cookie(name, value);
    cookie.setMaxAge(age);
    cookie.setVersion(version);
    cookie.setPath("/");

    try {

      cookie.setHttpOnly(true);
    } catch (NoSuchMethodError e) {
    }

    return cookie;
  }

}
