package org.prosolo.core.jsf;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.impl.Competence1ManagerImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Filter that checks whether url has trailing whitespace characters
 * and if it does, it redirects to the url without whitespace characters
 *
 * @author stefanvuckovic
 * @date 2019-05-29
 * @since 1.3.2
 */
public class RemoveTrailingSpaceFilter implements Filter {

    private static Logger logger = Logger.getLogger(Competence1ManagerImpl.class);

    private String[] whitespaceCharacters = {"%E2%80%8B"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String reqUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (request.getMethod().equals("GET")) {
            boolean endsWithSpace = false;
            String redirectUrl = null;
            if (queryString != null) {
                Optional<String> optQueryString = getStringWithoutTrailingWhitespaceIfExists(queryString);
                if (optQueryString.isPresent()) {
                    endsWithSpace = true;
                    redirectUrl = reqUrl + "?" + optQueryString.get();
                }
            } else {
                Optional<String> optUrl = getStringWithoutTrailingWhitespaceIfExists(reqUrl);
                if (optUrl.isPresent()) {
                    endsWithSpace = true;
                    redirectUrl = optUrl.get();
                }
            }
            if (endsWithSpace) {
                ((HttpServletResponse) res).sendRedirect(redirectUrl);
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * Returns passed string without trailing whitespace if whitespace exists.
     * Otherwise it returns empty value;
     *
     * @param s
     * @return
     */
    public Optional<String> getStringWithoutTrailingWhitespaceIfExists(String s) {
        if (s == null) {
            return Optional.empty();
        }
        String whitespaceCharactersRegex = String.format("(?:%s)+$", String.join("|", Arrays.stream(getWhitespaceCharacters()).map(el -> Pattern.quote(el)).collect(Collectors.toList())));
        String res = s.replaceFirst(whitespaceCharactersRegex, "");
        //if string is not changed it means it did not contain whitespaces so empty is returned
        return s.equals(res) ? Optional.empty() : Optional.of(res);
    }

    public String[] getWhitespaceCharacters() {
        return whitespaceCharacters;
    }

    @Override
    public void destroy() { }
}
