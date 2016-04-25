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
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.lti.LtiToolLaunchValidator;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.extract.LtiMessageBuilder;
import org.prosolo.web.lti.message.extract.LtiMessageBuilderFactory;
import org.prosolo.web.lti.urlbuilder.ToolLaunchUrlBuilderFactory;
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
	@Inject
	private LtiToolLaunchValidator toolLaunchValidator;
	@Inject
	private ApplicationBean applicationBean;

	public LTIProviderLaunchBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}

	// called when Tool Consumer submits request
	public void processPOSTRequest() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		try {
			logger.info("New Lti Launch");
			LTILaunchMessage msg = validateRequest();
			logger.info("Launch request valid");
			launch(msg);
		} catch (Exception e) {
			redirectUser(externalContext, e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private void launch(LTILaunchMessage msg) throws Exception{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session=(HttpSession) externalContext.getSession(false);
		//if there is a different user logged in in same browser, we must invalidate his session first or exception will be thrown
		applicationBean.unregisterSession(session);
		
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		LtiTool tool = toolManager.getLtiToolForLaunch(msg.getId());
		toolLaunchValidator.validateLaunch(tool, msg.getConsumerKey(), getVersion(msg.getLtiVersion()), request);
		logger.info("Tool launch valid, tool id: "+tool.getId());
		User user = getUserForLaunch(tool, msg);
		logger.info("User for LTI launch logged in, user email "+user.getEmail());
		boolean loggedIn = login(user);
		
		if(loggedIn) {
			String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
			courseManager.enrollUserIfNotEnrolled(user, tool.getLearningGoalId(), page, "name:lti_launch|context:/name:lti_tool|id:" + msg.getId() + "/", null);
			String url = ToolLaunchUrlBuilderFactory.getLaunchUrlBuilder(tool.getToolType()).
					getLaunchUrl(tool, user.getId());
			logger.info("Redirecting to "+url);
			//externalContext.redirect(externalContext.getRequestContextPath() + "/index.xhtml");
			externalContext.redirect(externalContext.getRequestContextPath() + "/" + url);
		} else {
			throw new Exception("User loggin unsuccessful");
		}
	}
	

	private void redirectUser(ExternalContext externalContext, String message) {
		String url = PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
		if (url != null) {
			String returnURL = buildReturnURL(url, message);
			try {
				logger.info("Redirecting to "+returnURL);
				externalContext.redirect(returnURL);
			} catch (IOException ex) {
				logger.error(ex);
			}
		}else{
			logger.info("LTI consumer did not send return url to redirect user back");
		}
	}

	private boolean login(User user) throws AuthenticationException {
		boolean loggedIn = authenticationService.loginOpenId(user.getEmail());
		if(loggedIn){
			loggedUserBean.init(user.getEmail());
			return true;
		}
		return false;
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
		if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
						.getMethod())) {
			throw new Exception("Not POST Request!");
		}
		LTILaunchMessage message = createLTILaunchMessage();
		//validatePostRequest(message, request);
		return message;
	}

	// create return url with query params
	private String buildReturnURL(String url, String message) {
		Map<String, String> params = new HashMap<>();
		params.put(LTIConstants.PARAM_LTI_ERRORLOG, message);
		params.put(LTIConstants.PARAM_LTI_ERRORMSG, "Activity can not be started!");
		return Util.buildURLWithParams(url, params);
	}

	// wrap POST parameters in LTILaunchMessage object
	private LTILaunchMessage createLTILaunchMessage() throws Exception {
		try{
			LtiMessageBuilder msgE = LtiMessageBuilderFactory.createMessageExtractor();
			LTILaunchMessage msg = (LTILaunchMessage) msgE.getLtiMessage();
			return msg;
		}catch(Exception e){
			throw new Exception("Required parameter missing from launch");
		}

	}

}
