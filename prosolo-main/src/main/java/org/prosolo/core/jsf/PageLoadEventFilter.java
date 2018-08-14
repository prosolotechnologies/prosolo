package org.prosolo.core.jsf;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.impl.Competence1ManagerImpl;
import org.prosolo.web.ApplicationPagesBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.stereotype.Component;

import javax.faces.application.ResourceHandler;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "pageLoadEventFilter")
public class PageLoadEventFilter implements Filter {

	private static Logger logger = Logger.getLogger(Competence1ManagerImpl.class);
	
	@Inject private EventFactory eventFactory;

	private static List<String> skipPages = Arrays.asList(
			"/api/",
			"/login.xhtml",
			"/root.xhtml",
			"/ltiproviderlaunch.xhtml",
			"/ltitoolproxyregistration.xhtml",
			"/openid.xhtml",
			"/favicon.ico",
			"/robots.txt",
			"/version.txt");
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		
		boolean resourceRequest = requestURI.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER + "/")
				|| requestURI.startsWith(contextPath + "/resources/");
		boolean ajaxRequest = "partial/ajax".equals(request.getHeader("Faces-Request"));
		boolean toSkip = skipPages.stream().anyMatch(page -> requestURI.startsWith(contextPath + page));

		if (!resourceRequest && !ajaxRequest && !toSkip) {
			String uri = requestURI;
			if (requestURI.startsWith(contextPath)) {
				uri = uri.substring(uri.indexOf(contextPath) + contextPath.length());
			}
			ApplicationPagesBean applicationPagesBean = ServiceLocator.getInstance().getService(ApplicationPagesBean.class);
			ApplicationPage page = applicationPagesBean.getPageForURI(uri);
			
			long userId = 0;
			long organizationId = 0;
			String sessionId = null;
			HttpSession session = request.getSession(false);

			if (session != null) {
				sessionId = session.getId();
				LoggedUserBean loggedUserBean = (LoggedUserBean) session.getAttribute("loggeduser");

				if (loggedUserBean != null) {
					userId = loggedUserBean.getUserId();
					organizationId = loggedUserBean.getOrganizationId(request);
				}
			}
			if (page == null) {
				logger.warn("Page " + uri + " is not mapped in ApplicationPage enum");
			}
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress != null) {
				// get first IP from comma separated list
				ipAddress = ipAddress.replaceFirst(",.*", "");
			} else {
				ipAddress = request.getRemoteAddr();
			}
			logger.info("IP address: " + ipAddress);

			Map<String, String> params = new HashMap<>();
			params.put("ip", ipAddress);
			params.put("uri", uri);
			params.put("pretty_uri", (String) request.getAttribute("javax.servlet.forward.request_uri"));
			eventFactory.generateEvent(
					EventType.PAGE_OPENED, UserContextData.of(userId, organizationId, sessionId, new PageContextData(uri, null, null)),
					null, null, null, params);
		}
		
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
