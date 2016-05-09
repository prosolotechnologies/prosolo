package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityEditBean")
@Component("activityEditBean")
@Scope("view")
public class ActivityEditBean implements Serializable {

	private static final long serialVersionUID = 7678126570859694510L;

	private static Logger logger = Logger.getLogger(ActivityEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Competence1Manager compManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UploadManager uploadManager;

	private String id;
	private String compId;
	private long decodedId;
	private long decodedCompId;
	
	private ActivityData activityData;
	private String competenceName;
	
	private ActivityType[] activityTypes;
	
	private String linkToAdd;
	
	private PublishedStatus[] actStatusArray;
	
	public void init() {
		initializeValues();
		if(id == null) {
			activityData = new ActivityData(false);
			if(compId != null) {
				decodedCompId = idEncoder.decodeId(compId);
				activityData.setCompetenceId(decodedCompId);
			}
		} else {
			try {
				decodedId = idEncoder.decodeId(id);
				logger.info("Editing activity with id " + decodedId);
				loadActivityData(decodedId);
			} catch(Exception e) {
				logger.error(e);
				activityData = new ActivityData(false);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		}
		
		loadCompetenceName();
	}
	
	private void initializeValues() {
		activityTypes = ActivityType.values();
		actStatusArray = PublishedStatus.values();
	}

	private void loadCompetenceName() {
		if(activityData.getCompetenceId() > 0) {
			competenceName = compManager.getCompetenceTitle(activityData.getCompetenceId());
			activityData.setCompetenceName(competenceName);
		}
	}

	private void loadActivityData(long id) {
		activityData = activityManager.getActivityDataForEdit(id, loggedUser.getUser().getId());
		
		if(activityData == null) {
			activityData = new ActivityData(false);
			PageUtil.fireErrorMessage("Activity data can not be found");
		}
		
		logger.info("Loaded activity data for activity with id "+ id);
	}
	
	public boolean isLinkListEmpty() {
		List<ResourceLinkData> links = activityData.getLinks();
		if(links == null || links.isEmpty()) {
			return true;
		}
		for(ResourceLinkData rl : links) {
			if(rl.getStatus() != ObjectStatus.REMOVED) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isFileListEmpty() {
		List<ResourceLinkData> files = activityData.getFiles();
		if(files == null || files.isEmpty()) {
			return true;
		}
		for(ResourceLinkData rl : files) {
			if(rl.getStatus() != ObjectStatus.REMOVED) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isActivityTypeSelected(ActivityType type) {
		return type == activityData.getActivityType();
	}
	
	public void updateType(ActivityType type) {
		activityData.setActivityType(type);
	}
	
	public void removeFile(ResourceLinkData link) {
		link.statusRemoveTransition();
		if(link.getStatus() != ObjectStatus.REMOVED) {
			activityData.getFiles().remove(link);
		}
	}
	
	public void removeLink(ResourceLinkData link) {
		link.statusRemoveTransition();
		if(link.getStatus() != ObjectStatus.REMOVED) {
			activityData.getLinks().remove(link);
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		
		try {
			String fileName = uploadedFile.getFileName();
			String fullPath = uploadManager.storeFile(null, uploadedFile, fileName);
			ResourceLinkData rl = new ResourceLinkData();
			rl.setUrl(fullPath);
			rl.setLinkName(fileName);
			rl.setStatus(ObjectStatus.CREATED);
			activityData.getFiles().add(rl);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
	}
	
	public void addLink() {
		ResourceLinkData rl = new ResourceLinkData();
		rl.setUrl(linkToAdd);
		rl.setStatus(ObjectStatus.CREATED);
		activityData.getLinks().add(rl);
		linkToAdd = "";
	}
	
	public boolean isCreateUseCaseOrFirstTimeDraft() {
		return activityData.getActivityId() == 0 || 
				(!activityData.isPublished() && !activityData.isDraft());
	}
	
	/*
	 * ACTIONS
	 */
	
	public void preview() {
		saveActivityData(true, true);
	}
	
	public void save() {
		boolean isNew = activityData.getActivityId() == 0;
		boolean saved = saveActivityData(false, !isNew);
		if(saved && isNew) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extContext.redirect(extContext.getRequestContextPath() + 
						"/competences/" + compId + "/edit" );
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean saveActivityData(boolean saveAsDraft, boolean reloadData) {
		try {
			if(activityData.getActivityId() > 0) {
				if(activityData.hasObjectChanged()) {
					if(saveAsDraft) {
						activityData.setStatus(PublishedStatus.DRAFT);
					}
					activityManager.updateActivity(activityData, 
							loggedUser.getUser().getId());
				}
			} else {
				if(saveAsDraft) {
					activityData.setStatus(PublishedStatus.DRAFT);
				}
				Activity1 act = activityManager.saveNewActivity(activityData, 
						loggedUser.getUser().getId());
				decodedId = act.getId();
				id = idEncoder.encodeId(decodedId);
			}
			
			if(reloadData && activityData.hasObjectChanged()) {
				//reload data
				loadActivityData(decodedId);
				activityData.setCompetenceName(competenceName);
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			/*
			 * to be able to check in oncomplete event if action executed successfully or not.
			 */
			FacesContext.getCurrentInstance().validationFailed();
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void delete() {
		try {
			if(activityData.getActivityId() > 0) {
				/*
				 * passing decodedId because we need to pass id of
				 * original competence and not id of a draft version
				 */
				activityManager.deleteActivity(decodedId, activityData, loggedUser.getUser().getId());
				activityData = new ActivityData(false);
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			} else {
				PageUtil.fireErrorMessage("Activity is not saved so it can't be deleted");
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	 
	public String getPageHeaderTitle() {
		return activityData.getActivityId() > 0 ? "Edit Activity" : "New Activity";
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

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public ActivityData getActivityData() {
		return activityData;
	}

	public void setActivityData(ActivityData activityData) {
		this.activityData = activityData;
	}

	public ActivityType[] getActivityTypes() {
		return activityTypes;
	}

	public void setActivityTypes(ActivityType[] activityTypes) {
		this.activityTypes = activityTypes;
	}

	public String getLinkToAdd() {
		return linkToAdd;
	}

	public void setLinkToAdd(String linkToAdd) {
		this.linkToAdd = linkToAdd;
	}

	public PublishedStatus[] getActStatusArray() {
		return actStatusArray;
	}

	public void setActStatusArray(PublishedStatus[] actStatusArray) {
		this.actStatusArray = actStatusArray;
	}

}
