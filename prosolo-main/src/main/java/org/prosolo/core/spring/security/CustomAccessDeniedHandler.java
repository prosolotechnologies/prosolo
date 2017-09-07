package org.prosolo.core.spring.security;

import org.prosolo.web.util.page.PageUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author stefanvuckovic
 * @date 2017-08-23
 * @since 1.0.0
 */
public class CustomAccessDeniedHandler extends AccessDeniedHandlerImpl {

    private String errorPage;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        super.setErrorPage(PageUtil.getSectionForUri(request.getRequestURI().substring(request.getContextPath().length())).getPrefix() + errorPage);
        super.handle(request, response, accessDeniedException);
    }

    @Override
    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }
}
