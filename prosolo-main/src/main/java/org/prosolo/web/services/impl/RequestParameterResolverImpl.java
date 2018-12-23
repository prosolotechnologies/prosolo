package org.prosolo.web.services.impl;

import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.services.RequestParameterNames;
import org.prosolo.web.services.RequestParameterResolver;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author stefanvuckovic
 * @date 2018-10-31
 * @since 1.2.0
 */
@Service ("org.prosolo.web.services.RequestParameterResolver")
public class RequestParameterResolverImpl implements RequestParameterResolver {

    @Inject private UrlIdEncoder idEncoder;

    @Override
    public long getOrganizationIdFromRequestParameter(HttpServletRequest req) {
        if (req != null) {
            String orgId = req.getParameter(RequestParameterNames.ORGANIZATION_ID);
            return idEncoder.decodeId(orgId);
        } else {
            return 0L;
        }
    }

}
