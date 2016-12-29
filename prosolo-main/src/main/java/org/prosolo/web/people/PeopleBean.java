package org.prosolo.web.people;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
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
	private TextSearch textSearch;

	private List<UserData> followingUsers;

	private int usersNumber;
	private final int limit = 5;
	private int page = 1;
	private int numberOfPages;
	private List<PaginationLink> paginationLinks;
	
	private String searchTerm = "";

	public void init() {
		initFollowingUsers();
	}

	private void initFollowingUsers() {
		try {
//			followingUsers = new ArrayList<UserData>();
//			usersNumber = followResourceManager.getNumberOfFollowingUsers(loggedUser.getUserId());
//
//			List<User> followingUsersList = usersNumber > 0
//					? followResourceManager.getFollowingUsers(loggedUser.getUserId(), page - 1, limit)
//					: new ArrayList<User>();
//
//			if (followingUsersList != null && !followingUsersList.isEmpty()) {
//				for (User user : followingUsersList) {
//					UserData userData = new UserData(user);
//					followingUsers.add(userData);
//				}
//			}
			searchPeopleUserFollows();
			generatePagination();
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
		this.page = 1;
		searchPeopleUserFollows();
	}
	
	public void searchPeopleUserFollows() {
		try {
			if (followingUsers != null) {
				this.followingUsers.clear();
			}

			fetchFollowingUsers();
			generatePagination();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void fetchFollowingUsers() {
		TextSearchResponse1<UserData> searchResponse = textSearch.searchPeopleUserFollows(
				searchTerm, 
				page - 1, limit, 
				loggedUser.getUserId());

		usersNumber = (int) searchResponse.getHitsNumber();
		followingUsers = searchResponse.getFoundNodes();
	}

	public void generatePagination() {
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
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
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

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
}
