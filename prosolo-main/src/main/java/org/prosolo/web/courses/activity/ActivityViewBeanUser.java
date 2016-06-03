package org.prosolo.web.courses.activity;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityViewBean")
@Component("activityViewBean")
@Scope("view")
public class ActivityViewBeanUser implements Serializable {

	private static final long serialVersionUID = -8910052333513137853L;

	private static Logger logger = Logger.getLogger(ActivityViewBeanUser.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private UploadManager uploadManager;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;

	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;
	private String mode;
	
	private CompetenceData1 competenceData;

	public void init() {	
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedActId > 0 && decodedCompId > 0) {
			try {
				decodedCredId = idEncoder.decodeId(credId);
				if(decodedCredId > 0) {
					competenceData = activityManager
							.getFullTargetActivityOrActivityData(decodedCredId,
									decodedCompId, decodedActId, loggedUser.getUser().getId());
				} else {
					boolean shouldReturnDraft = false;
					if("preview".equals(mode)) {
						shouldReturnDraft = true;
					} 
					competenceData = activityManager
							.getCompetenceActivitiesWithSpecifiedActivityInFocusForUser(
									decodedActId,  loggedUser.getUser().getId(), shouldReturnDraft);
				}
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					commentBean.init(CommentedResourceType.Activity, 
							competenceData.getActivityToShowWithDetails().getActivityId(), false);
					
					loadCompetenceAndCredentialTitle();
				}
			} catch(Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading activity");
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
	
	private void loadCompetenceAndCredentialTitle() {
		String compTitle = null;
		String credTitle = null;
		decodedCredId = idEncoder.decodeId(credId);
		if(competenceData.getActivityToShowWithDetails().isEnrolled()) {
			compTitle = compManager.getTargetCompetenceTitle(competenceData
					.getActivityToShowWithDetails().getCompetenceId());
			if(decodedCredId > 0) {
				credTitle = credManager.getTargetCredentialTitle(decodedCredId, loggedUser
						.getUser().getId());
			}
		} else {
			compTitle = compManager.getCompetenceTitle(decodedCompId);
			if(decodedCredId > 0) {
				credTitle = credManager.getCredentialTitle(decodedCredId);
			}
		}
		competenceData.setTitle(compTitle);
		competenceData.setCredentialId(decodedCredId);
		competenceData.setCredentialTitle(credTitle);
		
	}
	
	public boolean isActivityActive(ActivityData act) {
		return competenceData.getActivityToShowWithDetails().getActivityId() == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData.getActivityToShowWithDetails().getCreatorId() == loggedUser.getUser().getId();
	}
	
	public String getLabelForActivity() {
 		if(isPreview()) {
 			return "(Preview)";
 		} else if(isCurrentUserCreator() && !competenceData.getActivityToShowWithDetails().isEnrolled() 
 				&& !competenceData.getActivityToShowWithDetails().isPublished()) {
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
	
	public void completeActivity() {
		try {
			activityManager.completeActivity(
					competenceData.getActivityToShowWithDetails().getTargetActivityId(), 
					competenceData.getActivityToShowWithDetails().getCompetenceId(), 
					decodedCredId, 
					loggedUser.getUser().getId());
			competenceData.getActivityToShowWithDetails().setCompleted(true);
			for(ActivityData ad : competenceData.getActivities()) {
				if(ad.getActivityId() == competenceData.getActivityToShowWithDetails().getActivityId()) {
					ad.setCompleted(true);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while marking activity as completed");
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		
		try {
			String fileName = uploadedFile.getFileName();
			String fullPath = uploadManager.storeFile(null, uploadedFile, fileName);
			activityManager.saveAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId(), fileName, fullPath);
			competenceData.getActivityToShowWithDetails().setAssignmentTitle(fileName);
			competenceData.getActivityToShowWithDetails().setAssignmentLink(fullPath);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while uploading assignment");
		}
	}
	
	public void deleteAssignment() {
		try {
			activityManager.deleteAssignment(competenceData.getActivityToShowWithDetails()
					.getTargetActivityId());
			competenceData.getActivityToShowWithDetails().setAssignmentTitle(null);
			competenceData.getActivityToShowWithDetails().setAssignmentLink(null);
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

	public String getActId() {
		return actId;
	}

	public void setActId(String actId) {
		this.actId = actId;
	}

	public long getDecodedActId() {
		return decodedActId;
	}

	public void setDecodedActId(long decodedActId) {
		this.decodedActId = decodedActId;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public void setDecodedCompId(long decodedCompId) {
		this.decodedCompId = decodedCompId;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public void setDecodedCredId(long decodedCredId) {
		this.decodedCredId = decodedCredId;
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
