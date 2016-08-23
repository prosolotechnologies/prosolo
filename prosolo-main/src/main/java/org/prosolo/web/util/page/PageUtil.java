package org.prosolo.web.util.page;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.web.util.ResourceBundleUtil;

public class PageUtil {
	
	protected static Logger logger = Logger.getLogger(PageUtil.class);

	public static String getPostParameter(String parameterName) {
		Map<String, String> contextParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return contextParameters.get(parameterName);
	}
	
	public static void fireSuccessfulInfoMessage(String description) {
		fireSuccessfulInfoMessage(null, description);
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
		fireInfoMessage(clientId, "Successful", description); 
	} 

	public static void fireInfoMessage(String clientId, String title, String description) {
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
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("login?faces-redirect=true");
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
		String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
		/*
		 * find section by returning viewId substring from the beginning to the second
		 * occurrence of '/' character. That is because viewId always starts with 
		 * "/section/page" (if there is a section)
		 */
		int secondSlashIndex = StringUtils.ordinalIndexOf(viewId, "/", 2);
		String section = "";
		if (secondSlashIndex != -1) {
			section = viewId.substring(0, secondSlashIndex);
		}
		
		if (section.equals(PageSection.ADMIN.getPrefix())) {
			return PageSection.ADMIN;
		} else if (section.equals(PageSection.MANAGE.getPrefix())) {
			return PageSection.MANAGE;
		} else {
			return PageSection.STUDENT;
		}
	}
}
