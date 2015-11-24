package org.prosolo.web.dialogs;

import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.home.MessagesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "freeToJoinGoalDialog")
@Component("freeToJoinGoalDialog")
@Scope("view")
public class FreeToJoinGoalDialog extends MessagesBean{

	private static final long serialVersionUID = -3365757063775626528L;

	private static final Logger logger = Logger.getLogger(FreeToJoinGoalDialog.class.getName());
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private EventFactory eventFactory;

	private GoalData learningGoal;
	private UserData receiver;
	private boolean canBeJoined;
	private String context;
	private long id;
	
	public void initializeGoalData(GoalData goalData, String context) {
		try {
			this.context = context;
			this.id = goalData.getGoalId();
			this.learningGoal = goalData;
			this.canBeJoined = goalManager.canUserJoinGoal(goalData.getGoalId(), loggedUser.getUser());
			this.receiver = goalData.getCreator();
			this.messageContent = "I would like to join your goal";

			logDialogUse(goalData.getGoalId(), context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void joinGoal() {
		try {
			TargetLearningGoal targetGoal = goalManager.createNewTargetLearningGoal(loggedUser.getUser(), learningGoal.getGoalId());
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.JoinedGoal, loggedUser.getUser(), targetGoal, parameters);
			
			PageUtil.fireSuccessfulInfoMessage("goalDetailsDialogGrowl", "You have joined the goal '" + learningGoal.getTitle()+"'.");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	public void logDialogUse(long goalId, String context){
		loggingNavigationBean.logServiceUse(
				ComponentName.FREE_TO_JOIN_GOAL_DIALOG,
				"action",  "openFreeToJoinGoalDialog",
				"context", context,
				"learningGoalId", String.valueOf(goalId));
	}

	/*
	 * GETTERS / SETTERS
	 */
	public GoalData getLearningGoal() {
		return learningGoal;
	}

	public void setLearningGoal(GoalData learningGoal) {
		this.learningGoal = learningGoal;
	}

	public UserData getReceiver() {
		return receiver;
	}

	public boolean isCanBeJoined() {
		return canBeJoined;
	}

	public String getContext() {
		return context;
	}

	public long getId() {
		return id;
	}
	
}
