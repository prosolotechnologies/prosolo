package org.prosolo.web.lti;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.lti.LtiToolLaunchValidator;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.extract.LtiMessageBuilder;
import org.prosolo.web.lti.message.extract.LtiMessageBuilderFactory;
import org.prosolo.web.lti.urlbuilder.ToolLaunchUrlBuilderFactory;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ManagedBean(name = "ltiproviderlaunchbean")
@Component("ltiproviderlaunchbean")
@Scope("request")
public class LTIProviderLaunchBean implements Serializable {

	private static final long serialVersionUID = 2181430839104491540L;

	private static Logger logger = Logger.getLogger(LTIProviderLaunchBean.class);

	@Inject
	private LtiToolManager toolManager;
	@Inject 
	private LtiUserManager ltiUserManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private AuthenticationService authenticationService;
	@Inject
	private LtiToolLaunchValidator toolLaunchValidator;
	@Inject
	private ApplicationBean applicationBean;

	public LTIProviderLaunchBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}

	private String learningContext = "name:lti_launch";

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
	
	private void launch(LTILaunchMessage msg) throws Exception {
		logger.info("LTI provider launch: " + new Gson().toJson(msg));

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		LtiTool tool = toolManager.getToolDetails(msg.getId());

		// validating the tool, the method will throw an exception if not valid
		toolLaunchValidator.validateLaunch(tool, msg.getConsumerKey(), getVersion(msg.getLtiVersion()), (HttpServletRequest) externalContext.getRequest());
		logger.info("Tool launch valid, tool id: "+tool.getId());

		// fetching or creating a user
		User user = getUserForLaunch(tool, msg);

		boolean loggedIn = login(user);

		if (loggedIn) {
			logger.info("User for LTI launch logged in, user email " + user.getEmail());

			String url = ToolLaunchUrlBuilderFactory.getLaunchUrlBuilder(tool.getToolType()).getLaunchUrl(tool, user.getId(), user.getOrganization().getId());

			logger.info("Redirecting user to "+url);
			PageUtil.redirect(url);
		} else {
			throw new Exception("User login unsuccessful");
		}
	}
	

	private void redirectUser(ExternalContext externalContext, String message) {
		String url = PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
		if (url != null) {
			String returnURL = buildReturnURL(url, message);
			logger.info("Redirecting to "+returnURL);
			PageUtil.redirect(returnURL);
		} else {
			logger.info("LTI consumer did not send return url to redirect user back");
		}
	}

	private boolean login(User user) {
		//if there is a different user logged in, invalidate his session
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		applicationBean.unregisterSession(session);
		return loggedUserBean.loginUser(user.getEmail(), learningContext);
	}


	private User getUserForLaunch(LtiTool tool, LTILaunchMessage msg) throws Exception {
		try {
			UserContextData contextData = UserContextData.ofOrganization(tool.getOrganization().getId());

			// get role from the LTI message if present
			String roles = msg.getRoles();	// it more roles are present, fetch only the first one (for now)
			String roleName = roles != null ? roles.split(",")[0] : SystemRoleNames.USER;
			return ltiUserManager.getUserForLaunch(
					tool.getToolSet().getConsumer().getId(),
					msg.getUserID(),
					msg.getUserFirstName(),
					msg.getUserLastName(),
					msg.getUserEmail(),
					tool.getUnit() != null ? tool.getUnit().getId() : 0,
					LTIToProSoloRoleMapper.getRole(roleName),
					tool.getUserGroup() != null ? tool.getUserGroup().getId() : 0,
					contextData);
		} catch (Exception e) {
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
