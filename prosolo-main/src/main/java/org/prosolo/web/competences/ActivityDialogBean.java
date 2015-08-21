package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.competences.data.ActivityType;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic Nov 15, 2013
 */
@ManagedBean(name = "activityDialogBean")
@Component("activityDialogBean")
@Scope("view")
public class ActivityDialogBean implements Serializable {

	private static final long serialVersionUID = 3064854497830837831L;
	
	private static Logger logger = Logger.getLogger(ActivityDialogBean.class);
	
	@Autowired private UploadManager uploadManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private HTMLParser htmlParser;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private EventFactory eventFactory;

	private ActivityFormData activityFormData = new ActivityFormData();

	private ActivityType activityType = ActivityType.RESOURCE;
	
	
	public SelectItem[] getActivityTypes() {
		SelectItem[] items = new SelectItem[ActivityType.values().length];
		int i = 0;
		for (ActivityType actType : ActivityType.values()) {
			items[i++] = new SelectItem(actType.name(), actType.getLabel(), null, false);
		}
		return items;
	}
	
	public boolean isType(String type) {
		return activityType != null && activityType.name().equals(type);
	}

	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			this.activityFormData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}

	public void handleLink() {
		this.fetchLinkContents();
	}

	public void fetchLinkContents() {
		String linkString = this.activityFormData.getLink();

		if (linkString != null && linkString.length() > 0) {
			logger.debug("User " + loggedUser.getUser()	+ " is fetching contents of a link: " + linkString);

			AttachmentPreview attachmentPreview = htmlParser.parseUrl(StringUtil.cleanHtml(linkString.trim()));

			if (attachmentPreview != null) {
				this.activityFormData.setAttachmentPreview(attachmentPreview);
			} else {
				this.activityFormData.setLinkInvalid(true);
			}
		}
	}
	
	public void initActivityEdit(ActivityWallData activityData) {
		activityFormData = new ActivityFormData(activityData);
		this.activityType = activityData.getActivityType();
	}

	public void saveActivity() {
		try {
			// activity is edited
			Activity activity = null;
			if (activityFormData.getId() > 0) {
				
				 activity = activityManager.updateActivity(
						activityFormData.getId(),
						activityFormData.getTitle(),
						activityFormData.getDescription(),
						this.activityType, 
						activityFormData.isMandatory(), 
						activityFormData.getAttachmentPreview(), 
						activityFormData.getMaxNumberOfFiles(), 
						activityFormData.isVisibleToEveryone(), 
						activityFormData.getDuration(),
						loggedUser.getUser());
				 
				ManageCompetenceBean manageCompBean = PageUtil.getViewScopedBean("manageCompetenceBean", ManageCompetenceBean.class);
				
				if (manageCompBean != null) {
					manageCompBean.updateActivity(activity);
					PageUtil.fireSuccessfulInfoMessage("Activity added");
				}				 
			} 
			else {
				activityFormData.setType(this.activityType);
				 activity = resourceFactory.createNewActivity(
						loggedUser.getUser(), 
						activityFormData,
						VisibilityType.PUBLIC);
			
				ManageCompetenceBean manageCompBean = PageUtil.getViewScopedBean("manageCompetenceBean", ManageCompetenceBean.class);
				
				if (manageCompBean != null) {
					manageCompBean.addActivity(activity);
					PageUtil.fireSuccessfulInfoMessage("Activity added");
				}
			}
			eventFactory.generateEvent(EventType.Create_Manager, loggedUser.getUser(), activity);
			
			reset();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	public void reset() {
		activityFormData = new ActivityFormData();
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public ActivityFormData getActivityFormData() {
		return activityFormData;
	}

	public void setActivityFormData(ActivityFormData activityFormData) {
		this.activityFormData = activityFormData;
	}

	public String getActivityType() {
		return activityType.name();
	}

	public void setActivityType(String activityType) {
		this.activityType = ActivityType.valueOf(activityType);
	}
	
}
