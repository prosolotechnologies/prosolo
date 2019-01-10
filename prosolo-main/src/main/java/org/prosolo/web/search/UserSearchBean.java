package org.prosolo.web.search;

import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "userSearchBean")
@Component("userSearchBean")
@Scope("view")
public class UserSearchBean implements Serializable {
	
	private static final long serialVersionUID = -1442084449890830994L;
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private UserTextSearch userTextSearch;
	@Inject private LoggingNavigationBean loggingNavigationBean;
	
	private String query;
	//TODO we should make transition to the approach we are using in other places for learning context
	private String page;
	private String context;
	private List<UserData> users;
	private int userSize;
	
	public UserSearchBean() {
		users = new ArrayList<>();
	}

	public void search() {
		List<Long> excludeUsers = new ArrayList<>();
		excludeUsers.add(loggedUser.getUserId());
		
		search(query, excludeUsers);
	}
	
	public void search(String searchQuery, List<Long> excludeUsers) {
		this.users.clear();
		this.userSize = 0;

		fetchUsers(searchQuery, excludeUsers);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			//TODO learning context is retrieved in logServiceUse method
			//PageContextData lcd = new PageContextData(page, context, null);
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_PEOPLE, 
					searchQuery);
		}
	}
 
	private void fetchUsers(String query, List<Long> excludeUsers) {
		PaginatedResult<UserData> usersResponse = userTextSearch.searchUsers(loggedUser.getOrganizationId(), query,
		0, 0, false, excludeUsers);

		if (usersResponse != null) {
			this.userSize = (int) usersResponse.getHitsNumber();
			this.users = usersResponse.getFoundNodes();
		} else {
			this.userSize = 0;
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

	public void setQuery(String query) {
		this.query = query;
	}

	public List<UserData> getUsers() {
		return users;
	}

	public int getUserSize() {
		return userSize;
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
