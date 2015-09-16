package org.prosolo.web.lti;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.services.oauth.OauthService;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "ltiproviderlaunchbean")
@Component("ltiproviderlaunchbean")
@Scope("request")
public class LTIProviderLaunchBean implements Serializable {

	private static final long serialVersionUID = 2181430839104491540L;

	private static Logger logger = Logger.getLogger(LTIProviderLaunchBean.class);

	@Inject
	private OauthService oAuthService;

	public LTIProviderLaunchBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}
	//called when Tool Consumer submits request
	public void processPOSTRequest() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		try {
			validateRequest();
			externalContext.redirect(LTIConstants.TOOL_URL);
		} catch (Exception e) {
			String url = PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
			if (url != null) {
				String returnURL = formReturnURL(url, e.getMessage());
				try {
					externalContext.redirect(returnURL);
				} catch (IOException ex) {
					logger.error(e);
				}
			}
			logger.error(e);
		}
	}
	//validate Tool Launch request
	private void validateRequest() throws Exception {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
						.getMethod())) {
			throw new Exception("Not POST Request!");
		}
		LTILaunchMessage message = createLTILaunchMessage();
		String secret = getSharedSecret(message.getConsumerKey());
		if(secret == null){
			throw new Exception("Unknown Consumer");
		}
		String url = Settings.getInstance().config.application.domain + LTIConstants.TOOL_LAUNCH_ENDPOINT;
		boolean valid = validatePostRequest(message, secret, request, url);
		logger.info("VALID: " + valid);
		if(!valid){
			throw new Exception("Launch not valid!");
		}
	}
	//get shared secret for consumer key (from db)
	private String getSharedSecret(String consumerKey) {
		return "6f735b26-677e-43df-b239-66a64fe655d6";
	}
	//create return url with query params
	private String formReturnURL(String url, String message) {
		Map<String, String> params = new HashMap<>();
		params.put(LTIConstants.PARAM_LTI_ERRORLOG, message);
		params.put(LTIConstants.PARAM_LTI_ERRORMSG, "Activity can not be started!");
		return Util.formURLWithParams(url, params);
	}
	//wrap POST parameters in LTILaunchMessage object
	private LTILaunchMessage createLTILaunchMessage() {
		/*
		 * logger.info("SVI PARAMETRI"); Map<String, String[]> map =
		 * request.getParameterMap(); for (Entry<String, String[]> entry :
		 * map.entrySet()) { logger.info(entry.getKey() + ":" +
		 * entry.getValue()[0]); } logger.info("KRAJ PARAMETARA");
		 */
		LTILaunchMessage msg = new LTILaunchMessage();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLtiVersion(PageUtil.getPostParameter(LTIConstants.LTI_VERSION));
		msg.setUserID(PageUtil.getPostParameter(LTIConstants.USER_ID));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		String roles = PageUtil.getPostParameter(LTIConstants.ROLES);
		if (roles != null) {
			if (roles.indexOf(",") != -1) {
				String[] parserdRoles = roles.split(",");
				for (String s : parserdRoles) {
					msg.getRoles().add(s);
				}
			} else {
				msg.getRoles().add(roles);
			}
		}

		msg.setConsumerKey(PageUtil.getPostParameter(LTIConstants.OAUTH_CONSUMER_KEY));
		msg.setContextID(PageUtil.getPostParameter(LTIConstants.CONTEXT_ID));
		msg.setContextType(PageUtil.getPostParameter(LTIConstants.CONTEXT_TYPE));
		msg.setResourceLinkID(PageUtil.getPostParameter(LTIConstants.RESOURCE_LINK_ID));
		msg.setToolConsumerInstanceGUID(PageUtil.getPostParameter(LTIConstants.TOOL_CONSUMER_INSTANCE_GUID));
		msg.setContextTitle(PageUtil.getPostParameter(LTIConstants.CUSTOM_CONTEXT_TITLE));
		msg.setResourceLinkTitle(PageUtil.getPostParameter(LTIConstants.CUSTOM_RESOURCE_LINK_TITLE));
		msg.setUserFirstName(PageUtil.getPostParameter(LTIConstants.CUSTOM_LIS_PERSON_NAME_GIVEN));
		msg.setUserLastName(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_NAME_FAMILY));
		msg.setUserEmail(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY));
		msg.setToolConsumerInstanceName(PageUtil.getPostParameter(LTIConstants.CUSTOM_TOOL_CONSUMER_INSTANCE_NAME));

		logger.info("Message type: " + msg.getMessageType());
		logger.info("LTI version: " + msg.getLtiVersion());
		logger.info("Resource link id: " + msg.getResourceLinkID());
		logger.info("Resource link title: " + msg.getResourceLinkTitle());
		logger.info("User ID: " + msg.getUserID());
		logger.info("Roles: " + msg.getRoles());
		logger.info("Context ID: " + msg.getContextID());
		logger.info("Context Title: " + msg.getContextTitle());
		logger.info("Launch presentation Return URL: " + msg.getLaunchPresentationReturnURL());
		logger.info("Tool Consumer Instance name: " + msg.getToolConsumerInstanceName());
		logger.info("User first name: " + msg.getUserFirstName());
		logger.info("User last name: " + msg.getUserLastName());
		logger.info("User email: " + msg.getUserEmail());
		logger.info("Oauth Consumer key: " + msg.getConsumerKey());

		return msg;
	}
	
	public boolean validatePostRequest(LTILaunchMessage message, String secret, HttpServletRequest request,
			String url) {
		try {
			if (secret != null && !"".equals(secret)) {
				oAuthService.validatePostRequest(request, url, message.getConsumerKey(), secret);
			}
			if (!LTIConstants.MESSAGE_TYPE_LTILAUNCH.equals(message.getMessageType())
					|| (!LTIConstants.LTI_VERSION_ONE.equals(message.getLtiVersion())
							&& !LTIConstants.LTI_VERSION_TWO.equals(message.getLtiVersion()))) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
