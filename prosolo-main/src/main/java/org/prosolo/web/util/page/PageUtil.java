package org.prosolo.web.util.page;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.web.util.ResourceBundleUtil;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

public class PageUtil {
	
	protected static Logger logger = Logger.getLogger(PageUtil.class);

	public static String getPostParameter(String parameterName) {
		Map<String, String> contextParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return contextParameters.get(parameterName);
	}
	
	public static String getGetParameter(String parameterName) {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get(parameterName);
	}
	
	public static int getGetParameterAsInteger(String parameterName) {
		String param = getGetParameter(parameterName);
		
		try {
			return Integer.parseInt(param);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static void fireSuccessfulInfoMessage(String description) {
		fireSuccessfulInfoMessage(null, description);
	}
	
	public static void fireSuccessfulInfoMessageAcrossPages(String description) {
		fireSuccessfulInfoMessage(null, description);
		keepFiredMessagesAcrossPages();
	}

	public static void fireErrorMessageAcrossPages(String msg) {
		fireErrorMessage(msg);
		keepFiredMessagesAcrossPages();
	}

	public static void keepFiredMessagesAcrossPages() {
		ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
		Flash flash = extContext.getFlash();
		flash.setKeepMessages(true);
		flash.setRedirect(true);
	}
	
	public static void fireSuccessfulInfoMessageFromBundle(String messageName, Locale locale, Object... parameters) {
		try {
			String pattern = ResourceBundleUtil.getMessage(
					messageName, 
					locale);
			
			String message = MessageFormat.format(pattern, parameters);
			
			fireSuccessfulInfoMessage(null, message);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}
	
	public static void fireSuccessfulInfoMessage(String clientId, String description) {
		fireWarnMessage(clientId, "Successful", description);
	} 

	public static void fireWarnMessage(String clientId, String title, String description) {
		FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(title, description));
	}
	
	public static void fireErrorMessage(String description) {
		fireErrorMessage(null, description);
	}
	
	public static void fireErrorMessage(String clientId, String description) {
		fireErrorMessage(clientId, "Error", description);
	}
	
	public static void fireErrorMessage(String clientId, String title, String description) {
		FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, description));
	}

	public static void fireWarnMessage(String title, String description) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, description));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getViewScopedBean(String beanName, Class<T> clazz) {
		Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
		return (T) viewMap.get(beanName);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getSessionScopedBean(String beanName, Class<T> clazz) {
	    Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		return (T) sessionMap.get(beanName);
	}

	public static void redirectToLoginPage() {
		redirect("login?faces-redirect=true");
	}

	/**
	 * Redirects to the url specified by {@code url} argument after prepending application context path.
	 *
	 * @param url - relative url
	 */
	public static void redirect(String url) {
		try {
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			extContext.redirect(extContext.getRequestContextPath() + url);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static void sendToAccessDeniedPage() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().dispatch("accessDenied");
	}
	
	/**
	 * Returns section for current view.
	 * Example: if current view is '/manage/credential.xhtml method will return 
	 * /manage.
	 * @return
	 */
	public static PageSection getSectionForView() {
		return getSectionForUri(getPage());
	}

	public static String getPage() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId();
	}

	/**
	 * Returns section based on a passed uri.
	 *
	 * It is not important if uri passed is pretty uri or servlet path (path to real file)
	 * as long as it does not contain context path
	 *
	 * @return
	 */
	public static PageSection getSectionForUri(String uri) {
		/*
		 * find section by returning uri substring from the beginning to the second
		 * occurrence of '/' character. That is because uri always starts with
		 * "/section/page" (if there is a section)
		 */
		int secondSlashIndex = StringUtils.ordinalIndexOf(uri, "/", 2);
		String section = "";
		if (secondSlashIndex != -1) {
			section = uri.substring(0, secondSlashIndex);
		} else {
			/*
			if there is no second slash, whole uri is set to be the current section:
			this covers cases where uri is /admin or /manage
			 */
			section = uri;
		}

		if (section.equals(PageSection.ADMIN.getPrefix())) {
			return PageSection.ADMIN;
		} else if (section.equals(PageSection.MANAGE.getPrefix())) {
			return PageSection.MANAGE;
		} else {
			return PageSection.STUDENT;
		}
	}

	/*
	 * Retrieves original URL after a forward from Rewrite framework
	 */
	public static String getRewriteURL() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		
		return (String) request.getAttribute("javax.servlet.forward.request_uri");
	}
	
	/**
	 * Extracts learning context post parameters from request and returns result.
	 * 
	 * Method relies on following parameter names:
	 *  - 'page' param name for page
	 *  - 'learningContext' param name for context
	 *  - 'service' param name for service
	 *  
	 * @return
	 */
	public static PageContextData extractLearningContextData() {
		String page = getPostParameter("page");
		String lContext = getPostParameter("learningContext");
		String service = getPostParameter("service");
		PageContextData context = new PageContextData(page, lContext, service);
		return context;
	}

	/**
	 * Extracts learning context from component attributes and returns result.
	 *
	 * Method relies on following attributes names:
	 *  - 'page' attribute name for page
	 *  - 'learningContext' attribute name for context
	 *  - 'service' attribute name for service
	 *
	 * @return
	 */
	public static PageContextData extractLearningContextDataFromComponent(UIComponent component) {
		String page = (String) component.getAttributes().get("page");
		String lContext = (String) component.getAttributes().get("learningContext");
		String service = (String) component.getAttributes().get("service");
		PageContextData context = new PageContextData(page, lContext, service);
		return context;
	}
	
	public static void forward(String url) {
		try {
			FacesContext.getCurrentInstance().getExternalContext().dispatch(url);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.error(ioe);
		}
	}
	
	public static void accessDenied() {
		forward(getSectionForView().getPrefix() + "/accessDenied");
	}
	
	public static void notFound() {
		forward(getSectionForView().getPrefix() + "/notfound");
	}

	/**
	 * Forwards to not found page
	 * @param uri
	 */
	public static void notFound(String uri) {
		forward(getSectionForUri(uri).getPrefix() + "/notfound");
	}

	public static boolean isInManageSection() {
		String currentUrl = getRewriteURL();
		return currentUrl.contains("/manage/");
	}

}
