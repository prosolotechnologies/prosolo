package org.prosolo.core.jsf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ResourceHandler;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.impl.Competence1ManagerImpl;
import org.prosolo.web.ApplicationPagesBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.stereotype.Component;

@Component(value = "pageLoadEventFilter")
public class PageLoadEventFilter implements Filter {

	private static Logger logger = Logger.getLogger(Competence1ManagerImpl.class);
	
	@Inject private EventFactory eventFactory;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		
		boolean resourceRequest = requestURI.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER + "/")
				|| requestURI.startsWith(contextPath + "/resources/");
		boolean ajaxRequest = "partial/ajax".equals(request.getHeader("Faces-Request"));
		//boolean ajaxRequest2 = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
		//logger.info("IS AJAX REQUEST: " + ajaxRequest + ", " + ajaxRequest2);
		if(!resourceRequest && !ajaxRequest) {
			String uri = requestURI;
			if(requestURI.startsWith(contextPath)) {
				uri = uri.substring(uri.indexOf(contextPath) + contextPath.length());
			}
			ApplicationPagesBean applicationPagesBean = ServiceLocator.getInstance()
					.getService(ApplicationPagesBean.class);
			ApplicationPage page = applicationPagesBean.getPageForURI(uri);
			
			long userId = 0;
			HttpSession session = request.getSession(false);
			if(session != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) session.getAttribute("loggeduser");
				if(loggedUserBean != null) {
					userId = loggedUserBean.getUserId();
				}
			}
			if(page == null) {
				logger.warn("Page is not mapped in ApplicationPage enum");
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
			try {
				eventFactory.generateEvent(EventType.PAGE_OPENED, userId, 
						null, null, uri, null,
						null, params);
			} catch (EventException e) {
				logger.error("Error while generating page open event " + e);
			}
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
