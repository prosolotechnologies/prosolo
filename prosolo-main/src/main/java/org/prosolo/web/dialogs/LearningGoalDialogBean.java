package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.activityWall.UserDataFactory;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.notifications.RequestManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.home.data.ResourceAvailability;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="goalDialog")
@Component("goalDialog")
@Scope("view")
public class LearningGoalDialogBean implements Serializable {
	
	private static final long serialVersionUID = -5274507284010351282L;

	private static Logger logger = Logger.getLogger(LearningGoalDialogBean.class);
	
	@Autowired private LearningGoalManager goalManager;
	@Autowired private RequestManager requestManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private LearnBean learningGoals;
	
	private UserData recommender;
	private LearningGoal goal;
	private TargetLearningGoal targetGoal;
	private LearningGoalDialogMode mode;
	private String message;
	private ResourceAvailability resourceAvailability;
	private List<ActivityWallData> usedCompetences = new ArrayList<ActivityWallData>();
	private long noUsersEnrolled;
	
	public void init() {
		usedCompetences = generateActivitiesDataForLearningGoal(targetGoal);
		initializeResourceAvailability(goal);
		
		this.noUsersEnrolled = goalManager.getNumberOfUsersLearningGoal(goal);
	}
	
	/*
	 * ACTIONS
	 */
	public void initializeOtherGoalDialogById(long goalId) {
		try {
			this.goal = goalManager.loadResource(LearningGoal.class, goalId);
			mode = LearningGoalDialogMode.OTHERS_GOAL;
			init();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializeOtherTargetGoalDialogById(long targetLearningGoalId) {
		try {
			this.targetGoal= goalManager.loadResource(TargetLearningGoal.class, targetLearningGoalId);
			this.goal = targetGoal.getLearningGoal();
			mode = LearningGoalDialogMode.OTHERS_GOAL;
			init();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializeRecommendedGoalDialog(long goalId) {
		try {
			this.goal = goalManager.loadResource(LearningGoal.class, goalId);
			mode = LearningGoalDialogMode.RECOMMENDED;
			init();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void initializeRecommendedBySystemGoalDialog(LearningGoal goal) {
		this.goal = goalManager.merge(goal);
		mode = LearningGoalDialogMode.RECOMMENDEDBYSYSTEM;
		init();
	}

	public void initializeRecommendedGoalDialog(long goalId, long recommenderId) {
		try {
			this.goal = goalManager.loadResource(LearningGoal.class, goalId);
			this.recommender = UserDataFactory.createUserData(goalManager.loadResource(User.class, recommenderId));
			
			mode = LearningGoalDialogMode.RECOMMENDED;
			init();
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	 
	private void initializeResourceAvailability(LearningGoal goal){
		if (checkIfUserHasGoal(goal)) {
			this.setResourceAvailability(ResourceAvailability.USER_HAS_THIS_GOAL);
		} else if (checkIfUserHasSentRequestForGoal(goal)) {
			this.setResourceAvailability(ResourceAvailability.REQUEST_ALREADY_SENT);
		} else if (goal.isFreeToJoin()) {
			this.setResourceAvailability(ResourceAvailability.IS_FREE_TO_JOIN);
		} else if (!goal.isFreeToJoin()) {
			this.setResourceAvailability(ResourceAvailability.CAN_BE_REQUESTED_TO_JOIN);
		}
	}

	private boolean checkIfUserHasGoal(LearningGoal goal) {
		// check if user has this goal among his active goals
		List<GoalData> ongoingGoals = learningGoals.getOngoingGoals();
		
		for (GoalData goalData : ongoingGoals) {
			if (goalData.getGoalId() == goal.getId()) {
				return true;
			}
		}
		
		// check if user has already completed this goal
		PortfolioBean portfolioBean = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);
		
		if (portfolioBean != null) {
			List<GoalData> completedGoals = portfolioBean.getCompletedGoals();
			
			if (completedGoals != null && !completedGoals.isEmpty()) {
				for (GoalData goalData : completedGoals) {
					if (goalData.getGoalId() == goal.getId()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean checkIfUserHasSentRequestForGoal(LearningGoal goal) {
		if (loggedUser.isLoggedIn()) {
			return requestManager.existsRequestToJoinGoal(loggedUser.getUserId(), goal);
		}
		return false;
	}
	
	public void joinLearningGoal() {
		joinLearningGoal(this.goal);
	}
	
	
	public void joinLearningGoalById(long goalId) {
		try {
			this.goal = goalManager.loadResource(LearningGoal.class, goalId);
			joinLearningGoal(goal);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void joinLearningGoal(LearningGoal goal) {
		try {
			goalManager.createNewTargetLearningGoal(loggedUser.getUserId(), goal);
			PageUtil.fireSuccessfulInfoMessage("goalDetailsDialogGrowl", "You have joined the goal '" + goal.getTitle()+"'.");
		} catch (EventException e) {
			logger.error(e);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		
	}
	
	public List<ActivityWallData> generateActivitiesDataForLearningGoal(TargetLearningGoal targetGoal) {
		List<ActivityWallData> wallActivities = new ArrayList<ActivityWallData>();
		
		if (targetGoal != null) {
			targetGoal = goalManager.merge(targetGoal);
			Collection<TargetCompetence> targetCompetences = targetGoal.getTargetCompetences();
			
			if (targetCompetences != null && !targetCompetences.isEmpty()) {
				
				for (TargetCompetence targetCompetence : targetCompetences) {
					List<TargetActivity> compActivities = targetCompetence.getTargetActivities();
					
					if (compActivities != null && !compActivities.isEmpty()) {
						Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
						
						for (TargetActivity act : compActivities) {
							ActivityWallData wallActivity = compWallActivityConverter
									.convertTargetActivityToActivityWallData(null, act,
											act.getMaker().getId(), locale, false, false);
							
							if (wallActivity != null) {
								wallActivities.add(wallActivity);
							}
						}
					}
				}
			}
		}
		return wallActivities;
	}

	/* 
	 * GETTERS / SETTERS 
	 */
	public LearningGoal getGoal() {
		return goal;
	}

	public void setGoal(LearningGoal goal) {
		this.goal = goal;
	}
	
	public TargetLearningGoal getTargetGoal() {
		return targetGoal;
	}

	public LearningGoalDialogMode getMode() {
		return mode;
	}

	public void setMode(LearningGoalDialogMode mode) {
		this.mode = mode;
	}
	
	public UserData getRecommender() {
		return recommender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<ActivityWallData> getUsedCompetences() {
		return usedCompetences;
	}

	public void setUsedCompetences(List<ActivityWallData> usedCompetences) {
		this.usedCompetences = usedCompetences;
	}

	public ResourceAvailability getResourceAvailability() {
		return resourceAvailability;
	}

	public void setResourceAvailability(ResourceAvailability resourceAvailability) {
		this.resourceAvailability = resourceAvailability;
	}
	
	public long getNoUsersEnrolled() {
		return noUsersEnrolled;
	}
	
	public enum LearningGoalDialogMode {
		MY_GOAL,
		RECOMMENDED,
		OTHERS_GOAL, 
		RECOMMENDEDBYSYSTEM
	}
}
