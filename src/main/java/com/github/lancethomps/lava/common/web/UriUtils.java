package com.github.lancethomps.lava.common.web;

import static com.github.lancethomps.lava.common.logging.Logs.logError;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * The Class UrlUtils.
 */
public class UriUtils {

	/** The Constant PAGE_FILE_PATTERN. */
	public static final Pattern PAGE_FILE_PATTERN = Pattern.compile("/sites/([^/]+)/(.*?)([.]page)?$");

	/** The Constant URI_DOMAIN_PATTERN. */
	public static final Pattern URI_DOMAIN_PATTERN = Pattern.compile("^([\\w.-]+:)?//([^/]*)/?.*");

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(UriUtils.class);

	/** The Constant PAGE_LINK_URL_PATTERN. */
	private static final Pattern PAGE_LINK_URL_PATTERN = Pattern.compile("^\\$PAGE_LINK\\[(.*)\\]$");

	/** The Constant PAGE_URL_PATTERN. */
	private static final Pattern PAGE_URL_PATTERN = Pattern.compile("^/([^/]+)/?(.*?)([.]page)?$");

	/** The scheme to port mappings. */
	private static Map<String, Integer> schemeToPortMappings = new HashMap<>();

	static {
		schemeToPortMappings.put("ftp", 21);
		schemeToPortMappings.put("http", 80);
		schemeToPortMappings.put("https", 443);
	}

	/**
	 * Append query parameter.
	 *
	 * @param uri the uri
	 * @param name the name
	 * @param value the value
	 * @return the string
	 */
	public static String appendQueryParameter(String uri, String name, String value) {
		String queryString = name + '=' + value;
		return appendQueryString(uri, queryString);
	}

	/**
	 * Append the specified query string to an existing URI (fast implementation).
	 *
	 * @param uri the uri
	 * @param queryString the query string
	 * @return the string
	 */
	public static String appendQueryString(String uri, String queryString) {

		String newUri = uri;

		if (isNotEmpty(queryString)) {
			queryString = getEncodedQueryString(queryString).toString();
			int length = uri.length() + queryString.length() + 1;
			StringBuilder sb = new StringBuilder(length);

			String query = null;
			String path;

			int fragmentIndex = uri.indexOf('#');
			String fragment = fragmentIndex < 0 ? null : uri.substring(fragmentIndex);
			String file = fragmentIndex < 0 ? uri : uri.substring(0, fragmentIndex);

			int queryIndex = file.indexOf('?');
			if (queryIndex != -1) {
				query = file.substring(queryIndex + 1);
				path = file.substring(0, queryIndex);
			} else {
				path = file;
			}

			sb.append(path);
			sb.append('?');
			sb.append(queryString);

			if (query != null) {
				sb.append('&');
				sb.append(query);
			}

			if (fragment != null) {
				sb.append(fragment);
			}

			newUri = sb.toString();
		}

		return newUri;

	}

	/**
	 * Append specified string to end of to path component of URI.
	 *
	 * @param uri the uri
	 * @param append the append
	 * @return the string
	 */
	public static String appendToPath(String uri, String append) {

		String path = uri;
		String queryFragment = "";
		int queryIndex = uri.indexOf('?');

		if (queryIndex >= 0) {
			path = uri.substring(0, queryIndex);
			queryFragment = uri.substring(queryIndex);
		} else {
			int fragmentIndex = uri.indexOf('#');
			if (fragmentIndex >= 0) {
				path = uri.substring(0, fragmentIndex);
				queryFragment = uri.substring(fragmentIndex);
			}
		}

		return path + append + queryFragment;
	}

	/**
	 * Creates the from file path.
	 *
	 * @param path the path
	 * @return the uri
	 * @throws URISyntaxException the URI syntax exception
	 */
	public static URI createFromFilePath(String path) throws URISyntaxException {
		return new URI(StringUtils.replace(path, "\\", "/"));
	}

	/**
	 * Decode.
	 *
	 * @param value the value
	 * @return the string
	 */
	public static String decode(String value) {
		String decoded;
		try {
			decoded = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			logError(LOG, e, e.getMessage());
			decoded = value;
		}
		return decoded;
	}

	/**
	 * Encode.
	 *
	 * @param value the value
	 * @return the string
	 */
	public static String encode(String value) {
		String encoded;
		try {
			encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			logError(LOG, e, e.getMessage());
			encoded = value;
		}
		return encoded;
	}

	/**
	 * Encode xml.
	 *
	 * @param aString the a string
	 * @return the string
	 */
	public static String encodeXml(String aString) {
		String xmlUri = StringEscapeUtils.escapeXml10(aString);
		return xmlUri;
	}

