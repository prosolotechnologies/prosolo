package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.LearningGoalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchGoalsBean")
@Component("searchGoalsBean")
@Scope("view")
public class SearchGoalsBean implements Serializable{

	private static final long serialVersionUID = 7425398428736202443L;
	
	private static Logger logger = Logger.getLogger(SearchGoalsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	private String context;
	private List<LearningGoalData> goals;
	private int goalsSize;
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	public SearchGoalsBean() {
		goals = new ArrayList<LearningGoalData>();
	}
	
	public void executeSearch(){
		this.limit = 3;		
		searchLearningGoals(this.query, null, false);
	}
	@Deprecated
	public void searchLearningGoalsListener(ValueChangeEvent event) {
		this.limit = 3;
		searchLearningGoals(event.getNewValue().toString(), null, false);
	}
	
	public void searchAllLearningGoals() {
		searchLearningGoals(query, null, true);
	}
	
	public void searchLearningGoals(String searchQuery) {
		searchLearningGoals(searchQuery, null, false);
	}
	
	public void searchLearningGoals(String searchQuery, 
			Collection<LearningGoal> goalsToExclude, 
			boolean loadOneMore) {
		
		this.goals.clear();
		this.goalsSize = 0;
		fetchGoals(searchQuery, goalsToExclude, this.limit, loadOneMore);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_GOALS, 
					"query", searchQuery,
					"context", context);
		}
	}
	
	public void loadMoreGoals() {
		page++;
		
		fetchGoals(query, null, this.limit, true);
		
		loggingNavigationBean.logServiceUse(
				ComponentName.SEARCH_GOALS, 
				"query", query,
				"context", "search.goals");
	}

	public void fetchGoals(String searchQuery, Collection<LearningGoal> goalsToExclude, int limit, boolean loadOneMore) {
//		 TextSearchResponse searchResponse = textSearch.searchLearningGoals(
//			searchQuery,
//			this.page, 
//			limit,
//			loadOneMore,
//			goalsToExclude);
//		 
//		@SuppressWarnings("unchecked")
//		List<LearningGoal> foundGoals = (List<LearningGoal>) searchResponse.getFoundNodes();
//		this.goalsSize = (int) searchResponse.getHitsNumber();
//		
//		// if there is more than limit, set moreToLoad to true
//		if (loadOneMore && foundGoals.size() == limit+1) {
//			foundGoals = foundGoals.subList(0, foundGoals.size()-1);
//			moreToLoad = true;
//		} else {
//			moreToLoad = false;
//		}
//		
//		goals.addAll(convertToGoalData(foundGoals, loggedUser.getUserId()));
//		
//		if(foundGoals != null && !foundGoals.isEmpty()) {
//			Map<Long, List<Long>> counts = learningGoalManager.getCollaboratorsOfLearningGoals(foundGoals);
//			
//			if (counts != null) {
//				for (LearningGoalData courseData : this.goals) {
//					List<Long> memberIds = counts.get(courseData.getId());
//					
//					if (memberIds != null) {
//						courseData.setMemberIds(memberIds);
//					}
//				}
//			}
//		}
	}

	public boolean hasMoreLearningGoals() {
		return goalsSize > limit + 1;
	}
	
	public String retrieveGoalMembersNumberById(long goalId) {
		if (goalId > 0) {
			Integer membersNumber = learningGoalManager.retrieveCollaboratorsNumber(goalId, loggedUser.getUserId());
			
			if (membersNumber == 0) {
				return "No members yet";
			} else if (membersNumber == 1) {
				return "1 member";
			} else
				return membersNumber + " members";
		} else {
			logger.error("Could not retrieve goal members as the goal sent is null");
			return "";
		}
	}
	
	/*
	 * Utility
	 */
	public List<LearningGoalData> convertToGoalData(List<LearningGoal> goals, long userId) {
		List<LearningGoalData> goalsData = new ArrayList<LearningGoalData>();
		
		if (goals != null && !goals.isEmpty()) {
			for (LearningGoal goal : goals) {
				LearningGoalData learningGoalData = new LearningGoalData(goal);
				
				try {
					learningGoalData.setCanBeRequestedToJoin(learningGoalManager.canUserJoinGoal(goal.getId(), userId));
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
				learningGoalData.setFreeToJoin(goal.isFreeToJoin());
				goalsData.add(learningGoalData);
			}
		}
		return goalsData;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public List<LearningGoalData> getGoals() {
		return goals;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getGoalsSize() {
		return goalsSize;
	}
	
	public void setGoalsSize(int goalsSize) {
		this.goalsSize = goalsSize;
	}
	
	public void setPage(int page) {
		this.page = page;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
}
