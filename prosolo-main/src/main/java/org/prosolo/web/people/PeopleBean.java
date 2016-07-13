package org.prosolo.web.people;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.settings.ProfileSettingsBean;
import org.prosolo.web.util.PageUtil;
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

	protected static Logger logger = Logger.getLogger(PeopleBean.class);

	@Autowired
	private FollowResourceManager followResourceManager;
	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private CollaboratorsRecommendation cRecommendation;

	private List<UserData> usersToFollow;
	private List<UserData> followingUsers;

	private int usersNumber;
	private final int limit = 5;
	private int page = 1;
	private int numberOfPages;
	private List<PaginationLink> paginationLinks;

	@PostConstruct
	public void initPeopleBean() {
		initFollowingUsers();
	}

	public void initFollowingUsers() {
		try {
			followingUsers = new ArrayList<UserData>();
			usersNumber = followResourceManager.getNumberOfFollowingUsers(loggedUser.getUser());

			List<User> followingUsersList = usersNumber > 0
					? followResourceManager.getFollowingUsers(loggedUser.getUser(), page - 1, limit)
					: new ArrayList<User>();

			if (followingUsersList != null && !followingUsersList.isEmpty()) {
				for (User user : followingUsersList) {
					UserData userData = UserDataFactory.createUserData(user);
					followingUsers.add(userData);
				}
			}
			generatePagination();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		try {
			usersToFollow = new ArrayList<UserData>();
			List<User> usersToFollowList = cRecommendation
					.getRecommendedCollaboratorsBasedOnLocation(loggedUser.getUser(), 3);

			if (usersToFollowList != null && !usersToFollowList.isEmpty()) {
				for (User user : usersToFollowList) {
					UserData userData = UserDataFactory.createUserData(user);
					usersToFollow.add(userData);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
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

	public List<UserData> getUsersToFollow() {
		return usersToFollow;
	}

	public void setUsersToFollow(List<UserData> usersToFollow) {
		this.usersToFollow = usersToFollow;
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
