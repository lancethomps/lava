package com.github.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class BaseTestControllerWithDefaultHandler.
 *
 * @author lathomps
 */
@Controller
public class BaseTestControllerWithDefaultHandler {

	/**
	 * Handle default.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the data view
	 * @throws Exception the exception
	 */
	@RequestMapping(value = "/*")
	public DataView handleDefault(HttpServletRequest request, HttpServletResponse response) throws Exception {
		DataView view = new DataView("test");
		return view;
	}

}
