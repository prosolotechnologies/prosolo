package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "userSearchBean")
@Component("userSearchBean")
@Scope("view")
public class UserSearchBean implements Serializable {
	
	private static final long serialVersionUID = -1442084449890830994L;
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private UserTextSearch userTextSearch;
	@Inject private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	private String page;
	private String context;
	private List<UserData> users;
	private int userSize;
	
	public UserSearchBean() {
		users = new ArrayList<UserData>();
	}
	
	public void searchUsers() {
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUserId());
		
		searchUsers(query, excludeUsers);
	}
	
	public void searchUsers(String searchQuery) {
		List<Long> excludeUsers = new ArrayList<Long>();
		excludeUsers.add(loggedUser.getUserId());
		
		searchUsers(searchQuery, excludeUsers);
	}
	
	public void searchUsers(String searchQuery, List<Long> excludeUsers) {
		this.users.clear();
		this.userSize = 0;

		fetchUsers(searchQuery, excludeUsers);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			LearningContextData lcd = new LearningContextData(page, context, null);
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_PEOPLE, 
					searchQuery,
					lcd);
		}
		
		searchQuery = null;
	}
 
	public void fetchUsers(String query, List<Long> excludeUsers) {
		PaginatedResult<UserData> usersResponse = userTextSearch.getUsersWithRoles(
				query, 0, 0, false, 0, null, false, excludeUsers);
		
		if (usersResponse != null) {
			this.userSize = (int) usersResponse.getHitsNumber();
			this.users = usersResponse.getFoundNodes();
		} else {
			this.userSize=0;
		}
		
	}
	
	public void resetSearch() {
		users = new ArrayList<>();
		query = "";
		userSize = 0;
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
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	
}
