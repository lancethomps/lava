package com.github.lancethomps.lava.common.web;

import javax.servlet.http.Cookie;

/**
 * A factory for creating Cookie objects.
 */
public class CookieFactory {

	/**
	 * Creates a new Cookie object.
	 *
	 * @param name the name
	 * @return the cookie
	 */
	public static Cookie createCookie(String name) {
		return createCookie(name, name, -1);
	}

	/**
	 * Creates a new Cookie object.
	 *
	 * @param name the name
	 * @param age the age
	 * @return the cookie
	 */
	public static Cookie createCookie(String name, int age) {
		return createCookie(name, name, age);
	}

	/**
	 * Creates a new Cookie object.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the cookie
	 */
	public static Cookie createCookie(String name, String value) {
		return createCookie(name, value, -1);
	}

	/**
	 * Creates a new Cookie object.
	 *
	 * @param name the name
	 * @param value the value
	 * @param age the age
	 * @return the cookie
	 */
	public static Cookie createCookie(String name, String value, int age) {
		return createCookie(name, value, age, 0);
	}

	/**
	 * Creates a new Cookie object.
	 *
	 * @param name the name
	 * @param value the value
	 * @param age the age
	 * @param version the version
	 * @return the cookie
	 */
	public static Cookie createCookie(String name, String value, int age, int version) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(age);
		cookie.setVersion(version);
		cookie.setPath("/");

		try {
			// NB: This is servlet spec 3.0, not supported under JBoss 4.2.3 (Teamsite)
			cookie.setHttpOnly(true);
		} catch (NoSuchMethodError e) {
			;
		}

		return cookie;
	}

}
