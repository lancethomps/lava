package com.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lancethomps.lava.common.logging.Logs;

public class RequestValidationFactory {

  private static final Logger LOG = LogManager.getLogger(RequestValidationFactory.class);

  public static <T> List<String> validateFieldList(
    T request,
    String userId,
    List<String> current,
    Set<String> permissioned,
    Set<String> explicitlyAllowed
  ) {
    return current == null ? null : current.stream().filter(field -> {
      if ((explicitlyAllowed != null) && explicitlyAllowed.contains(field)) {
        explicitlyAllowed.remove(field);
        return true;
      }
      if (StringUtils.contains(field, "*")) {
        String prefix = StringUtils.substringBefore(field, "*");
        if (permissioned.stream().anyMatch(pf -> pf.startsWith(prefix)) || permissioned.stream().anyMatch(field::startsWith)) {
          Logs.logError(
            LOG, new RequestValidationException(), "User [%s] tried to use permissioned field of [%s] in request [%s]!", userId,
            field, request
          );
          return false;
        }
        return true;
      }
      return permissioned.stream().noneMatch(field::startsWith);
    }).collect(Collectors.toList());
  }

}
