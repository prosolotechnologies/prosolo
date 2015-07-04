/**
 * 
 */
package org.prosolo.web.communications.evaluation.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.workflow.evaluation.Badge;
import org.prosolo.util.date.DateUtil;

/**
 * @author "Nikola Milikic"
 * 
 */
public class EvaluatedResourceData implements Serializable {

	private static final long serialVersionUID = -7717344346726021261L;

	private long id;
	private String title;
	private String uri;
	private EvaluatedResourceType type;
	private boolean accepted;
	private boolean readOnly;
	private String comment;
	private String dateStared;
	private String dateCompleted;
	private BaseEntity resource;
	private List<SelectedBadge> badges;
	
	public EvaluatedResourceData(){
		badges = new ArrayList<SelectedBadge>();
	}
	
	public EvaluatedResourceData(BaseEntity resource, EvaluatedResourceType type, String comment) {
		this(resource.getId(), resource.getTitle(), type);
		this.resource = resource;
		this.comment = comment;
		this.accepted = false;
		this.dateStared = DateUtil.getPrettyDate(resource.getDateCreated());
		
		if (resource instanceof TargetLearningGoal) {
			TargetLearningGoal tGoal = (TargetLearningGoal) resource;
			
			if (tGoal.getCompletedDate() != null) {
				this.dateCompleted = DateUtil.getPrettyDate(tGoal.getCompletedDate());
			}
		}
	}
	
	public EvaluatedResourceData(long id, String title, EvaluatedResourceType type) {
		this();
		this.id = id;
		//this.uri = resource.getUri();
		this.title = title;
		this.type = type;
//		this.resource = resource;
	}
	
	public EvaluatedResourceData(BaseEntity resource, EvaluatedResourceType type, boolean accepted) {
		this(resource.getId(), resource.getTitle(), type);
		this.resource = resource;
		this.accepted = accepted;
		this.dateStared = DateUtil.getPrettyDate(resource.getDateCreated());
		
		if (resource instanceof TargetLearningGoal) {
			TargetLearningGoal tGoal = (TargetLearningGoal) resource;
			
			if (tGoal.getCompletedDate() != null) {
				this.dateCompleted = DateUtil.getPrettyDate(tGoal.getCompletedDate());
			}
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public EvaluatedResourceType getType() {
		return type;
	}

	public void setType(EvaluatedResourceType type) {
		this.type = type;
	}
	
	public boolean isAccepted() {
		return accepted;
	}
	
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getDateStared() {
		return dateStared;
	}

	public void setDateStared(String dateStared) {
		this.dateStared = dateStared;
	}

	public String getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(String dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public BaseEntity getResource() {
		return resource;
	}

	public void setResource(BaseEntity resource) {
		this.resource = resource;
	}

	public List<SelectedBadge> getBadges() {
		return badges;
	}

	public void setBadges(List<SelectedBadge> badges) {
		this.badges = badges;
	}
	
	public void addBadge(SelectedBadge badge) {
		if (badge != null) {
			badges.add(badge);
		}
	}
	
	public List<Badge> getChosenBadges() {
		List<Badge> selectedBadges = new ArrayList<Badge>();
		
		for (SelectedBadge badge : badges) {
			if (badge.isSelected()) {
				selectedBadges.add(badge.getBadge());
			}
		}
		
		return selectedBadges;
	}
	
	public List<SelectedBadge> getSelectedBadges() {
		List<SelectedBadge> selectedBadges = new ArrayList<SelectedBadge>();
		
		for (SelectedBadge badge : badges) {
			if (badge.isSelected()) {
				selectedBadges.add(badge);
			}
		}
		
		return selectedBadges;
	}
	
	public void addBadges(List<Badge> badges) {
		if (badges != null && !badges.isEmpty()) {
			for (Badge badge : badges) {
				addBadge(new SelectedBadge(badge));
			}
		}
	}

}
