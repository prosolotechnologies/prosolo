package org.prosolo.web.search.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.activitywall.data.UserDataFactory;

public class LearningGoalData implements Serializable {
	
	private static final long serialVersionUID = -2140618666609224791L;

	private long id;
	private String title;
	private String description;
	private UserData maker;
	private Date deadline;
	private String tagsString;
	private boolean canBeRequestedToJoin;
	private boolean freeToJoin;
	private List<Long> memberIds;
	
	public LearningGoalData () {}
	
	public LearningGoalData(LearningGoal goal) {
		this.id = goal.getId();
		this.title = goal.getTitle();
		this.description = goal.getDescription();
		this.maker = UserDataFactory.createUserData(goal.getMaker());
		this.deadline = goal.getDeadline();
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(goal.getTags());
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

	public UserData getMaker() {
		return maker;
	}

	public void setMaker(UserData maker) {
		this.maker = maker;
	}

	public Date getDeadline() {
		return deadline;
	}
	
	public String getPrettyDeadline() {
		if (deadline != null) {
			return DateUtil.getPrettyDate(deadline);
		} else {
			return "";
		}
	}

	public void setDeadline(Date deadline) {
		if (deadline != null)
			this.deadline = deadline;
	}
	
	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}

	public boolean isCanBeRequestedToJoin() {
		return canBeRequestedToJoin;
	}

	public void setCanBeRequestedToJoin(boolean canBeRequestedToJoin) {
		this.canBeRequestedToJoin = canBeRequestedToJoin;
	}
	
	public boolean isFreeToJoin() {
		return freeToJoin;
	}

	public void setFreeToJoin(boolean freeToJoin) {
		this.freeToJoin = freeToJoin;
	}

	public List<Long> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(List<Long> memberIds) {
		this.memberIds = memberIds;
	}
	
}
