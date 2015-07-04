package org.prosolo.web.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.prosolo.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.util.string.StringUtil;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.util.ResourceDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author Zoran Jeremic Feb 1, 2014
 */
@ManagedBean(name = "logsFilterBean")
@Component("logsFilterBean")
@Scope("view")
public class LogsFilterBean implements Serializable {

	private static final long serialVersionUID = -5452146059271122076L;

	@Autowired private TextSearch textSearch;
	@Autowired private LoggingDBManager loggingDBManager;
	
	private List<UserData> usersList;
	private String searchText;
	private boolean twitterPosts;
	private List<UserData> userSearchResults;
	private List<String> eventTypes;
	private List<String> selectedEventTypes;
	private List<String> objectTypes;
	private List<String> selectedObjectTypes;
	private Date startDate;
	private Date endDate;
	
	@PostConstruct
	public void init(){
		System.out.println("Logs filter bean...init");
		usersList = new ArrayList<UserData>();
		userSearchResults = new ArrayList<UserData>();
		setEventTypes(loggingDBManager.getAllDistinctValuesOfEventField("eventType"));
		setObjectTypes(loggingDBManager.getAllDistinctValuesOfEventField("objectType"));
		selectedEventTypes = new ArrayList<String>();
		selectedObjectTypes = new ArrayList<String>();
		startDate = null;
		endDate = null;
	}
	
	public void executeTextSearch(String toExcludeString) {
		long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);
		
		List<Long> totalListToExclude = new ArrayList<Long>();
		
		if (moreToExclude != null) {
			for (long l : moreToExclude) {
				totalListToExclude.add(l);
			}
		}
		
		userSearchResults.clear();
		TextSearchResponse usersResponse = textSearch.searchUsers(searchText, 0, 4, false, totalListToExclude);	
		
		@SuppressWarnings("unchecked")
		List<User> result = (List<User>) usersResponse.getFoundNodes();
		
		for (User user : result) {
			UserData userData = new UserData(user);
			userSearchResults.add(userData);
		}
	}
	
	public void addUser(UserData userData) {
		userSearchResults.clear();
		searchText = "";
		
		if (!usersList.contains(userData)) {
			usersList.add(userData);
		}
	}

	public void removeUser(UserData userData) {
		if (usersList.contains(userData)) {
			usersList.remove(userData);
		}
	}
	
	public String getToExcludeIds() {
		return ResourceDataUtil.getUserIds(usersList).toString();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public List<UserData> getUsersList() {
		return usersList;
	}

	public void setUsersList(List<UserData> usersList) {
		this.usersList = usersList;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public List<UserData> getUserSearchResults() {
		return userSearchResults;
	}

	public void setUserSearchResults(List<UserData> userSearchResults) {
		this.userSearchResults = userSearchResults;
	}

	public boolean isTwitterPosts() {
		return twitterPosts;
	}

	public void setTwitterPosts(boolean twitterPosts) {
		this.twitterPosts = twitterPosts;
	}

	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> getSelectedEventTypes() {
		return selectedEventTypes;
	}

	public void setSelectedEventTypes(List<String> selectedEventTypes) {
		this.selectedEventTypes = selectedEventTypes;
	}

	public List<String> getObjectTypes() {
		return objectTypes;
	}

	public void setObjectTypes(List<String> objectTypes) {
		this.objectTypes = objectTypes;
	}

	public List<String> getSelectedObjectTypes() {
		return selectedObjectTypes;
	}

	public void setSelectedObjectTypes(List<String> selectedObjectTypes) {
		this.selectedObjectTypes = selectedObjectTypes;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
