package org.prosolo.web.dialogs;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.notifications.RequestManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.home.GlobalSearchBean;
import org.prosolo.web.home.MessagesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.notification.data.GoalStatus;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.prosolo.web.search.data.LearningGoalData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "requestToJoinGoalBean")
@Component("requestToJoinGoalBean")
@Scope("view")
public class RequestToJoinGoalDialog extends MessagesBean{

	private static final long serialVersionUID = -6164377349129882128L;
	private static final Logger logger = Logger.getLogger(RequestToJoinGoalDialog.class.getName());
	
	@Autowired private DefaultManager defaultManager;
	@Autowired private RequestManager requestManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private GlobalSearchBean globalSearchBean;
	@Autowired private LoggingNavigationBean loggingNavigationBean;

	private GoalData learningGoal;
	private UserData receiver;
	private boolean canBeRequestedToJoin;
	private String context;
	private GoalStatus goalStatus;
	
	// if a goal is connected to a course
	private CourseData courseData;
	
	public void requestToJoinGoalById(long goalId, String context){
		try {
			LearningGoal goal = learningGoalManager.loadResource(LearningGoal.class, goalId);
			requestToJoinGoal(goal, context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void requestToJoinGoal(LearningGoal goal, String context) {
		logger.debug("Opening Request to Join Goal dialog for a learning goal " +
				"'"+goal.getTitle()+"'("+goal.getId()+") initiated by the user '"+loggedUser.getUser()+"'");
		
		this.learningGoal = new GoalData(goal);
		
		User user = HibernateUtil.initializeAndUnproxy(goal.getMaker());
		
		this.context = context;
		this.receiver = new UserData(user);
		this.messageContent = "I would like to join your goal";
		
		logDialogUse(goal.getId(), context);
	}
	
	public void requestToJoinLearningGoal(LearningGoalData goalData, String context) {
		logger.debug("Opening Request to Join Goal dialog for a learning goal " +
				"'"+goalData.getTitle()+"'("+goalData.getId()+") initiated by the user '"+loggedUser.getUser()+"'");
		
		this.context = context;
		
		this.learningGoal = new GoalData();
		this.learningGoal.setGoalId(goalData.getId());
		this.learningGoal.setTitle(goalData.getTitle());
		this.learningGoal.setDescription(goalData.getDescription());
	
		this.receiver = goalData.getMaker();
		this.messageContent = "I would like to join your goal";

		logDialogUse(goalData.getId(), context);
	}

	public void initializeGoalData(GoalData goalData, UserData requestFrom, String context) {
		try {
			this.context = context;
			this.learningGoal = goalData;
			this.canBeRequestedToJoin = learningGoalManager.canUserJoinGoal(goalData.getGoalId(), loggedUser.getUser());
			this.receiver = requestFrom;
			this.messageContent = "I would like to join your goal";
			
			if (goalData.getTargetGoalId() > 0) {
				this.goalStatus = NotificationDataConverter.getGoalStatus(
					loggedUser.getUser(), 
					(Session) defaultManager.getPersistence().currentManager(), 
					goalData.getTargetGoalId());
			}

			logDialogUse(goalData.getGoalId(), context);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void sendRequestToJoinGoal() {
		try {
			 requestManager.requestToJoinTargetLearningGoal(learningGoal.getTargetGoalId(),
					loggedUserBean.getUser(), receiver.getId(),
					messageContent, context);
			 
			 PageUtil.fireSuccessfulInfoMessage("newRequestToJoinFormGrowl", "You have sent a request to "+ receiver.getName()+".");
		} catch (EventException e) {
			logger.error("Can't send request to join Learning Goal :"+learningGoal.getGoalId()+" message:"+e.getLocalizedMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Can't send request to join Learning Goal :"+learningGoal.getGoalId()+" message:"+e.getLocalizedMessage());
		}
	}
	
	public void sendRequestToJoinGoal(long goalId, String goalTitle, long receiverId, String context) {
		try {
			requestManager.requestToJoinTargetLearningGoal(
					goalId,
					loggedUserBean.getUser(), 
					receiverId, 
					messageContent,
					context);
			
			PageUtil.fireSuccessfulInfoMessage("goalDetailsDialogGrowl", "You have sent a request to join learning goal '"+ goalTitle+"'.");
		} catch (EventException e) {
			logger.error("Can not send a request to join Learning Goal: "+goalId+". "+e.getLocalizedMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Can not send a request to join Learning Goal: "+goalId+". "+e.getLocalizedMessage());
		}
		this.messageContent = null;
	}

	public boolean checkIfLearningGoalIsFreeToJoin(LearningGoalData goalData) {
		if(goalData != null){
			boolean result = learningGoalManager.isUserMemberOfLearningGoal(goalData.getId(), loggedUser.getUser());
		
			if (result == false) {
				if (goalData.isFreeToJoin()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void logDialogUse(long goalId, String context){
		loggingNavigationBean.logServiceUse(
				ComponentName.REQUEST_TO_JOIN_GOAL_DIALOG,
				"action",  "openRequestToJoinGoalDialog",
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

	public boolean isCanBeRequestedToJoin() {
		return canBeRequestedToJoin;
	}

	public String getContext() {
		return context;
	}

	public GoalStatus getGoalStatus() {
		return goalStatus;
	}

	public CourseData getCourseData() {
		return courseData;
	}
	
}
