package org.prosolo.web.lti;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.extract.LtiMessageExtractor;
import org.prosolo.web.lti.message.extract.LtiMessageExtractorFactory;
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
	private LtiToolManager toolManager;
	@Inject 
	private LtiUserManager userManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private AuthenticationService authenticationService;
	@Inject
	private CourseManager courseManager;

	public LTIProviderLaunchBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}

	// called when Tool Consumer submits request
	public void processPOSTRequest() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		try {
			LTILaunchMessage msg = validateRequest();
			launch(msg);
		} catch (Exception e) {
			redirectUser(externalContext, e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private void launch(LTILaunchMessage msg) throws Exception{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		LtiTool tool = getToolForLaunch(request, msg);
		User user = getUserForLaunch(tool, msg);
		boolean loggedIn = login(user);
		
		if(loggedIn){
			System.out.println("LOGGED IN");
			enrollUserIfNotEnrolled(user, tool.getLearningGoalId());
			String url = getUrlForRedirect(tool, user);
			System.out.println("URL "+url);
			//externalContext.redirect("index.xhtml");
			externalContext.redirect(url);
		}else{
			throw new Exception("User loggin unsuccessful");
		}
	}
	
	private void enrollUserIfNotEnrolled(User user, long courseId) throws RuntimeException{
		courseManager.enrollUserIfNotEnrolled(user, courseId);
	}

	private String getUrlForRedirect(LtiTool tool, User user) {
		return "learn.xhtml"+toolManager.getUrlParametersForLaunch(tool, user);
	}

	private void redirectUser(ExternalContext externalContext, String message) {
		String url = PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
		if (url != null) {
			String returnURL = formReturnURL(url, message);
			try {
				externalContext.redirect(returnURL);
			} catch (IOException ex) {
				logger.error(ex);
			}
		}
	}

	private boolean login(User user) throws AuthenticationException {
		boolean loggedIn = authenticationService.loginOpenId(user.getEmail().getAddress());
		if(loggedIn){
			System.out.println("LOGGED IN");
			loggedUserBean.init(user);
			return true;
		}
		return false;
	}
	
	private LtiTool getToolForLaunch(HttpServletRequest request, LTILaunchMessage msg) throws Exception{
		return toolManager.getLtiToolForLaunch(request, msg.getConsumerKey(), 
				getVersion(msg.getLtiVersion()), msg.getId());
	}
    
	private User getUserForLaunch(LtiTool tool, LTILaunchMessage msg) throws Exception{
		try{
			return userManager.getUserForLaunch(tool.getToolSet().getConsumer().getId(), msg.getUserID(), 
				msg.getUserFirstName(), msg.getUserLastName(), msg.getUserEmail(), tool.getLearningGoalId());
		}catch(Exception e){
			throw new Exception("User can not be found");
		}
	}

	private LtiVersion getVersion(String version){
		LtiVersion vers = null;
		if (LTIConstants.LTI_VERSION_ONE.equals(version)) {
			vers = LtiVersion.V1;
		} else {
			if (LTIConstants.LTI_VERSION_TWO.equals(version)) {
				vers = LtiVersion.V2;
			}
		}
		return vers;
	}

	private LTILaunchMessage validateRequest() throws Exception {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue()[0]);
		}
		if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
						.getMethod())) {
			throw new Exception("Not POST Request!");
		}
		LTILaunchMessage message = createLTILaunchMessage();
		//validatePostRequest(message, request);
		return message;
	}

	// validate Tool Launch request
	/*
	 * private void validateRequest() throws Exception { HttpServletRequest
	 * request = (HttpServletRequest)
	 * FacesContext.getCurrentInstance().getExternalContext().getRequest();
	 * Map<String, String []> map = request.getParameterMap(); for(Entry<String,
	 * String[]> entry:map.entrySet()){
	 * System.out.println(entry.getKey()+":"+entry.getValue()[0]); } if
	 * (!LTIConstants.POST_REQUEST.equalsIgnoreCase( ((HttpServletRequest)
	 * FacesContext.getCurrentInstance().getExternalContext().getRequest())
	 * .getMethod())) { throw new Exception("Not POST Request!"); }
	 * LTILaunchMessage message = createLTILaunchMessage(); String secret =
	 * getSharedSecret(message.getConsumerKey()); if(secret == null){ throw new
	 * Exception("Unknown Consumer"); } String url =
	 * Settings.getInstance().config.application.domain +
	 * LTIConstants.TOOL_LAUNCH_ENDPOINT; boolean valid =
	 * validatePostRequest(message, secret, request, url); logger.info("VALID: "
	 * + valid); if(!valid){ throw new Exception("Launch not valid!"); } }
	 */

	// create return url with query params
	private String formReturnURL(String url, String message) {
		Map<String, String> params = new HashMap<>();
		params.put(LTIConstants.PARAM_LTI_ERRORLOG, message);
		params.put(LTIConstants.PARAM_LTI_ERRORMSG, "Activity can not be started!");
		return Util.formURLWithParams(url, params);
	}

	// wrap POST parameters in LTILaunchMessage object
	private LTILaunchMessage createLTILaunchMessage() throws Exception {
		try{
			LtiMessageExtractor msgE = LtiMessageExtractorFactory.createMessageExtractor();
			LTILaunchMessage msg = (LTILaunchMessage) msgE.getLtiMessage();
			
			logger.info("Message type: " + msg.getMessageType());
			logger.info("LTI version: " + msg.getLtiVersion());
			
			logger.info("User ID: " + msg.getUserID());
			
			logger.info("Launch presentation Return URL: " + msg.getLaunchPresentationReturnURL());
			
			logger.info("User first name: " + msg.getUserFirstName());
			logger.info("User last name: " + msg.getUserLastName());
			logger.info("User email: " + msg.getUserEmail());
			logger.info("Oauth Consumer key: " + msg.getConsumerKey());
			return msg;
		}catch(Exception e){
			throw new Exception("Required parameter missing from launch");
		}
		/*
		 * logger.info("SVI PARAMETRI"); Map<String, String[]> map =
		 * request.getParameterMap(); for (Entry<String, String[]> entry :
		 * map.entrySet()) { logger.info(entry.getKey() + ":" +
		 * entry.getValue()[0]); } logger.info("KRAJ PARAMETARA");
		 */
		
		/*LTILaunchMessage msg = new LTILaunchMessage();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLtiVersion(PageUtil.getPostParameter(LTIConstants.LTI_VERSION));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setConsumerKey(PageUtil.getPostParameter(LTIConstants.OAUTH_CONSUMER_KEY));
		setVersionSpecificParameters(msg);*/
		//msg.setUserID(PageUtil.getPostParameter(LTIConstants.USER_ID));
		
		/*String roles = PageUtil.getPostParameter(LTIConstants.ROLES);
		if (roles != null) {
			if (roles.indexOf(",") != -1) {
				String[] parserdRoles = roles.split(",");
				for (String s : parserdRoles) {
					msg.getRoles().add(s);
				}
			} else {
				msg.getRoles().add(roles);
			}
		}*/

		
		//msg.setContextID(PageUtil.getPostParameter(LTIConstants.CONTEXT_ID));
		//msg.setContextType(PageUtil.getPostParameter(LTIConstants.CONTEXT_TYPE));
		//msg.setResourceLinkID(PageUtil.getPostParameter(LTIConstants.RESOURCE_LINK_ID));
		//msg.setToolConsumerInstanceGUID(PageUtil.getPostParameter(LTIConstants.TOOL_CONSUMER_INSTANCE_GUID));
		//msg.setContextTitle(PageUtil.getPostParameter(LTIConstants.CUSTOM_CONTEXT_TITLE));
		//msg.setResourceLinkTitle(PageUtil.getPostParameter(LTIConstants.CUSTOM_RESOURCE_LINK_TITLE));
		//msg.setUserFirstName(PageUtil.getPostParameter(LTIConstants.CUSTOM_LIS_PERSON_NAME_GIVEN));
		//msg.setUserFirstName(PageUtil.getPostParameter("custom_user_first_name"));
		//msg.setUserLastName(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_NAME_FAMILY));
		//msg.setUserLastName(PageUtil.getPostParameter("user_last_name"));
		//msg.setUserEmail(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY));
		//msg.setUserID(PageUtil.getPostParameter("custom_user_id"));
		//msg.setUserEmail(PageUtil.getPostParameter("custom_user_email"));
		//msg.setToolConsumerInstanceName(PageUtil.getPostParameter(LTIConstants.CUSTOM_TOOL_CONSUMER_INSTANCE_NAME));

		/*logger.info("Message type: " + msg.getMessageType());
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
		logger.info("Oauth Consumer key: " + msg.getConsumerKey());*/

	}

	/*public void validatePostRequest(LTILaunchMessage message, HttpServletRequest request) throws Exception {
		if (!LTIConstants.MESSAGE_TYPE_LTILAUNCH.equals(message.getMessageType())
				|| (!LTIConstants.LTI_VERSION_ONE.equals(message.getLtiVersion())
						&& !LTIConstants.LTI_VERSION_TWO.equals(message.getLtiVersion()))) {
			throw new Exception("Invalid launch");
		}

		String id = PageUtil.getPostParameter(LTIConstants.TOOL_ID);
		Validator validator = new NullValidator(new EmptyValidator(new LongValidator(null)));
		validator.performValidation(id, "Required parameter \"id\" missing or not properly formatted");
		toolId = Long.parseLong(id);

	}*/

	/*
	 * public boolean validatePostRequest(LTILaunchMessage message, String
	 * secret, HttpServletRequest request, String url) { try { if (secret !=
	 * null && !"".equals(secret)) { oAuthService.validatePostRequest(request,
	 * url, message.getConsumerKey(), secret); } if
	 * (!LTIConstants.MESSAGE_TYPE_LTILAUNCH.equals(message.getMessageType()) ||
	 * (!LTIConstants.LTI_VERSION_ONE.equals(message.getLtiVersion()) &&
	 * !LTIConstants.LTI_VERSION_TWO.equals(message.getLtiVersion()))) { return
	 * false; }
	 * 
	 * return true; } catch (Exception e) { return false; } }
	 */

	/*
	 * public void getTCProfile(String url) throws Exception{
	 * CloseableHttpClient httpClient = HttpClients.createDefault(); HttpGet
	 * getRequest = new HttpGet(
	 * "http://localhost/moodle/mod/lti/services.php/toolproxy/ij7Rfy75ZvZmmji/custom"
	 * ); // getRequest.addHeader(HttpHeaders.ACCEPT, //
	 * "application/vnd.ims.lti.v2.toolconsumerprofile+json");
	 * CloseableHttpResponse response = null; try { response =
	 * httpClient.execute(getRequest); HttpEntity entity = response.getEntity();
	 * String jsonString = EntityUtils.toString(entity); logger.info(
	 * "TC PROFILE RESPONSE: " + jsonString);
	 * 
	 * 
	 * } catch (Exception e) { logger.error(e); throw new Exception(
	 * "Error while getting Tool Consumer Profile"); } finally { try {
	 * response.close(); } catch (IOException e) { logger.error(e); } }
	 * 
	 * }
	 */

}
