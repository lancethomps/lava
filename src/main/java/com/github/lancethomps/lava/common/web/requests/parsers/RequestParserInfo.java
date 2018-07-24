package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Map;

/**
 * The Class HttpRequestParserInfo.
 */
public class RequestParserInfo {

	/** The complex request infos. */
	private List<RequestFieldInfo<?>> complexRequestInfos;

	/** The infos. */
	private List<RequestFieldInfo<?>> infos;

	/** The infos map. */
	private Map<String, RequestFieldInfo<?>> infosMap;

	/** The request bean info. */
	private RequestFieldInfo<?> requestBeanInfo;

	/**
	 * @return the complexRequestInfos
	 */
	public List<RequestFieldInfo<?>> getComplexRequestInfos() {
		return complexRequestInfos;
	}

	/**
	 * @return the infos
	 */
	public List<RequestFieldInfo<?>> getInfos() {
		return infos;
	}

	/**
	 * @return the infosMap
	 */
	public Map<String, RequestFieldInfo<?>> getInfosMap() {
		return infosMap;
	}

	/**
	 * @return the requestBeanInfo
	 */
	public RequestFieldInfo<?> getRequestBeanInfo() {
		return requestBeanInfo;
	}

	/**
	 * @param complexRequestInfos the complexRequestInfos to set
	 */
	public void setComplexRequestInfos(List<RequestFieldInfo<?>> complexRequestInfos) {
		this.complexRequestInfos = complexRequestInfos;
	}

	/**
	 * @param infos the infos to set
	 */
	public void setInfos(List<RequestFieldInfo<?>> infos) {
		this.infos = infos;
	}

	/**
	 * @param infosMap the infosMap to set
	 */
	public void setInfosMap(Map<String, RequestFieldInfo<?>> infosMap) {
		this.infosMap = infosMap;
	}

	/**
	 * @param requestBeanInfo the requestBeanInfo to set
	 */
	public void setRequestBeanInfo(RequestFieldInfo<?> requestBeanInfo) {
		this.requestBeanInfo = requestBeanInfo;
	}

}
