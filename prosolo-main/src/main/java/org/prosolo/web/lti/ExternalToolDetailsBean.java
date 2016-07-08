/**
 * 
 */
package org.prosolo.web.lti;

import java.io.Serializable;
import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.ToolSetManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.lti.data.ExternalToolFormData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;


/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "extenalToolDetailsBean")
@Component("extenalToolDetailsBean")
@Scope("view")
public class ExternalToolDetailsBean implements Serializable {

	private static final long serialVersionUID = 6383363883663936346L;

	private static Logger logger = Logger.getLogger(ExternalToolDetailsBean.class);
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Inject private LtiToolManager toolManager;
	@Inject private ToolSetManager tsManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private DefaultManager defaultManager;

	private String cred;
	private String comp;
	private String act;
	private String id;
	
	private long decodedCred;
	private long decodedComp;
	private long decodedAct;
	private long decodedId;
	
	private ExternalToolFormData toolData;
	
	public void init() {
		logger.info("User with email " + loggedUserBean.getSessionData().getEmail() +" redirected to the page manage/externalTools/toolDetails.xhtml");
		decodedCred = idEncoder.decodeId(cred);
		decodedComp = idEncoder.decodeId(comp);
		decodedAct = idEncoder.decodeId(act);
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			LtiTool tool = toolManager.getToolDetails(decodedId);
			toolData = new ExternalToolFormData(tool);
			logger.debug("Editing external tool with id " + id);
		} else {
			toolData = new ExternalToolFormData();
			logger.debug("Creating new external tool");
		}
	}
	
	public void save() {
		LtiTool tool = new LtiTool();
		tool.setToolType(getResourceType());
		tool.setName(toolData.getTitle());
		tool.setDescription(toolData.getDescription());
		
		if (decodedId > 0) {
			tool.setId(decodedId);
			
			try {
				toolManager.updateLtiTool(tool);
				logger.info("LTI tool updated");
				PageUtil.fireSuccessfulInfoMessage("External tool updated");
			} catch (Exception e) {
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			if (decodedCred > 0) {
				try {
					User user = defaultManager.loadResource(User.class, loggedUserBean.getUserId());
				
					tool.setCreatedBy(user);
					tool.setLearningGoalId(decodedCred);
					tool.setCompetenceId(decodedComp);
					tool.setActivityId(decodedAct);
					
					try {
						LtiToolSet ts = tsManager.saveToolSet(tool);
						logger.info("LTI tool saved");
						toolData.setConsumerKey(ts.getConsumer().getKeyLtiOne());
						toolData.setConsumerSecret(ts.getConsumer().getSecretLtiOne());
						Iterator<LtiTool> it = ts.getTools().iterator();
						tool = it.next();
						toolData.setLaunchUrl(tool.getFullLaunchURL());
						toolData.setRegUrl(ts.getFullRegistrationURL());
						PageUtil.fireSuccessfulInfoMessage("External tool saved");
						toolData.setInitialized(true);
					} catch (Exception e) {
						PageUtil.fireErrorMessage(e.getMessage());
					}
				} catch (ResourceCouldNotBeLoadedException e1) {
					logger.error(e1);
				}
			}
		}
		logger.debug("Saving external tool");
	}
	
	private ResourceType getResourceType() {
		ResourceType type = null;
		if(decodedAct > 0){
			type = ResourceType.Activity;
		}else{
			if(decodedComp > 0){
				type = ResourceType.Competence;
			}else{
				type = ResourceType.Credential;
			}
		}
		return type;
	}

	/*
	 * PARAMETERS
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getCred() {
		return cred;
	}

	public void setCred(String res) {
		this.cred = res;
	}

	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	/*
	 * GETTERS / SETTERS
	 */
	public ExternalToolFormData getToolData() {
		return toolData;
	}

	
}
