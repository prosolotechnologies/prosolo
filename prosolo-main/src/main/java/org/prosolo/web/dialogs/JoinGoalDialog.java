package org.prosolo.web.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.GoalStatus;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "joinGoalDialog")
@Component("joinGoalDialog")
@Scope("view")
public class JoinGoalDialog implements Serializable {

	private static final long serialVersionUID = -4612387953829473207L;

	private static Logger logger = Logger.getLogger(JoinGoalDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager goalManager;
	
	private NotificationData notificationData;
	private NodeData targetGoalToJoinData;
	private GoalStatus goalStatus;
	private String context;
	private List<UserData> collaborators;
	
	public void initializeJoinGoalDialog(NotificationData notificationData, String context) {
		this.notificationData = notificationData;
		this.targetGoalToJoinData = notificationData.getResource();
		this.goalStatus = notificationData.getGoalStatus();
		this.context = context;
		
		this.collaborators = new ArrayList<UserData>();
		List<User> collabs = goalManager.getCollaboratorsByTargetGoalId(targetGoalToJoinData.getId());
		
		if (collabs != null && !collabs.isEmpty()) {
			for (User user : collabs) {
				collaborators.add(new UserData(user));
			}
		}
	}
	
	public void accept() {
		TopNotificationsBean notificationsBean = PageUtil.getSessionScopedBean("topNotificationsBean", TopNotificationsBean.class);
	
		if (notificationsBean != null) {
			try {
				notificationsBean.acceptNotification(notificationData);
			} catch (IOException e) {
				logger.error(e);
			} catch (EventException e) {
				logger.error(e);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	public void ignore() {
		TopNotificationsBean notificationsBean = PageUtil.getSessionScopedBean("topNotificationsBean", TopNotificationsBean.class);
		
		if (notificationsBean != null) {
			try {
				notificationsBean.ignoreNotification(notificationData);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}
	
	public NodeData getTargetGoalToJoinData() {
		return targetGoalToJoinData;
	}

	public void setTargetGoalToJoinData(NodeData targetGoalToJoinData) {
		this.targetGoalToJoinData = targetGoalToJoinData;
	}
	
	public GoalStatus getGoalStatus() {
		return goalStatus;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<UserData> getCollaborators() {
		return collaborators;
	}
	
}