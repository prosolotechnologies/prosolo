package org.prosolo.core.ssl;/**
 * Created by zoran on 10/09/16.
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * zoran 10/09/16
 */
class SendRedirectOverloadedResponse extends HttpServletResponseWrapper {

    private HttpServletRequest m_request;
    private String prefix = null;

    public SendRedirectOverloadedResponse(HttpServletRequest inRequest,
                                          HttpServletResponse response) {
        super(response);
        m_request = inRequest;
        prefix = getPrefix(inRequest);
    }

    public void sendRedirect(String location) throws IOException {
        String finalurl = null;
        if (isUrlAbsolute(location)) {
            finalurl = location;
        } else {
            finalurl = fixForScheme(prefix + location);
        }
        super.sendRedirect(finalurl);
    }

    public boolean isUrlAbsolute(String url) {
        String lowercaseurl = url.toLowerCase();
        if (lowercaseurl.startsWith("http") == true) {
            return true;
        } else {
            return false;
        }
    }

    public String fixForScheme(String url) {
        //alter the url here if you were to change the scheme return url;
        return url;
    }

    public String getPrefix(HttpServletRequest request) {
        StringBuffer str = request.getRequestURL();
        String url = str.toString();
        String uri = request.getRequestURI();
        int offset = url.indexOf(uri);
        String prefix = url.substring(0,offset);
        return prefix;
    }
}
