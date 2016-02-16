package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ExternalActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;
import org.prosolo.services.nodes.data.activity.UploadAssignmentResourceData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceActivitiesBean")
@Component("competenceActivitiesBean")
@Scope("view")
public class CompetenceActivitiesBean implements Serializable {

	private static final long serialVersionUID = -6145577969728042249L;

	private static Logger logger = Logger.getLogger(CompetenceActivitiesBean.class);

	@Autowired private CompetenceManager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private LoggedUserBean loggedUserBean;
	
	private String id;
	private String title;
	private long decodedId;
	
	private List<ActivityData> activities;
	
	private ActivityData activityToEdit;
	private UploadAssignmentResourceData uploadResData;
	private ExternalActivityResourceData externalResData;
	private ResourceActivityResourceData resourceResData;
	
	private ActivityData activityToEditBackup;
	
	private ResourceType[] resTypes;
	
	public CompetenceActivitiesBean() {

	}
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		resTypes = ResourceType.values();
		activityToEdit = new ActivityData();
		if (decodedId > 0) {
			logger.info("Searching activities for competence with id: " + decodedId);
			try {
				if(title == null || "".equals(title)) {
					title = competenceManager.getCompetenceTitle(decodedId);
				}
				activities = competenceManager.getCompetenceActivities(decodedId);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	

	public void initResourceData() {
		activityToEdit.createResourceDataBasedOnResourceType();
		this.uploadResData = null;
		this.externalResData = null;
		this.resourceResData = null;
		
		ResourceData resData = activityToEdit.getResourceData();
		if(resData != null) {
			switch(resData.getActivityType()) {
				case ASSIGNMENT_UPLOAD:
					this.uploadResData = (UploadAssignmentResourceData) resData;
					return;
				case EXTERNAL_TOOL:
					this.externalResData = (ExternalActivityResourceData) resData;
					return;
				case RESOURCE:
					this.resourceResData = (ResourceActivityResourceData) resData;
					return;
			}
		}
	}
	
	public void setActivityForEdit() {
		setActivityForEdit(new ActivityData());
		activityToEdit.setMakerId(loggedUserBean.getUser().getId());
	}

	private void setActivityForEdit(ActivityData activityData) {
		this.activityToEdit = activityData;
	}
	
	public void saveActivity() {
		try {
			CompetenceActivity compAct = competenceManager.saveCompetenceActivity(decodedId, activityToEdit);
			activityToEdit.setCompetenceActivityId(compAct.getId());
			activities.add(activityToEdit);
			activityToEdit = null;
			PageUtil.fireSuccessfulInfoMessage("Competence activity saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
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

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ActivityData getActivityToEdit() {
		return activityToEdit;
	}

	public void setActivityToEdit(ActivityData activityToEdit) {
		this.activityToEdit = activityToEdit;
	}

	public ActivityData getActivityToEditBackup() {
		return activityToEditBackup;
	}

	public void setActivityToEditBackup(ActivityData activityToEditBackup) {
		this.activityToEditBackup = activityToEditBackup;
	}

	public ResourceType[] getResTypes() {
		return resTypes;
	}

	public void setResTypes(ResourceType[] resTypes) {
		this.resTypes = resTypes;
	}

	public UploadAssignmentResourceData getUploadResData() {
		return uploadResData;
	}

	public void setUploadResData(UploadAssignmentResourceData uploadResData) {
		this.uploadResData = uploadResData;
	}

	public ExternalActivityResourceData getExternalResData() {
		return externalResData;
	}

	public void setExternalResData(ExternalActivityResourceData externalResData) {
		this.externalResData = externalResData;
	}

	public ResourceActivityResourceData getResourceResData() {
		return resourceResData;
	}

	public void setResourceResData(ResourceActivityResourceData resourceResData) {
		this.resourceResData = resourceResData;
	}

}
