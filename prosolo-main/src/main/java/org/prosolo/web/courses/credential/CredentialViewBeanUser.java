package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialViewBean")
@Component("credentialViewBean")
@Scope("view")
public class CredentialViewBeanUser implements Serializable {

	private static final long serialVersionUID = 2225577288550403383L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;

	private String id;
	private long decodedId;
	private String mode;
	private boolean justEnrolled;
	
	private CredentialData credentialData;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();

	public void init() {	
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				if("preview".equals(mode)) {
					credentialData = credentialManager.getCredentialDataForEdit(decodedId, 
							loggedUser.getUser().getId(), true);
					ResourceCreator rc = new ResourceCreator();
					User user = loggedUser.getUser();
					rc.setFullName(user.getName(), user.getLastname());
					rc.setAvatarUrl(user.getAvatarUrl());
					credentialData.setCreator(rc);
				} else {
					credentialData = credentialManager.getFullTargetCredentialOrCredentialData(decodedId, 
							loggedUser.getUser().getId());
					if(justEnrolled) {
						PageUtil.fireSuccessfulInfoMessage("You have enrolled in the credential " + 
								credentialData.getTitle());
					}
				}
				if(credentialData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
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
		return credentialData == null || credentialData.getCreator() == null ? false : 
			credentialData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
 	public String getLabelForCredential() {
 		if(isPreview()) {
 			return "(Preview)";
 		} else if(isCurrentUserCreator() && !credentialData.isEnrolled() && !credentialData.isPublished()) {
 			return "(Draft)";
 		} else {
 			return "";
 		}
 	}
 	
	public boolean isPreview() {
		return "preview".equals(mode);
	}
	
	/*
	 * ACTIONS
	 */
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if(!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = new ArrayList<>();
			if(cd.isEnrolled()) {
				activities = activityManager.getTargetActivitiesData(cd.getTargetCompId());
			} else {
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			}
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}
	
	public void enrollInCredential() {
		try {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			CredentialData cd = credentialManager.enrollInCredential(decodedId, 
					loggedUser.getUser().getId(), lcd);
			credentialData = cd;
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	public void setupAssessmentRequestRecepient() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("assessmentRecipient");
		if(StringUtils.isNotBlank(id)){
			assessmentRequestData.setAssessorId(Long.valueOf(id));
		}
	}
	
	public void submitAssessment() {
		if(credentialData.isInstructorPresent()) {
			//there is instructor, set it from existing data (it was set from getFullTargetCredentialOrCredentialData)
			assessmentRequestData.setAssessorId(credentialData.getInstructorId());
		}
		//at this point, assessor should be set either from credential data or user-submitted peer id
		if(assessmentRequestData.isAssessorSet()) {
				populateAssessmentRequestFields();
				assessmentManager.requestAssessment(assessmentRequestData);
		}
		else {
			PageUtil.fireErrorMessage("No assessor set");
		}
	}

	private void populateAssessmentRequestFields() {
		assessmentRequestData.setStudentId(loggedUser.getUser().getId());
		assessmentRequestData.setCredentialId(credentialData.getId());
		assessmentRequestData.setTargetCredentialId(credentialData.getTargetCredId());
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isJustEnrolled() {
		return justEnrolled;
	}

	public void setJustEnrolled(boolean justEnrolled) {
		this.justEnrolled = justEnrolled;
	}

	public AssessmentRequestData getAssessmentRequestData() {
		return assessmentRequestData;
	}

	public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
		this.assessmentRequestData = assessmentRequestData;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}
	
}
