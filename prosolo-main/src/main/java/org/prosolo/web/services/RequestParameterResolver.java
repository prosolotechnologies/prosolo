package org.prosolo.web.services;

import javax.servlet.http.HttpServletRequest;

/**
 * @author stefanvuckovic
 * @date 2018-10-31
 * @since 1.2.0
 */
public interface RequestParameterResolver {

    long getOrganizationIdFromRequestParameter(HttpServletRequest req);
}
