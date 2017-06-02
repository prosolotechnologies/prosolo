package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.activityWall.UserDataFactory;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.RequestManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.ResourceDataUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="inviteGoalCollaboratorDialog")
@Component("inviteGoalCollaboratorDialog")
@Scope("view")
public class InviteGoalCollaboratorDialogBean implements Serializable {
	
	private static final long serialVersionUID = -3642762984584866751L;

	private static Logger logger = Logger.getLogger(InviteGoalCollaboratorDialogBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DefaultManager defaultManager;
	@Autowired private RequestManager requestManager;
	@Inject private UserTextSearch userTextSearch;
	@Autowired private EventFactory eventFactory;
	@Autowired private LoggingNavigationBean actionLogger;
	
	private String keyword;
	private String message = "";
	
	private List<UserData> userSearchResults;
	private List<UserData> collabratorsToInvite;
	private List<UserData> existingCollaborators;
	private List<UserData> invitedUsers;
	private List<UserData> recommendedCollaborators;
	private List<UserData> recommendedCollaboratorsEdit;
	private List<Long> excludedUsersForSearch = new ArrayList<Long>();
	
//	private LearningGoal goal;
	private GoalDataCache goalsData;
	
	@PostConstruct
	public void init(){
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
		reset();
	}
	
	public void initialize(GoalDataCache goalsData) {
		this.goalsData = goalsData;

		// invited collaborators
		invitedUsers.clear();
		
		List<User> invited = requestManager.getUsersWithUnansweredInvitationForGoal(this.goalsData.getData().getTargetGoalId());
		
		if (invited != null && !invited.isEmpty()) {
			for (User user : invited) {
				invitedUsers.add(UserDataFactory.createUserData(user));
			}
		}
		
		// existing collaborators
		existingCollaborators.clear();
		
		List<UserData> existing = goalsData.getCollaborators();
		
		if (existing != null && !existing.isEmpty()) {
			existingCollaborators.addAll(existing);
		}
		
		// recommended collaborators
		recommendedCollaborators.clear();
		recommendedCollaboratorsEdit.clear();
		
		List<UserData> recommended = goalsData.getRecommendedCollaborators();
		
		if (recommended != null && !recommended.isEmpty()) {
			recommended.removeAll(invitedUsers);
			recommended.removeAll(existingCollaborators);
			
			recommendedCollaborators.addAll(recommended);
			recommendedCollaboratorsEdit.addAll(recommended);
		}
		
		Collections.sort(recommendedCollaboratorsEdit);
		
		LearnBean goalsBean = PageUtil.getSessionScopedBean("learninggoals", LearnBean.class);
		
		if (goalsBean != null) {
			long targetGoalId = goalsBean.getSelectedGoalData().getData().getTargetGoalId();
			
			actionLogger.setLink("");
			actionLogger.logServiceUse(ComponentName.INVITE_GOAL_COLLABORATOR_DIALOG, 
					"context", "learn.targetGoal."+targetGoalId+".navigation");
		}
	}

	public void reset() {
		userSearchResults = new ArrayList<UserData>();  
		collabratorsToInvite = new ArrayList<UserData>();
		existingCollaborators = new ArrayList<UserData>();
		invitedUsers = new ArrayList<UserData>();
		recommendedCollaborators = new ArrayList<UserData>();
		recommendedCollaboratorsEdit = new ArrayList<UserData>();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void sendGoalInvites(){
		try {
			if (this.goalsData != null) {
				long targetGoalId = goalsData.getData().getTargetGoalId();
				
				TargetLearningGoal targetGoal = defaultManager.loadResource(TargetLearningGoal.class, targetGoalId);
				
				for (UserData collabToInviteData : collabratorsToInvite) {
					Request	invitation = requestManager.inviteToJoinResource(
							targetGoal, 
							loggedUser.getUserId(), 
							collabToInviteData.getId(),
							message);
					
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", "learn.targetGoal."+targetGoal.getId());
					parameters.put("targetGoalId", String.valueOf(targetGoal.getId()));
					parameters.put("user", String.valueOf(collabToInviteData.getId()));
					
					eventFactory.generateEvent(EventType.JOIN_GOAL_INVITATION, loggedUser.getUserId(), invitation, null, parameters);
					
					logger.debug("User "+loggedUser.getUserId()+" sent invitation to user "+collabToInviteData+" to join target learning goal "+targetGoalId);
				}
				
				PageUtil.fireSuccessfulInfoMessage(collabratorsToInvite.size() + (collabratorsToInvite.size() == 1 ? " goal invitation is sent!" : " goal invitations are sent!"));
			}
		} catch (EventException e) {
			logger.error(e.getMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		reset();
	}
	
	public void executeTextSearch(String toExcludeString) {
		long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);
		
		excludedUsersForSearch.clear();
		
		List<Long> totalListToExclude = new ArrayList<Long>(excludedUsersForSearch);
		totalListToExclude.add(loggedUser.getUserId());
		
		if (moreToExclude != null) {
			for (long l : moreToExclude) {
				totalListToExclude.add(l);
			}
		}
		
		for (UserData user : existingCollaborators) {
			totalListToExclude.add(user.getId());
		}
		
		for (UserData user : invitedUsers) {
			totalListToExclude.add(user.getId());
		}
		
		userSearchResults.clear();
		
		if (!keyword.isEmpty() && (keyword != null)) {
			TextSearchResponse usersResponse = userTextSearch.searchUsers(keyword.toString(), 0, 4, false, 
					totalListToExclude);	
			
			@SuppressWarnings("unchecked")
			List<User> result = (List<User>) usersResponse.getFoundNodes();
			
			for (User user : result) {
				UserData userData = UserDataFactory.createUserData(user);
				
				userSearchResults.add(userData);
			}
		}
	}
	
	public void addUser(UserData userData) {
		if (userData != null && !collabratorsToInvite.contains(userData)) {
			collabratorsToInvite.add(userData);
//			userData.setDisabled(true);
			
			recommendedCollaboratorsEdit.remove(userData);
		}
		
		userSearchResults.clear();
		keyword = "";
	}
	
	public void removeUser(UserData userData) {
		if (userData != null) {
			Iterator<UserData> iterator = this.collabratorsToInvite.iterator();
			
			while (iterator.hasNext()) {
				UserData u = (UserData) iterator.next();
				
				if (u.equals(userData)) {
//					u.setDisabled(false);
					iterator.remove();
					break;
				}
			}
			
			if (recommendedCollaborators.contains(userData)) {
				recommendedCollaboratorsEdit.add(userData);
				Collections.sort(recommendedCollaboratorsEdit);
			}
		}
		userSearchResults.clear();
	}
	
	public String getToExcludeIds() {
		Collection<Long> toExclude = new ArrayList<Long>();
		toExclude.addAll(ResourceDataUtil.getUserIds(existingCollaborators));
		toExclude.addAll(ResourceDataUtil.getUserIds(collabratorsToInvite));
		return toExclude.toString();
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<UserData> getUserSearchResults() {
		return userSearchResults;
	}
	
	public List<UserData> getCollabratorsToInvite() {
		return collabratorsToInvite;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<UserData> getRecommendedCollaboratorsEdit() {
		return recommendedCollaboratorsEdit;
	}

	public List<UserData> getExistingCollaborators() {
		return existingCollaborators;
	}

	public List<UserData> getInvitedUsers() {
		return invitedUsers;
	}
	
}