	/**
	 * Extract query parameter.
	 *
	 * @param url the url
	 * @param name the name
	 * @param decode the decode
	 * @return the string
	 */
	public static String extractQueryParameter(String url, String name, boolean decode) {
		String val = null;
		Matcher matcher = Pattern.compile("([?&])" + name + "(=([^&#]*)|&|#|$)").matcher(url);
		if (matcher.find()) {
			val = matcher.group(3);
		}
		return (val != null) && decode ? decode(val) : null;
	}

	/**
	 * Gets the encoded query string.
	 *
	 * @param queryString the query string
	 * @return the encoded query string
	 */
	public static UrlEncodedQueryString getEncodedQueryString(String queryString) {
		UrlEncodedQueryString encodedQueryString = UrlEncodedQueryString.parse(queryString);
		return encodedQueryString;
	}

	/**
	 * Encode the query string portion of the given URI (fast implementation).
	 *
	 * @param uri the uri
	 * @return the encoded uri
	 */
	public static String getEncodedUri(String uri) {
		String query = null;
		String path;

		int fragmentIndex = uri.indexOf('#');
		String fragment = fragmentIndex < 0 ? null : uri.substring(fragmentIndex + 1);
		String file = fragmentIndex < 0 ? uri : uri.substring(0, fragmentIndex);

		int queryIndex = file.lastIndexOf('?');
		if (queryIndex != -1) {
			query = file.substring(queryIndex + 1);
			path = file.substring(0, queryIndex);
		} else {
			path = file;
		}

		return getEncodedUri(path, query, fragment);
	}

	/**
	 * Gets the xml encoded uri.
	 *
	 * @param uri the uri up to the query string or fragment
	 * @param query the query
	 * @param fragment the fragment
	 * @return the xml encoded uri
	 */
	public static String getEncodedUri(String uri, String query, String fragment) {

		String encodedQueryString = query;
		if (isNotEmpty(query)) {
			UrlEncodedQueryString urlEncodedQueryString = getEncodedQueryString(query);
			encodedQueryString = urlEncodedQueryString.toString();
		}

		return getUriWithQueryString(uri, encodedQueryString, fragment);
	}

	/**
	 * Gets the host.
	 *
	 * @param url the url
	 * @return the host
	 */
	public static String getHost(String url) {
		try {
			return URI.create(url).getHost();
		} catch (IllegalArgumentException e) {
			Logs.logError(LOG, e, "Issue getting host from URL [%s]", url);
			return null;
		}
	}

	/**
	 * Gets the page name from a page file given in a DCR.
	 * <p>
	 * e.g. /sites/uk-retail/compliance/terms.page --> compliance/terms
	 * </p>
	 *
	 * @param pageLink the page link
	 * @return the page name
	 */
	public static String getPageName(String pageLink) {
		String pageName = pageLink;

		Matcher matcher = PAGE_FILE_PATTERN.matcher(pageLink);
		if (matcher.matches()) {
			pageName = matcher.group(2);
		}

		return pageName;
	}

	/**
	 * Returns the page name from an internal URI. <br>
	 * e.g. /uk-retail/compliance/terms-and-conditions.page --> compliance/terms-and-conditions
	 *
	 * @param site the site
	 * @param uri the uri
	 * @param startPage the start page
	 * @return the page name
	 */
	public static String getPageName(String site, String uri, String startPage) {
		String pageName = uri;

		Matcher matcher = PAGE_URL_PATTERN.matcher(uri);
		if (matcher.matches()) {

			pageName = matcher.group(2);
			if (isEmpty(pageName) || pageName.equals("/")) {
				pageName = startPage;
			}
		}

		return pageName;
	}

	/**
	 * Gets the path.
	 *
	 * @param uri the uri
	 * @return the path
	 */
	public static String getPath(String uri) {
		int queryIndex = uri.indexOf('?');

		if (queryIndex >= 0) {
			uri = uri.substring(0, queryIndex);

		} else {
			int fragmentIndex = uri.indexOf('#');
			if (fragmentIndex >= 0) {
				uri = uri.substring(0, fragmentIndex);
			}
		}

		return uri;
	}

	/**
	 * Gets the query string.
	 *
	 * @param uri the uri
	 * @return the query string
	 */
	public static String getQueryString(String uri) {

		String queryString = null;

		int fragmentIndex = uri.indexOf('#');
		String file = fragmentIndex < 0 ? uri : uri.substring(0, fragmentIndex);
		int queryIndex = file.lastIndexOf('?');

		if (queryIndex != -1) {
			queryString = file.substring(queryIndex + 1);
		}

		return queryString;
	}

