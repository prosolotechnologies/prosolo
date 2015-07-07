/**
 * 
 */
package org.prosolo.web.notification.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.activitywall.data.UserData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class NotificationData implements Serializable {

	private static final long serialVersionUID = -4021953490627642131L;

	private long id;
	private NodeData resource;
	private boolean read;
	private String chosenAction;
	private Date created;
//	private String resourceTitle;
	private String type;
	private boolean actionable;
	private EventType notificationType;
	private List<NotificationAction> actions;
	
	// used for join goal invitation
	private GoalStatus goalStatus;
	
	private UserData actor;
	private String date;
	
	private String message;

	public NotificationData() {
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	
	public String getChosenAction() {
		return chosenAction;
	}

	public void setChosenAction(String chosenAction) {
		this.chosenAction = chosenAction;
	}

//	public String getResourceTitle() {
//		return resourceTitle;
//	}
//
//	public void setResourceTitle(String resourceTitle) {
//		this.resourceTitle = resourceTitle;
//	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public EventType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(EventType notificationType) {
		this.notificationType = notificationType;
	}

	public NodeData getResource() {
		return resource;
	}

	public void setResource(NodeData resource) {
		this.resource = resource;
	}

	public boolean isActionable() {
		return actionable;
	}

	public void setActionable(boolean actionable) {
		this.actionable = actionable;
	}

	public List<NotificationAction> getActions() {
		return actions;
	}

	public void setActions(List<NotificationAction> actions) {
		this.actions = actions;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	public GoalStatus getGoalStatus() {
		return goalStatus;
	}

	public void setGoalStatus(GoalStatus goalStatus) {
		this.goalStatus = goalStatus;
	}

	public String getWhen() {
		return DateUtil.getTimeAgoFromNow(created);
	}
	
	public UserData getActor() {
		return actor;
	}

	public void setActor(UserData actor) {
		this.actor = actor;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
