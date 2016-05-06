package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.nodes.Activity1Manager;
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
public class ActivityViewBean implements Serializable {

	private static final long serialVersionUID = -8910052333513137853L;

	private static Logger logger = Logger.getLogger(ActivityViewBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentBean commentBean;
	@Inject private UploadManager uploadManager;

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
					long creatorId = 0;
					if("preview".equals(mode)) {
						shouldReturnDraft = false;
						creatorId = loggedUser.getUser().getId();
					} 
					competenceData = activityManager
							.getCompetenceActivitiesWithSpecifiedActivityInFocus(decodedActId, 
									creatorId, 0, shouldReturnDraft);
				}
				if(competenceData == null) {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				} else {
					commentBean.init(CommentedResourceType.Activity, 
							competenceData.getActivityToShowWithDetails().getActivityId());
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
	
	public boolean isActivityActive(ActivityData act) {
		return competenceData.getActivityToShowWithDetails().getActivityId() == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUser().getId();
	}
	
	public boolean isPreview() {
		return "preview".equals(mode);
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
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("Error while uploading assignment");
		}
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
