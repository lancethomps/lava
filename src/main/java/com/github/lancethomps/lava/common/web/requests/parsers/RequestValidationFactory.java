package com.github.lancethomps.lava.common.web.requests.parsers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.github.lancethomps.lava.common.logging.Logs;

/**
 * A factory for creating RequestValidation objects.
 */
public class RequestValidationFactory {

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(RequestValidationFactory.class);

	/**
	 * Validate field list.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @param userId the user id
	 * @param current the current
	 * @param permissioned the permissioned
	 * @param explicitlyAllowed the explicitly allowed
	 * @return the list
	 */
	public static <T> List<String> validateFieldList(T request, String userId, List<String> current, Set<String> permissioned, Set<String> explicitlyAllowed) {
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
			if (permissioned.stream().anyMatch(field::startsWith)) {
				return false;
			}
			return true;
		}).collect(Collectors.toList());
	}
}
