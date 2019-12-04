package com.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Map;

public class RequestParserInfo {

  private List<RequestFieldInfo<?>> complexRequestInfos;

  private List<RequestFieldInfo<?>> infos;

  private Map<String, RequestFieldInfo<?>> infosMap;

  private RequestFieldInfo<?> requestBeanInfo;

  public List<RequestFieldInfo<?>> getComplexRequestInfos() {
    return complexRequestInfos;
  }

  public void setComplexRequestInfos(List<RequestFieldInfo<?>> complexRequestInfos) {
    this.complexRequestInfos = complexRequestInfos;
  }

  public List<RequestFieldInfo<?>> getInfos() {
    return infos;
  }

  public void setInfos(List<RequestFieldInfo<?>> infos) {
    this.infos = infos;
  }

  public Map<String, RequestFieldInfo<?>> getInfosMap() {
    return infosMap;
  }

  public void setInfosMap(Map<String, RequestFieldInfo<?>> infosMap) {
    this.infosMap = infosMap;
  }

  public RequestFieldInfo<?> getRequestBeanInfo() {
    return requestBeanInfo;
  }

  public void setRequestBeanInfo(RequestFieldInfo<?> requestBeanInfo) {
    this.requestBeanInfo = requestBeanInfo;
  }

}
