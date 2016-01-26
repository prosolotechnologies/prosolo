/**
 * 
 */
package org.prosolo.core.jsf;

import java.io.IOException;

import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.web.ApplicationPage;
import org.prosolo.web.ApplicationPagesBean;
import org.prosolo.web.LoggedUserBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SavePageUrlInSessionFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		
		boolean resourceRequest = requestURI.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER + "/")
				|| requestURI.startsWith(contextPath + "/resources/");
		boolean ajaxRequest = "partial/ajax".equals(request.getHeader("Faces-Request"));

		if(!resourceRequest && !ajaxRequest) {
			String uri = requestURI;
			if(requestURI.startsWith(contextPath)) {
				uri = uri.substring(uri.indexOf(contextPath) + contextPath.length());
			}
			ApplicationPagesBean applicationPagesBean = ServiceLocator.getInstance()
					.getService(ApplicationPagesBean.class);
			ApplicationPage page = applicationPagesBean.getPageForURI(uri);
			HttpSession session = request.getSession(false);
			if(page != null && session != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) session.getAttribute("loggeduser");
				if(loggedUserBean != null) {
					loggedUserBean.getLearningContext().setPage(page);
					Gson gson = new GsonBuilder() 
				             .setPrettyPrinting()
				             .create();
					System.out.println("JSON " + gson.toJson(loggedUserBean.getLearningContext()));
				}
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
