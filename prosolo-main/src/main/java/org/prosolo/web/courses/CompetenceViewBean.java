package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceViewBean")
@Component("competenceViewBean")
@Scope("view")
public class CompetenceViewBean implements Serializable {

	private static final long serialVersionUID = 9208762722353804216L;

	private static Logger logger = Logger.getLogger(CompetenceViewBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;

	private String id;
	private long decodedId;
	private String targetId;
	private long decodedTargetId;
	private String mode;
	
	private CompetenceData1 competenceData;

	public void init() {	
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				decodedTargetId = idEncoder.decodeId(targetId);
				if(decodedTargetId > 0) {
					competenceData = competenceManager.getTargetCompetenceData(decodedTargetId, true, true);
					competenceData.determineIfStartedWorkingOnCompetence();
					competenceData.determineActivityFromWhichToStartLearning();
				} else {
					if("preview".equals(mode)) {
						competenceData = competenceManager.getCompetenceDataForEdit(decodedId, 
								loggedUser.getUser().getId(), true);
						ResourceCreator rc = new ResourceCreator();
						User user = loggedUser.getUser();
						rc.setFullName(user.getName(), user.getLastname());
						rc.setAvatarUrl(user.getAvatarUrl());
						competenceData.setCreator(rc);
					} else {
						competenceData = competenceManager.getCompetenceData(decodedId, true, true, 
								true, true);
					}
				}
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					commentBean.init(CommentedResourceType.Competence, competenceData.getCompetenceId());
				}
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
		}
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	public String getIdOfResumeFromActivity() {
		return idEncoder.encodeId(competenceData.getResumeFromId());
	}
	
	public boolean isPreview() {
		return "preview".equals(mode);
	}

	/*
	 * ACTIONS
	 */
	
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public long getDecodedTargetId() {
		return decodedTargetId;
	}

	public void setDecodedTargetId(long decodedTargetId) {
		this.decodedTargetId = decodedTargetId;
	}

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
