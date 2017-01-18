package org.prosolo.core.ssl;/**
 * Created by zoran on 10/09/16.
 */
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * zoran 10/09/16
 */
public class AbsoluteSendRedirectFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void destroy() { }
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        //continue the request
        chain.doFilter(request, new SendRedirectOverloadedResponse((HttpServletRequest) request, (HttpServletResponse) response));
    }
}
