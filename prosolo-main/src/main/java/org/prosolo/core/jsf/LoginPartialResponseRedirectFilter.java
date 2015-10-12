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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.prosolo.web.LoggedUserBean;

/**
 * @author "Nikola Milikic"
 *
 */
public class LoginPartialResponseRedirectFilter implements Filter {

	private static final String AJAX_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	        + "<partial-response><redirect url=\"%s\"></redirect></partial-response>";

	private static String[] allowedAccessPages = new String[]{
		"passwordReset",
		"recovery",
		"terms",
		"profile",
		"digest",
		"digest"
	};
	
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(false);
		String contextPath = request.getContextPath();
		String loginURL = contextPath + "/" + "login";

		LoggedUserBean loggedUserBean = (session == null) ? null : (LoggedUserBean) session.getAttribute("loggeduser");
		boolean loggedIn =  loggedUserBean == null ? false : loggedUserBean.getUser() != null;
		String requestURI = request.getRequestURI();
		boolean loginRequest = requestURI.equals(loginURL);
		boolean allowedToAccessPage = isAllowedToAccessPage(requestURI);
		boolean resourceRequest = requestURI.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER + "/") || 
				requestURI.startsWith(contextPath + "/resources/");
		boolean ajaxRequest = "partial/ajax".equals(request.getHeader("Faces-Request"));

		if (loggedIn || loginRequest || resourceRequest || allowedToAccessPage) {
			if (!resourceRequest) { // Prevent browser from caching restricted resources. See also http://stackoverflow.com/a/4194251/157882
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
				response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
				response.setDateHeader("Expires", 0); // Proxies.
			}

			chain.doFilter(request, response);
		} else if (ajaxRequest) { // Redirect on ajax request requires special XML response. See also http://stackoverflow.com/a/9311920/157882
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().printf(AJAX_REDIRECT_XML, loginURL);
		} else {
			chain.doFilter(request, response);
		}
	}

    /**
	 * @param requestURI
	 * @return
	 */
	private boolean isAllowedToAccessPage(String requestURI) {
		for (int i = 0; i < allowedAccessPages.length; i++) {
			if (requestURI.endsWith(allowedAccessPages[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
    public void destroy() { }
}
