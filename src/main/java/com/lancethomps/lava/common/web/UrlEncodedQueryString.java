package com.lancethomps.lava.common.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public final class UrlEncodedQueryString {

  public enum Separator {

    AMPERSAND {
      @Override
      public String toString() {

        return "&";
      }
    },

    SEMICOLON {
      @Override
      public String toString() {

        return ";";
      }
    }
  }

  private static final String PARSE_PARAMETER_SEPARATORS = String.valueOf(Separator.AMPERSAND) + Separator.SEMICOLON;
  private final Map<String, List<String>> queryMap = new LinkedHashMap<String, List<String>>();

  private UrlEncodedQueryString() {

  }

  public static UrlEncodedQueryString create() {

    return new UrlEncodedQueryString();
  }

  public static UrlEncodedQueryString create(Map<String, List<String>> parameterMap) {

    UrlEncodedQueryString queryString = new UrlEncodedQueryString();

    for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
      queryString.queryMap.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
    }

    return queryString;
  }

  public static UrlEncodedQueryString parse(final CharSequence query) {

    UrlEncodedQueryString queryString = new UrlEncodedQueryString();

    queryString.appendOrSet(query, true);

    return queryString;
  }

  public static UrlEncodedQueryString parse(final URI uri) {

    return parse(uri.getRawQuery());
  }

  public UrlEncodedQueryString append(final String query) {

    appendOrSet(query, true);
    return this;
  }

  public UrlEncodedQueryString append(final String name, final Number value) {

    appendOrSet(name, value.toString(), true);
    return this;
  }

  public UrlEncodedQueryString append(final String name, final String value) {

    appendOrSet(name, value, true);
    return this;
  }

  public URI apply(URI uri) {

    return apply(uri, Separator.AMPERSAND);
  }

  public URI apply(URI uri, Separator separator) {

    StringBuilder builder = new StringBuilder();
    if (uri.getScheme() != null) {
      builder.append(uri.getScheme());
      builder.append(':');
    }
    if (uri.getHost() != null) {
      builder.append("//");
      if (uri.getUserInfo() != null) {
        builder.append(uri.getUserInfo());
        builder.append('@');
      }
      builder.append(uri.getHost());
      if (uri.getPort() != -1) {
        builder.append(':');
        builder.append(uri.getPort());
      }
    } else if (uri.getAuthority() != null) {
      builder.append("//");
      builder.append(uri.getAuthority());
    }
    if (uri.getPath() != null) {
      builder.append(uri.getPath());
    }

    String query = toString(separator);
    if (query.length() != 0) {
      builder.append('?');
      builder.append(query);
    }
    if (uri.getFragment() != null) {
      builder.append('#');
      builder.append(uri.getFragment());
    }

    try {
      return new URI(builder.toString());
    } catch (URISyntaxException e) {

      throw new RuntimeException(e);
    }
  }

  public boolean contains(final String name) {

    return queryMap.containsKey(name);
  }

  public String get(final String name) {

    List<String> parameters = getValues(name);

    if ((parameters == null) || parameters.isEmpty()) {
      return null;
    }

    return parameters.get(0);
  }

  public Map<String, List<String>> getMap() {

    LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();

    for (Map.Entry<String, List<String>> entry : queryMap.entrySet()) {
      List<String> listValues = entry.getValue();
      map.put(entry.getKey(), new ArrayList<String>(listValues));
    }

    return map;
  }

  public Iterator<String> getNames() {

    return queryMap.keySet().iterator();
  }

  public List<String> getValues(final String name) {

    return queryMap.get(name);
  }

  public boolean isEmpty() {

    return queryMap.isEmpty();
  }

  public UrlEncodedQueryString remove(final String name) {

    appendOrSet(name, null, false);
    return this;
  }

  public UrlEncodedQueryString set(final String query) {

    appendOrSet(query, false);
    return this;
  }

  public UrlEncodedQueryString set(final String name, final Number value) {

    if (value == null) {
      remove(name);
      return this;
    }

    appendOrSet(name, value.toString(), false);
    return this;
  }

  public UrlEncodedQueryString set(final String name, final String value) {

    appendOrSet(name, value, false);
    return this;
  }

  @Override
  public String toString() {

    return toString(Separator.AMPERSAND);
  }

  public String toString(Separator separator) {

    StringBuilder builder = new StringBuilder();

    for (String name : queryMap.keySet()) {
      for (String value : queryMap.get(name)) {
        if (builder.length() != 0) {
          builder.append(separator);
        }

        try {
          builder.append(URLEncoder.encode(name, "UTF-8"));

          if (value != null) {
            builder.append('=');
            builder.append(URLEncoder.encode(value, "UTF-8"));
          }
        } catch (UnsupportedEncodingException e) {

          throw new RuntimeException(e);
        }
      }
    }

    return builder.toString();
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == this) {
      return true;
    }

    if (!(obj instanceof UrlEncodedQueryString)) {
      return false;
    }

    String query = toString();
    String thatQuery = ((UrlEncodedQueryString) obj).toString();

    return query.equals(thatQuery);
  }

  @Override
  public int hashCode() {

    return toString().hashCode();
  }

  private void appendOrSet(final CharSequence parameters, final boolean append) {

    if (parameters == null) {
      return;
    }

    StringTokenizer tokenizer = new StringTokenizer(parameters.toString(), PARSE_PARAMETER_SEPARATORS);

    Set<String> setAlreadyParsed = null;

    while (tokenizer.hasMoreTokens()) {
      String parameter = tokenizer.nextToken();

      int indexOf = parameter.indexOf('=');

      String name;
      String value;

      try {
        if (indexOf == -1) {
          name = parameter;
          value = null;
        } else {
          name = parameter.substring(0, indexOf);
          value = parameter.substring(indexOf + 1);
        }

        name = URLDecoder.decode(name, "UTF-8");

        if (!append) {
          if (setAlreadyParsed == null) {
            setAlreadyParsed = new HashSet<String>();
          }

          if (!setAlreadyParsed.contains(name)) {
            remove(name);
          }

          setAlreadyParsed.add(name);
        }

        if (value != null) {
          value = URLDecoder.decode(value, "UTF-8");
        }

        appendOrSet(name, value, true);
      } catch (UnsupportedEncodingException e) {

        throw new RuntimeException(e);
      }
    }
  }

  private void appendOrSet(final String name, final String value, final boolean append) {

    if (name == null) {
      throw new NullPointerException("name");
    }

    if (append) {
      List<String> listValues = queryMap.get(name);

      if (listValues != null) {
        listValues.add(value);
        return;
      }
    } else if (value == null) {

      queryMap.remove(name);
      return;
    }

    List<String> listValues = new ArrayList<String>();
    listValues.add(value);

    queryMap.put(name, listValues);
  }

}
