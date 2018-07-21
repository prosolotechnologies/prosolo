/**
 * 
 */
package org.prosolo.core.jsf;

import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author "Nikola Milikic"
 *
 */
public class PreventBrowserCachingRestrictedPagesFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    	//Prevent browser from caching restricted resources. See also http://stackoverflow.com/a/4194251/157882
		String requestURI = ((HttpServletRequest) req).getRequestURI();
		String contextPath = ((HttpServletRequest) req).getContextPath();
		boolean resourceRequest = requestURI.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER + "/")
				|| requestURI.startsWith(contextPath + "/resources/");
		if (!resourceRequest) {
			HttpServletResponse response = (HttpServletResponse) res;

			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0); // Proxies.
		}

		chain.doFilter(req, res);
	}

	@Override
    public void destroy() { }
}
