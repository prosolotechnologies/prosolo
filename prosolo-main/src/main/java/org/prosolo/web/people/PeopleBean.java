package org.prosolo.web.people;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Musa Paljos"
 * 
 */
@ManagedBean(name = "peopleBean")
@Component("peopleBean")
@Scope("view")
public class PeopleBean implements Paginable, Serializable {

	private static final long serialVersionUID = -5592166239184029819L;

	protected static Logger logger = Logger.getLogger(PeopleBean.class);

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private PeopleActionBean peopleActionBean;
	@Inject
	private FollowResourceManager followResourceManager;
	@Inject private UserTextSearch userTextSearch;

	private List<UserData> followingUsers;

	private String searchTerm = "";
	private PaginationData paginationData = new PaginationData(5);

	public void init() {
		initFollowingUsers();
	}

	private void initFollowingUsers() {
		try {
//			searchPeopleUserFollows();
			followingUsers = new ArrayList<UserData>();
			paginationData.update(followResourceManager.getNumberOfFollowingUsers(loggedUser.getUserId()));

			List<User> followingUsersList = paginationData.getNumberOfResults() > 0
					? followResourceManager.getFollowingUsers(loggedUser.getUserId(), paginationData.getPage() - 1, paginationData.getLimit())
					: new ArrayList<User>();

			if (followingUsersList != null && !followingUsersList.isEmpty()) {
				for (User user : followingUsersList) {
					UserData userData = new UserData(user);
					followingUsers.add(userData);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	public void followCollegueById(String userToFollowName, long userToFollowId) {
		peopleActionBean.followCollegueById(userToFollowName, userToFollowId);
		
		init();
	}
	
	public void unfollowCollegueById(String userToUnfollowName, long userToUnfollowId) {
		peopleActionBean.unfollowCollegueById(userToUnfollowName, userToUnfollowId);
		
		init();
	}

	public void addFollowingUser(UserData user) {
		if (followingUsers != null && !followingUsers.contains(user)) {
			followingUsers.add(user);
		}
	}

	public void removeFollowingUserById(long userId) {
		Iterator<UserData> iterator = followingUsers.iterator();

		while (iterator.hasNext()) {
			UserData u = (UserData) iterator.next();

			if (u.getId() == userId) {
				iterator.remove();
				break;
			}
		}
	}
	
	public void resetAndSearch() {
		paginationData.setPage(1);
		searchPeopleUserFollows();
	}
	
	public void searchPeopleUserFollows() {
		try {
			if (followingUsers != null) {
				this.followingUsers.clear();
			}

			fetchFollowingUsers();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void fetchFollowingUsers() {
		TextSearchResponse1<UserData> searchResponse = userTextSearch.searchPeopleUserFollows(
				searchTerm, 
				paginationData.getPage() - 1, paginationData.getLimit(), 
				loggedUser.getUserId());

		paginationData.setNumberOfResults((int) searchResponse.getHitsNumber());
		followingUsers = searchResponse.getFoundNodes();
	}

	// pagination helper methods
	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			initFollowingUsers();
		}
	}

	public List<UserData> getFollowingUsers() {
		return followingUsers;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
}
