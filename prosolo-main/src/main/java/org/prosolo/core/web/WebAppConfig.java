package org.prosolo.core.web;

import org.prosolo.common.config.AppConfig;
import org.prosolo.common.config.CommonSettings;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class holds configuration that the servlet container will pick up during the startup process.
 * It contains parameters that were once in the web.xml file.
 *
 * @author Nikola Milikic
 * @date 2018-07-12
 * @since 1.2
 */
// For some reason, this annotation does not work
//@WebListener
public class WebAppConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("The application started");
        ServletContext servletContext = event.getServletContext();

        // iterate through all maps' entries and add them to the servlet context
        Stream.of(getWebAppParams(), getMyFacesParams(), getPrimeFacesParams())
                .forEach(map ->
                        map.entrySet().stream().forEach(entry ->
                                servletContext.setInitParameter(entry.getKey(), entry.getValue())
                        ));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("The application stopped");
    }

    /**
     * Returns general web app pagameters.
     *
     * @return Map of parameter name / value entries
     */
    private Map<String, String> getWebAppParams() {
        return new HashMap<String, String>() {{
            put("javax.faces.FACELETS_REFRESH_PERIOD", CommonSettings.getInstance().config.appConfig.projectMode.equals(AppConfig.ProjectMode.DEV) ? "0" : "-1");
            put("javax.faces.PROJECT_STAGE", CommonSettings.getInstance().config.appConfig.projectMode.getMode());
            put("javax.faces.STATE_SAVING_METHOD", "server");
            put("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
            put("javax.faces.FACELETS_LIBRARIES", "/WEB-INF/functions.taglib.xml");
            put("javax.faces.RESOURCE_EXCLUDES", ".class .jsp .jspx .properties .xhtml .xml");
            put("javax.faces.FACELETS_BUFFER_SIZE", "165535");
            put("com.sun.faces.enableRestoreView11Compatibility", "true");
            put("org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "true");
        }};
    }

    /**
     * Returns MyFaces related config.
     *
     * @return Map of parameter name / value entries
     */
    private Map<String, String> getMyFacesParams() {
        return new HashMap<String, String>() {{
            put("org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION", "20");
            put("org.apache.myfaces.SERIALIZE_STATE_IN_SESSION", "false");
            put("org.apache.myfaces.COMPRESS_STATE_IN_SESSION", "false");
            put("org.apache.myfaces.annotation.SCAN_PACKAGES", "org.prosolo.web");
            put("org.apache.myfaces.STRICT_JSF_2_ALLOW_SLASH_LIBRARY_NAME", "true");
            put("org.apache.myfaces.USE_ENCRYPTION", "false");
            put("org.apache.myfaces.ALGORITHM", "AES");
            put("org.apache.myfaces.SECRET", "Q0hBTkdFIFNFQ1JFVCEhIQ");
            put("org.apache.myfaces.MAC_SECRET", "Q0hBTkdFIE1BQ1NFQ1JFVA");
        }};
    }

    /**
     * Returns MyFaces related config.
     *
     * @return Map of parameter name / value entries
     */
    private Map<String, String> getPrimeFacesParams() {
        return new HashMap<String, String>() {{
            put("primefaces.THEME", "none");
        }};
    }
}
