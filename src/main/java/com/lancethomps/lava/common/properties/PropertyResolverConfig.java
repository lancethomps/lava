package com.lancethomps.lava.common.properties;

import java.util.Map;

import com.lancethomps.lava.common.web.WebRequestContext;

public class PropertyResolverConfig {

  private WebRequestContext context;

  private Map<String, Object> customParams;

  private PropertyParserHelper helper;

  private boolean recursively = true;

  private String rootDir;

  public PropertyResolverConfig copy() {
    return copyWithNewRootDir(rootDir);
  }

  public PropertyResolverConfig copyWithNewRootDir(String rootDir) {
    return new PropertyResolverConfig().setContext(context).setHelper(helper).setRecursively(recursively).setRootDir(rootDir);
  }

  public WebRequestContext getContext() {
    return context;
  }

  public PropertyResolverConfig setContext(WebRequestContext context) {
    this.context = context;
    return this;
  }

  public Map<String, Object> getCustomParams() {
    return customParams;
  }

  public PropertyResolverConfig setCustomParams(Map<String, Object> customParams) {
    this.customParams = customParams;
    return this;
  }

  public PropertyParserHelper getHelper() {
    return helper;
  }

  public PropertyResolverConfig setHelper(PropertyParserHelper helper) {
    this.helper = helper;
    return this;
  }

  public String getRootDir() {
    return rootDir;
  }

  public PropertyResolverConfig setRootDir(String rootDir) {
    this.rootDir = rootDir;
    return this;
  }

  public boolean isRecursively() {
    return recursively;
  }

  public PropertyResolverConfig setRecursively(boolean recursively) {
    this.recursively = recursively;
    return this;
  }

}
