package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchPeopleBean")
@Component("searchPeopleBean")
@Scope("view")
public class SearchPeopleBean implements Serializable{
	
	private static final long serialVersionUID = 859431879196489582L;
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	private String context;
	private List<UserData> users;
	private int userSize;
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	public SearchPeopleBean() {
		users = new ArrayList<UserData>();
	}
	
	public void executeSearch(){
		this.limit = 3;
		searchPeople(this.query);
	}
	
	@Deprecated
	public void searchPeopleListener(ValueChangeEvent event) {
		this.limit = 3;
		searchPeople(event.getNewValue().toString());
	}
	
	public void searchPeople() {
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUser().getId());
		
		searchPeople(query, excludeUsers, true);
	}
	
	public void searchPeople(String searchQuery) {
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUser().getId());
		
		searchPeople(searchQuery, excludeUsers, true);
	}
	
	public void searchPeople(String searchQuery, List<Long> excludeUsers, boolean loadOneMore) {
		this.users.clear();
		this.userSize = 0;
		this.page = 0;

		fetchUsers(searchQuery, excludeUsers, this.limit, loadOneMore);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_PEOPLE, 
					"query", searchQuery,
					"context", this.context);
		}
		
		searchQuery = null;
	}
	
	public void refreshCurrentSearch() {
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUser().getId());
		TextSearchResponse usersResponse=textSearch.searchUsers(
				query,
				-1, 
				limit,
				false,
				excludeUsers);
		
		@SuppressWarnings("unchecked")
		List<User> foundUsers =  (List<User>) usersResponse.getFoundNodes();
		
		users.clear();
		users.addAll(convertToUserData(foundUsers));
	}
	
	public void loadMoreUsers() {
		page++;
		
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUser().getId());
		
		fetchUsers(query, excludeUsers, this.limit, true);
	}
 
	public void fetchUsers(String query, List<Long> excludeUsers, int limit, boolean loadOneMore) {
		TextSearchResponse usersResponse = textSearch.searchUsers(
				query, 
				this.page, 
				limit, 
				loadOneMore,
				excludeUsers);
		
		if (usersResponse != null) {
			@SuppressWarnings("unchecked")
			List<User> 	foundUsers = (List<User>) usersResponse.getFoundNodes();
			this.userSize = (int) usersResponse.getHitsNumber();
			
			if (loadOneMore && foundUsers.size() == limit + 1) {
				foundUsers = foundUsers.subList(0, foundUsers.size() - 1);
				moreToLoad = true;
			} else {
				moreToLoad = false;
			}

			users.addAll(convertToUserData(foundUsers));
		}else{
			this.userSize=0;
			moreToLoad=false;
		}
	 
		// if there is more than limit, set moreToLoad to true
		
	}

	public boolean hasMoreUsers() {
		return userSize > limit + 1;
	}
	
	/*
	 * Utility
	 */
	public List<UserData> convertToUserData(Collection<User> users) {
		List<UserData> usersData = new ArrayList<UserData>();
		
		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				UserData userData = UserDataFactory.createUserData(user);
				userData.setFollowed(followResourceManager.isUserFollowingUser(loggedUser.getUser(), user));
				usersData.add(userData);
			}
		}
		return usersData;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getQuery() {
		return query;
	}

	public List<UserData> getUsers() {
		return users;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getUserSize() {
		return userSize;
	}
	
	public void setUserSize(int userSize) {
		this.userSize = userSize;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
}
