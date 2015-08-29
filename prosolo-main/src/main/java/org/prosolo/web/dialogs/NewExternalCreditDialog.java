package org.prosolo.web.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.nodes.ActivityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "newExternalCreditBean")
@Component("newExternalCreditBean")
@Scope("view")
public class NewExternalCreditDialog implements Serializable {

	private static final long serialVersionUID = 5779526184542754212L;

	protected static Logger logger = Logger.getLogger(NewExternalCreditDialog.class);
	
	@Autowired private UploadManager uploadManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private PostManager postManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private PortfolioBean portfolioBean;
	
	private ExternalCreditData newCredit = new ExternalCreditData();
	private NewPostData newActivityData = new NewPostData();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	//add new activity
	public void createActivity() {
		ActivityWallData newAct = portfolioBean.createNewActivity(this.newActivityData);
		
		if (newAct != null) {
			newCredit.addActivity(newAct);
			this.newActivityData = new NewPostData();
		}
	}
	
	// Called from the activity search 
	public void connectActivity(Activity activity) {
		ActivityWallData newAct = portfolioBean.addActivity(activity);
		
		if (newAct != null) {
			newCredit.addActivity(newAct);
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newActivityData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	// used for activity search
	public String getAllActivitiesIds() {
		return ActivityUtil.extractActivityIds(newCredit.getActivities());
	}
	
	private UploadedFile certificateToUpload = null;
	
	public void uploadCertificate(FileUploadEvent event) {
		certificateToUpload = event.getFile();
	}
	
	public void saveNewExternalCredit() {
		try {
			String certificateLink = null;
			
			if (certificateToUpload != null) {
				certificateLink = uploadManager.storeFile(
						loggedUser.getUser(), 
						certificateToUpload, 
						certificateToUpload.getFileName());
				
				this.newCredit.setCertificateLink(certificateLink);
			}
			
			List<TargetActivity> activities = new ArrayList<TargetActivity>();
			
			for (ActivityWallData actWall : newCredit.getActivities()) {
				try {
					TargetActivity activity = activityManager.loadResource(TargetActivity.class, actWall.getObject().getId());
					activities.add(activity);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		
			ExternalCredit newExternalCredit = portfolioManager.createExternalCredit(
					loggedUser.refreshUser(), 
					newCredit.getTitle(), 
					newCredit.getDescription(), 
					certificateLink, 
					newCredit.getStart(), 
					newCredit.getEnd(), 
					activities, 
					newCredit.getCompetences(),
					"profile.credits");
			
			newCredit.setVisibility(VisibilityType.PUBLIC);
			
			newCredit.setExternalCredit(newExternalCredit);
			
			portfolioBean.getExternalCredits().add(newCredit);
			
			reset();
			
			PageUtil.fireSuccessfulInfoMessage("New external credit added!");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	/*
	 * Invoked from the added competence search 
	 */
	public void connectCompetence(Competence comp) {
		newCredit.addCompetence(comp);
	}
	
	public void reset() {
		this.newActivityData = new NewPostData();
		this.newCredit = new ExternalCreditData();
		this.certificateToUpload = null;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public ExternalCreditData getNewCredit() {
		return newCredit;
	}

	public void setNewCredit(ExternalCreditData newCredit) {
		this.newCredit = newCredit;
	}

	public NewPostData getNewActivityData() {
		return newActivityData;
	}

	public void setNewActivityData(NewPostData newActivityData) {
		this.newActivityData = newActivityData;
	}

	public UploadedFile getCertificateToUpload() {
		return certificateToUpload;
	}

	public void setCertificateToUpload(UploadedFile certificateToUpload) {
		this.certificateToUpload = certificateToUpload;
	}
	
}
