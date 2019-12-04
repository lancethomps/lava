package com.lancethomps.lava.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BaseTestControllerWithDefaultHandler {

  @RequestMapping(value = "/*")
  public DataView handleDefault(HttpServletRequest request, HttpServletResponse response) throws Exception {
    DataView view = new DataView("test");
    return view;
  }

}
