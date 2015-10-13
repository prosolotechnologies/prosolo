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
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.ToolSetManager;
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
	@Inject private LoggedUserBean user;

	private long cred;
	private long comp;
	private long act;
	
	private long id;
	private ExternalToolFormData toolData;
	
	public void init() {
		logger.info("User with email "+user.getUser().getEmail().getAddress()+" redirected to the page manage/externalTools/toolDetails.xhtml");
		if (id > 0) {
			LtiTool tool = toolManager.getToolDetails(id);
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
		if(id > 0){
			tool.setId(id);
			try{
				toolManager.updateLtiTool(tool);
				logger.info("LTI tool updated");
				PageUtil.fireSuccessfulInfoMessage("External tool updated");
			}catch(Exception e){
				PageUtil.fireErrorMessage(e.getMessage());
			}
		}else{
			if(cred > 0){
				tool.setLearningGoalId(cred);
				tool.setCompetenceId(comp);
				tool.setActivityId(act);
				tool.setCreatedBy(user.getUser());
				try{
					LtiToolSet ts = tsManager.saveToolSet(tool);
					logger.info("LTI tool saved");
					toolData.setConsumerKey(ts.getConsumer().getKeyLtiOne());
					toolData.setConsumerSecret(ts.getConsumer().getSecretLtiOne());
					Iterator<LtiTool> it = ts.getTools().iterator();
					tool = it.next();
					toolData.setLaunchUrl(tool.getFullLaunchURL());
					toolData.setRegUrl(ts.getFullRegistrationURL());
					PageUtil.fireSuccessfulInfoMessage("External tool saved");
				}catch(Exception e){
					PageUtil.fireErrorMessage(e.getMessage());
				}
			}
		}
		logger.debug("Saving external tool");
	}
	
	private ResourceType getResourceType() {
		ResourceType type = null;
		if(act > 0){
			type = ResourceType.Activity;
		}else{
			if(comp > 0){
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
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public long getCred() {
		return cred;
	}

	public void setCred(long res) {
		this.cred = res;
	}

	public long getComp() {
		return comp;
	}

	public void setComp(long comp) {
		this.comp = comp;
	}

	public long getAct() {
		return act;
	}

	public void setAct(long act) {
		this.act = act;
	}

	/*
	 * GETTERS / SETTERS
	 */
	public ExternalToolFormData getToolData() {
		return toolData;
	}

	
}
