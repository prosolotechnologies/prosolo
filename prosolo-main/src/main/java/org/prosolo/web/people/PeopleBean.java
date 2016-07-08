package org.prosolo.web.people;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.settings.ProfileSettingsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Musa Paljos"
 * 
 */
@ManagedBean(name = "peopleBean")
@Component("peopleBean")
@Scope("view")
public class PeopleBean implements Serializable, Paginable {

	private static final long serialVersionUID = 1649841825781113183L;

	protected static Logger logger = Logger.getLogger(ProfileSettingsBean.class);

	@Autowired
	private FollowResourceManager followResourceManager;
	@Autowired
	private FollowResourceAsyncManager followResourceAsyncManager;
	@Autowired
	private LoggedUserBean loggedUser;

	private List<UserData> whoToFollowList;
	private List<UserData> followingUsers;

	private int usersNumber;
	private final int limit = 5;
	private int page = 1;
	private int numberOfPages;
	private List<PaginationLink> paginationLinks;

	public void initPeopleBean() {
		initFollowingUsers();
	}

	public void initFollowingUsers() {
		try {

			followingUsers = new ArrayList<UserData>();
			logger.debug("Initializing following users for a user '" + loggedUser.getUserId() + "'");

			usersNumber = followResourceManager.getNumberOfFollowingUsers(loggedUser.getUserId());

			List<User> followingUsersList = usersNumber > 0
					? followResourceManager.getFollowingUsers(loggedUser.getUserId(), page - 1, limit) : new ArrayList();

			if (followingUsersList != null && !followingUsersList.isEmpty()) {
				for (User user : followingUsersList) {
					UserData userData = new UserData(user);
					followingUsers.add(userData);
				}
				logger.debug("Following users initialized '" + loggedUser.getUserId() + "'");
			}
			generatePagination();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException(e.getMessage());
		}
	}

	public void addFollowingUser(UserData user) {
		if (!followingUsers.contains(user)) {
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

	private void generatePagination() {
		// if we don't want to generate all links
		Paginator paginator = new Paginator(usersNumber, limit, page, 1, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
		logger.info("Number of pages for following users: " + numberOfPages);
	}

	// pagination helper methods

	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}

	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}

	@Override
	public void changePage(int page) {
		if (this.page != page) {
			this.page = page;
			initFollowingUsers();
		}
	}

	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}

	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}

	@Override
	public boolean isResultSetEmpty() {
		return numberOfPages == 0;
	}

	public List<UserData> getWhoToFollowList() {
		return whoToFollowList;
	}

	public void setWhoToFollowList(List<UserData> whoToFollowList) {
		this.whoToFollowList = whoToFollowList;
	}

	public List<UserData> getFollowingUsers() {
		return followingUsers;
	}

	public void setFollowingUsers(List<UserData> followingUsers) {
		this.followingUsers = followingUsers;
	}

	public int getUsersNumber() {
		return usersNumber;
	}

	public void setUsersNumber(int usersNumber) {
		this.usersNumber = usersNumber;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public int getLimit() {
		return limit;
	}

}
