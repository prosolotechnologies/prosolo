package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.search.TextSearch;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.SearchCompetencesBean;
import org.prosolo.web.search.SearchCoursesBean;
import org.prosolo.web.search.SearchGoalsBean;
import org.prosolo.web.search.SearchPeopleBean;
import org.prosolo.web.search.data.CompetenceData;
import org.prosolo.web.search.data.LearningGoalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic
 * @date Jun 18, 2012
 */
@ManagedBean(name = "globalSearchBean")
@Component("globalSearchBean")
@Scope("request")
public class GlobalSearchBean implements Serializable {

	private static final long serialVersionUID = 6338913685363216486L;
	
	private static Logger logger = Logger.getLogger(GlobalSearchBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private DefaultManager defaultManager;
	@Autowired private CourseManager courseManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private SearchCoursesBean searchCoursesBean;
	@Autowired private SearchGoalsBean searchGoalsBean;
	@Autowired private SearchCompetencesBean searchCompetencesBean;
	@Autowired private SearchPeopleBean searchPeopleBean;

	private String query;
	private Collection<UserData> foundUsers;
	private Collection<LearningGoalData> foundLearningGoals;
	private Collection<CourseData> foundCourses;
	private List<CompetenceData> foundCompetences;

	private int elementsPerGroup = 2;
	private int usersSize;
	private int goalsSize;
	private int coursesSize;
	private int competencesSize;
	
	public GlobalSearchBean() {
		foundUsers = new ArrayList<UserData>();
		foundLearningGoals = new ArrayList<LearningGoalData>();
		foundCourses = new ArrayList<CourseData>();
		foundCompetences = new ArrayList<CompetenceData>();
	}
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public void executeSearch(){
		loggingNavigationBean.logServiceUse(
				ComponentName.GLOBAL_SEARCH,
				"query", query);
		
		if (query != null && !query.isEmpty()) {
			searchUsers(query);
			searchLearningGoals(query);
			searchCourses(query);
			searchCompetences(query);
		}
	}
	
	@Deprecated
	public void searchBoxListener(ValueChangeEvent event) {
		query = event.getNewValue().toString();
		/*Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("query", query);
		loggingNavigationBean.logServiceUse(loggedUser.getUser(), ComponentName.GLOBAL_SEARCH, null, parameters);
		
		if (!query.isEmpty() && (query != null)) {
			searchUsers(query);
			searchLearningGoals(query);
			searchCourses(query);
			searchCompetences(query);
		}*/
	}

	private void searchUsers(String query){
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUser().getId());
		
		// reset previous search result
		searchPeopleBean.setPage(0);
		searchPeopleBean.setUserSize(0);
		searchPeopleBean.getUsers().clear();
		
		searchPeopleBean.fetchUsers(
				query, 
				excludeUsers, 
				Settings.getInstance().config.application.globalSearchItemsNumber, 
				false);
	}
	
	private void searchCourses(String query){
		// reset previous search result
		searchCoursesBean.setSize(0);
		searchCoursesBean.setPage(0);
		searchCoursesBean.getCourses().clear();
		
		searchCoursesBean.fetchCourses(
				query, 
				null,
				null,
				Settings.getInstance().config.application.globalSearchItemsNumber,
				false,
				true);
 	}
	
	private void searchCompetences(String query){
		// reset previous search result
		searchCompetencesBean.setCompsSize(0);
		searchCompetencesBean.setPage(0);
		searchCompetencesBean.getCompetences().clear();
		
		searchCompetencesBean.fetchCompetences(
				query, 
				null, 
				Settings.getInstance().config.application.globalSearchItemsNumber, 
				false);
	}
	
	private void searchLearningGoals(String searchWord) {
		// reset previous search data
		searchGoalsBean.setGoalsSize(0);
		searchGoalsBean.setPage(0);
		searchGoalsBean.getGoals().clear();
		
		searchGoalsBean.fetchGoals(query, null, Settings.getInstance().config.application.globalSearchItemsNumber, false);
	}

	public boolean isHasMoreGlobalResults() {
		return (searchPeopleBean.getUserSize() > elementsPerGroup + 1) || 
				(searchGoalsBean.getGoalsSize() > elementsPerGroup + 1) || 
				(searchCoursesBean.getSize() > elementsPerGroup + 1) || 
				(searchCompetencesBean.getCompsSize() > elementsPerGroup + 1);
	}

	public boolean isHasGlobalResults() {
		return searchPeopleBean.getUserSize() > 0 || 
				searchGoalsBean.getGoalsSize() > 0 || 
				searchCoursesBean.getSize() > 0 || 
				searchCompetencesBean.getCompsSize() > 0;
	}

	/* 
	 * GETTERS / SETTERS
	 */
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public int getUsersSize() {
		return usersSize;
	}

	public Collection<LearningGoalData> getFoundLearningGoals() {
		return foundLearningGoals;
	}

	public LoggedUserBean getLoggedUser() {
		return loggedUser;
	}

	public Collection<UserData> getFoundUsers() {
		return foundUsers;
	}

	public int getElementsPerGroup() {
		return elementsPerGroup;
	}

	public int getGoalsSize() {
		return goalsSize;
	}

	public Collection<CourseData> getFoundCourses() {
		return foundCourses;
	}

	public void setFoundCourses(Collection<CourseData> foundCourses) {
		this.foundCourses = foundCourses;
	}

	public int getCoursesSize() {
		return coursesSize;
	}

	public void setCoursesSize(int coursesSize) {
		this.coursesSize = coursesSize;
	}
	
	public List<CompetenceData> getFoundCompetences() {
		return foundCompetences;
	}

	public void setFoundCompetences(List<CompetenceData> foundCompetences) {
		this.foundCompetences = foundCompetences;
	}

	public int getCompetencesSize() {
		return competencesSize;
	}

	public void setCompetencesSize(int competencesSize) {
		this.competencesSize = competencesSize;
	}
	
}
