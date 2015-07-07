package org.prosolo.web.dialogs.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.data.CompetenceAnalyticsData;

public class CompetenceFormData implements Serializable{

	private static final long serialVersionUID = 7363359846553731441L;

	private long id;
	private String title;
	private String description;
	private int duration = 1;
	private int validity = 1;
	private User maker;
	private boolean completed;
	private boolean likedByUser;
	private boolean dislikedByUser;
	private boolean ownedByUser;
	private boolean canEdit;
	private boolean isFollowedByUser;

	private CompetenceAnalyticsData analyticsData;
	private String tagsString;
	private Collection<Tag> tags;
	private List<Competence> prerequisites;
	private List<Competence> corequisites;
	private List<ActivityWallData> activities;
	
	public CompetenceFormData(){
		prerequisites = new ArrayList<Competence>();
		corequisites = new ArrayList<Competence>();
		activities = new LinkedList<ActivityWallData>();
	}

	public CompetenceFormData(Competence comp){ 
		this.id = comp.getId();
		this.title = comp.getTitle();
		this.description = comp.getDescription();
		this.validity = comp.getValidityPeriod();
		this.duration = comp.getDuration();
		this.maker = comp.getMaker();
		this.tags = comp.getTags();
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(this.tags);
		this.prerequisites = new ArrayList<Competence>(comp.getPrerequisites());
		this.corequisites = new ArrayList<Competence>(comp.getCorequisites());
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<ActivityWallData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityWallData> activities) {
		this.activities = activities;
	}
	
	public void addActivity(ActivityWallData activity) {
		if (activity != null) {
			this.activities.add(activity);
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isLikedByUser() {
		return likedByUser;
	}

	public void setLikedByUser(boolean likedByUser) {
		this.likedByUser = likedByUser;
	}
	
	public boolean isDislikedByUser() {
		return dislikedByUser;
	}

	public void setDislikedByUser(boolean dislikedByUser) {
		this.dislikedByUser = dislikedByUser;
	}

	public CompetenceAnalyticsData getAnalyticsData() {
		return analyticsData;
	}

	public void setAnalyticsData(CompetenceAnalyticsData analyticsData) {
		this.analyticsData = analyticsData;
	}

	public boolean isOwnedByUser() {
		return ownedByUser;
	}

	public void setOwnedByUser(boolean ownedByUser) {
		this.ownedByUser = ownedByUser;
	}
	
	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}

	public Collection<Tag> getTags() {
		return tags;
	}

	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
	}
	
	public List<Competence> getPrerequisites() {
		return prerequisites;
	}

	public void setPrerequisites(List<Competence> prerequisites) {
		this.prerequisites = prerequisites;
	}

	public void addPrerequisite(Competence prerequisite) {
		if (prerequisite != null) {
			if (!prerequisites.contains(prerequisite)) {
				prerequisites.add(prerequisite);
			}
		}
	}
	
	public void removePrerequisite(Competence prerequisite) {
		if (prerequisite != null) {
			Iterator<Competence> iterator = prerequisites.iterator();
			
			while (iterator.hasNext()) {
				Competence competence = (Competence) iterator.next();
				
				if (competence.getId() == prerequisite.getId()) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public List<Competence> getCorequisites() {
		return corequisites;
	}

	public void setCorequisites(List<Competence> corequisites) {
		this.corequisites = corequisites;
	}

	public void addCorequisite(Competence corequisite) {
		if (corequisite != null) {
			if (!corequisites.contains(corequisite)) {
				corequisites.add(corequisite);
			}
		}
	}
	
	public void removeCorequisite(Competence corequisite) {
		if (corequisite != null) {
			Iterator<Competence> iterator = corequisites.iterator();
			
			while (iterator.hasNext()) {
				Competence competence = (Competence) iterator.next();
				
				if (competence.getId() == corequisite.getId()) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
	
	public boolean isFollowedByUser() {
		return isFollowedByUser;
	}

	public void setFollowedByUser(boolean isFollowedByUser) {
		this.isFollowedByUser = isFollowedByUser;
	}

	public Competence updateCompetenceWithData(TargetCompetence tComp) {
		Competence comp = tComp.getCompetence();
		comp.setTitle(title);
		comp.setDescription(description);
		comp.setValidityPeriod(validity);
		return comp;
	}
	
}
