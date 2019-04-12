package com.github.lancethomps.lava.common.ser;

import java.util.Set;

import com.github.lancethomps.lava.common.web.requests.parsers.RequestField;

public class CsvParams extends ExternalizableBean {

  @RequestField
  private Set<String> alwaysIncludeHeaders;

  @RequestField
  private Boolean skipMapConversion;

  public Set<String> getAlwaysIncludeHeaders() {
    return alwaysIncludeHeaders;
  }

  public CsvParams setAlwaysIncludeHeaders(Set<String> alwaysIncludeHeaders) {
    this.alwaysIncludeHeaders = alwaysIncludeHeaders;
    return this;
  }

  public Boolean getSkipMapConversion() {
    return skipMapConversion;
  }

  public CsvParams setSkipMapConversion(Boolean skipMapConversion) {
    this.skipMapConversion = skipMapConversion;
    return this;
  }

}
