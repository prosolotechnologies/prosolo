package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.competences.data.ActivityFormData;
import org.prosolo.web.dialogs.data.CompetenceFormData;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Deprecated
@ManagedBean(name = "manageCompetenceBean")
@Component("manageCompetenceBean")
@Scope("view")
public class ManageCompetenceBean implements Serializable {

	private static final long serialVersionUID = -7601307404210958000L;

	private static Logger logger = Logger.getLogger(ManageCompetenceBean.class);

	@Autowired private TagManager tagManager;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private ActivityDialogBean activitiesBean;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private ResourceFactory resourceFactory;
	@Inject private UrlIdEncoder idEncoder;
	
	private CompetenceFormData compData = new CompetenceFormData();
	private Competence competence;
	private boolean showActivityDetails = true;

	private String id;
	private String origin;
	
	// used for detecting position change
	private String positionUpdateStatus;
	
	public ManageCompetenceBean() {

	}
	
	public void init() {
		System.out.println("INIT COMPETENCE");
		long decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			System.out.println("INIT COMPETENCE ID:"+id);
			try {
				competence = competenceManager.loadResource(Competence.class, decodedId);
				compData = new CompetenceFormData(competence);
				
				List<ActivityWallData> actData = compWallActivityConverter.convertCompetenceActivities(
						competence.getActivities(), 
						loggedUser.getUserId(), 
						true, 
						false);
				System.out.println("INIT COMPETENCE activities:"+actData.size());
				compData.setActivities(actData);
			} catch(ObjectNotFoundException onf) {
				try {
					logger.error(onf);
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public void autosave() {
		try {
			updateCompetence();
			
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getMessage(
							"manager.competences.autosave.success", 
							loggedUser.getLocale()));
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}

	public void addActivity(Activity activity) {
		connectActivity(activity);
	}
	
	public void createActivity() {
		try {
			ActivityFormData activityFormData = activitiesBean.getActivityFormData();
			
			// activity is being created
			if (activityFormData.getId() == 0) {
				Activity activity = resourceFactory.createNewActivity(
						loggedUser.getUserId(), 
						activityFormData,
						VisibilityType.PUBLIC);
				
				connectActivity(activity);
				PageUtil.fireSuccessfulInfoMessage("managerCreateCompetence:newCompForm:newCompFormGrowl", "Activity added");
			} 
			// activity is edited
			else {
				PageUtil.fireSuccessfulInfoMessage("managerCreateCompetence:newCompForm:newCompFormGrowl", "Activity updated");
			}
		} catch (EventException | ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireSuccessfulInfoMessage("managerCreateCompetence:newCompForm:newCompFormGrowl", "Error updating activity");
		}
		autosave();
	}

	public void updateCompetenceAction() {
		if (competence != null) {
			updateCompetence();
			
//			try {
//				eventFactory.generateEvent(EventType.Edit, loggedUser.getUser(), competence);
//			} catch (EventException e1) {
//				logger.error(e1);
//			}
			
			PageUtil.fireSuccessfulInfoMessage("compSearchForm:compSearchFormGrowl", 
					"Competence details are updated");
		
			try {
				ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
				context.getFlash().setKeepMessages(true);
				String destination = "";
				
				if (origin != null && !origin.isEmpty()) {
					destination = origin;
				} else {
					destination = "/manage/competences";
				}
				
				context.redirect(context.getRequestContextPath() + destination);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public void updateCompetence() {
		competence = competenceManager.updateCompetence(
				competence,
				compData.getTitle(),
				compData.getDescription(),
				compData.getDuration(),
				compData.getValidity(),
				new HashSet<Tag>(tagManager.parseCSVTagsAndSave(compData.getTagsString())),
				compData.getCorequisites(),
				compData.getPrerequisites(),
				compData.getActivities(),
				true
		);
	}
	
	// Called also from the activity search box
	public void connectActivity(Activity activity) {
		activity = HibernateUtil.initializeAndUnproxy(activity);
		
		ActivityWallData actData = compWallActivityConverter.convertActivityToActivityWallData(
				activity, 
				loggedUser.getUserId(), 
				loggedUser.getLocale(), 
				true, 
				false);
		
		actData.setPosition(compData.getActivities().size());
		compData.addActivity(actData);
		Collections.sort(compData.getActivities());
		
		recalculatePositionNumbers();
		
		autosave();
	}
	
	public void updateActivity(Activity activity) {
		activity = HibernateUtil.initializeAndUnproxy(activity);
		
		int activityPosition = -1;
		
		for (ActivityWallData actData : compData.getActivities()) {
			activityPosition++;
			
			if (actData.getObject().getId() == activity.getId()) {
				break;
			}
		}
		
		if (activityPosition == -1) {
			logger.error("Something is wrong. Could not find activity data for activity " + activity.getId());
		} else {
			ActivityWallData actData = compWallActivityConverter.convertActivityToActivityWallData(
					activity, 
					loggedUser.getUserId(), 
					loggedUser.getLocale(), 
					true, 
					false);
			
			actData.setPosition(activityPosition);
			compData.getActivities().set(activityPosition, actData);
		}
		
		autosave();
	}
	
	public void removeActivity(ActivityWallData activityDataToRemove){
		List<ActivityWallData> activities = compData.getActivities();
		
		if (activities != null && !activities.isEmpty()) {
			Iterator<ActivityWallData> iterator = activities.iterator();
			
			while (iterator.hasNext()) {
				ActivityWallData activityWallData = (ActivityWallData) iterator.next();
				
				if (activityDataToRemove.getObject().getId() == activityWallData.getObject().getId()) {
					iterator.remove();
					break;
				}
			}
			
			recalculatePositionNumbers();
			
//			if (position >= 0) {
//				for (int i = (int) position; i < activities.size(); i++) {
//					ActivityWallData actData = activities.get(i);
//				}
//			}
			
			autosave();
		}
	}
	
	public String getActivitiesIds() {
		List<ActivityWallData> activities = compData.getActivities();
		
		if (activities.size() > 0) {
			long[] ids = new long[activities.size()];
			
			for (int i = 0; i < ids.length; i++) {
				ids[i] = activities.get(i).getObject().getId();
			}
			return Arrays.toString(ids);
		}
		return null;
	}
	
	public String getCompetenceIds() {
		List<Long> compIdsList = new LinkedList<Long>();
		
		compIdsList.add(competence.getId());
		
		for (Competence prereq : compData.getPrerequisites()) {
			compIdsList.add(prereq.getId());
		}
		
		for (Competence coreq : compData.getCorequisites()) {
			compIdsList.add(coreq.getId());
		}
		
		return Arrays.toString(compIdsList.toArray());
	}
	
	public void updatePosition() {
		if (positionUpdateStatus != null) {
			String[] positions = positionUpdateStatus.split(",");
			int startPos = Integer.valueOf(positions[0]);
			int endPos = Integer.valueOf(positions[1]);
			ActivityWallData actData = compData.getActivities().get(startPos);
			compData.getActivities().remove(startPos);
			compData.getActivities().add(endPos, actData);
			
			recalculatePositionNumbers();
			
			autosave();
		}
	}

	private void recalculatePositionNumbers() {
		int index = 0;
		for (ActivityWallData act : compData.getActivities()) {
			act.setPosition(index++);
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
	
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public CompetenceFormData getCompData() {
		return compData;
	}

	public void setCompData(CompetenceFormData competenceData) {
		this.compData = competenceData;
	}

	public String getPositionUpdateStatus() {
		return positionUpdateStatus;
	}

	public void setPositionUpdateStatus(String positionUpdateStatus) {
		this.positionUpdateStatus = positionUpdateStatus;
	}

	public boolean isShowActivityDetails() {
		return showActivityDetails;
	}

	public void setShowActivityDetails(boolean showActivityDetails) {
		this.showActivityDetails = showActivityDetails;
	}
	
}
