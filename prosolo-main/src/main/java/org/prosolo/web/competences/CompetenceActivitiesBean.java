package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ExternalActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;
import org.prosolo.services.nodes.data.activity.UploadAssignmentResourceData;
import org.prosolo.services.nodes.data.activity.mapper.ActivityMapperFactory;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ActivityDataMapper;
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
	@Inject private TextSearch textSearch;
	
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
	
	private String actSearchTerm;
	private List<ActivityData> searchResults;
	private List<Long> activitiesToExclude;
	
	private int currentNumberOfActivities;
	
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
				initializeActivities();
				logger.info("Loaded competence activities for competence with id "+ 
						decodedId);
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
	
	private void initializeActivities() {
		activities = competenceManager.getCompetenceActivities(decodedId);
		activitiesToExclude = new ArrayList<>();
		for(ActivityData ad : activities) {
			activitiesToExclude.add(ad.getActivityId());
		}
		currentNumberOfActivities = activities.size();
	}

	public void searchActivities() {
		searchResults = new ArrayList<>();
		if(actSearchTerm != null && !actSearchTerm.isEmpty()) {
			int size = activitiesToExclude.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = activitiesToExclude.get(i);
			}
			TextSearchResponse searchResponse = textSearch.searchActivities(
					actSearchTerm, 
					0, 
					Integer.MAX_VALUE, 
					false, 
					toExclude);
			
			@SuppressWarnings("unchecked")
			List<Activity> acts = (List<Activity>) searchResponse.getFoundNodes();
			if(acts != null) {
				for(Activity a : acts) {
					ActivityDataMapper mapper = ActivityMapperFactory.getActivityDataMapper(a);
					if(mapper != null) {
						ActivityData ad = mapper.mapToActivityData();
						searchResults.add(ad);
					}
				}
			}
		} 
	}
	
	public void addActivityFromSearch(ActivityData act) {
		try {
			saveNewCompActivity(act);
			PageUtil.fireSuccessfulInfoMessage("Activity added");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		searchResults = new ArrayList<>();
	}
	
	private void saveNewCompActivity(ActivityData act) {
		currentNumberOfActivities++;
		act.setOrder(currentNumberOfActivities);
		CompetenceActivity cAct = getLearningContextParametersAndSaveActivity(act);
		act.setCompetenceActivityId(cAct.getId());
		activities.add(act);
		activitiesToExclude.add(act.getActivityId());
	}

	public void deleteCompActivity() {
		try {
			currentNumberOfActivities--;
			int index = activities.indexOf(activityToEdit);
			List<ActivityData> changedActivities = shiftOrderOfActivitiesUp(index);
			competenceManager.deleteCompetenceActivity(decodedId, activityToEdit.getCompetenceActivityId(), 
					changedActivities);
			activities.remove(activityToEdit);
			activitiesToExclude.remove(new Long(activityToEdit.getActivityId()));
			PageUtil.fireSuccessfulInfoMessage("Activity deleted");
		} catch(DbConnectionException e) {
			logger.error(e);
			initializeActivities();
			PageUtil.fireErrorMessage(e.getMessage());
		}
		activityToEdit = null;
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

	public void setActivityForEdit(ActivityData activityData) {
		this.activityToEdit = activityData;
	}
	
	public void saveActivity() {
		try {
			saveNewCompActivity(activityToEdit);
			PageUtil.fireSuccessfulInfoMessage("Competence activity saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		activityToEdit = null;
	}

	private CompetenceActivity getLearningContextParametersAndSaveActivity(ActivityData activity) {
		String learningContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		String page = PageUtil.getPostParameter("page");
		return competenceManager.saveCompetenceActivity(decodedId, activity,
				page, learningContext, service);
	}
	
	public void moveDown(int index) {
		moveActivity(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveActivity(index - 1, index);
	}
	
	public void moveActivity(int i, int k) {
		ActivityData ad1 = activities.get(i);
		ad1.setOrder(ad1.getOrder() + 1);
		ActivityData ad2 = activities.get(k);
		ad2.setOrder(ad2.getOrder() - 1);
		try {
			List<ActivityData> changed = new ArrayList<>();
			changed.add(ad1);
			changed.add(ad2);
			competenceManager.updateOrderOfCompetenceActivities(changed);
			Collections.swap(activities, i, k);
		} catch(DbConnectionException e) {
			logger.error(e);
			ad1.setOrder(ad1.getOrder() - 1);
			ad2.setOrder(ad2.getOrder() + 1);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		
	}
	
	private List<ActivityData> shiftOrderOfActivitiesUp(int index) {
		List<ActivityData> changedActivities = new ArrayList<>();
		for(int i = index; i < currentNumberOfActivities; i++) {
			ActivityData ad = activities.get(i);
			ad.setOrder(ad.getOrder() - 1);
			changedActivities.add(ad);
		}
		return changedActivities;
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

	public String getActSearchTerm() {
		return actSearchTerm;
	}

	public void setActSearchTerm(String actSearchTerm) {
		this.actSearchTerm = actSearchTerm;
	}

	public List<ActivityData> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<ActivityData> searchResults) {
		this.searchResults = searchResults;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public int getCurrentNumberOfActivities() {
		return currentNumberOfActivities;
	}

	public void setCurrentNumberOfActivities(int currentNumberOfActivities) {
		this.currentNumberOfActivities = currentNumberOfActivities;
	}

}