	/**
	 * Gets the query string from custom parameters.
	 *
	 * @param params the params
	 * @param encode the encode
	 * @return the query string from custom parameters
	 */
	public static String getQueryStringFromCustomParameters(Map<String, ? extends Collection<String>> params, boolean encode) {
		StringBuilder query = new StringBuilder();
		if (MapUtils.isNotEmpty(params)) {
			for (Entry<String, ? extends Collection<String>> entry : params.entrySet()) {
				for (String paramValue : entry.getValue()) {
					query
						.append(encode ? encode(entry.getKey()) : entry.getKey())
						.append('=')
						.append(encode ? encode(paramValue) : paramValue)
						.append('&');
				}
			}
			query.deleteCharAt(query.length() - 1);
		}
		return query.toString();
	}

	/**
	 * Gets the query string from parameters.
	 *
	 * @param request the request
	 * @return the query string from parameters
	 */
	public static String getQueryStringFromParameters(HttpServletRequest request) {
		return getQueryStringFromParameters(request.getParameterMap());
	}

	/**
	 * Gets the query string from parameters.
	 *
	 * @param params the params
	 * @return the query string from parameters
	 */
	public static String getQueryStringFromParameters(Map<String, String[]> params) {
		StringBuilder query = new StringBuilder();
		if (MapUtils.isNotEmpty(params)) {
			for (Entry<String, String[]> entry : params.entrySet()) {
				for (String paramValue : entry.getValue()) {
					query.append(entry.getKey()).append('=').append(paramValue).append('&');
				}
			}
			query.deleteCharAt(query.length() - 1);
		}
		return query.toString();
	}

	/**
	 * Builds the scheme/domain/port part of a URI taking into account default port mappings.
	 *
	 * @param scheme the scheme
	 * @param domain the domain
	 * @param port the port
	 * @return the string
	 */
	public static StringBuilder getUriSchemeDomainPort(String scheme, String domain, int port) {
		StringBuilder uri = new StringBuilder(50).append(scheme).append("://").append(domain);

		int defaultPort = schemeToPortMappings.get(scheme);
		if ((defaultPort != port) && (port != 80) && (port != -1)) {
			uri.append(':').append(port);
		}

		return uri;
	}

	/**
	 * Gets the uri with query string.
	 *
	 * @param uri the uri portion up to the query string
	 * @param queryString the query string
	 * @return the uri with query string
	 */
	public static String getUriWithQueryString(String uri, String queryString) {
		return getUriWithQueryString(uri, queryString, null);
	}

	/**
	 * Gets the uri with query string.
	 *
	 * @param uri the uri portion up to the query string
	 * @param queryString the query string
	 * @param fragment the fragment
	 * @return the uri with query string
	 */
	public static String getUriWithQueryString(String uri, String queryString, String fragment) {

		StringBuilder sb = new StringBuilder(uri);

		if (isNotEmpty(queryString)) {
			sb.append('?').append(queryString);
		}

		if (isNotEmpty(fragment)) {
			sb.append('#').append(fragment);
		}

		return sb.toString();
	}

	/**
	 * Checks if is absolute uri.
	 *
	 * @param uri the uri
	 * @return true, if is absolute uri
	 */
	public static boolean isAbsoluteUri(String uri) {
		Matcher matcher = URI_DOMAIN_PATTERN.matcher(uri);
		return matcher.matches();
	}

	/**
	 * Checks if is valid url.
	 *
	 * @param url the url
	 * @return true, if is valid url
	 */
	public static boolean isValidUrl(String url) {
		try {
			URL urlObj = new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * Removes the query parameter.
	 *
	 * @param url the url
	 * @param keys the keys
	 * @return the string
	 */
	public static String removeQueryParameter(String url, String... keys) {
		for (String key : keys) {
			Pattern pattern = Pattern.compile("(?<=(&|\\?|^))" + key + "=[^&]+&?");
			url = removeEnd(removeEnd(pattern.matcher(url).replaceAll(StringUtils.EMPTY), "&"), "?");
		}
		return url;
	}

	/**
	 * Strips the $PAGE_LINK out of any URL. Returns the URL if it doesn't contain PAGE_LINK.
	 *
	 * @param url the url
	 * @return the stripped url
	 */
	public static String stripPageLink(String url) {
		String strippedUrl = url;

		if (isNotEmpty(url)) {
			Matcher matcher = PAGE_LINK_URL_PATTERN.matcher(url);
			if (matcher.matches()) {
				strippedUrl = matcher.group(1);
			}
		}

		return strippedUrl;
	}
}
