package org.prosolo.web.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserSelectionData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "groupUsersBean")
@Component("groupUsersBean")
@Scope("view")
public class GroupUsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5524803558318260566L;

	protected static Logger logger = Logger.getLogger(GroupUsersBean.class);

	@Inject private TextSearch textSearch;
	@Inject private UserGroupManager userGroupManager;
	@Inject private EventFactory eventFactory;
	@Inject private LoggedUserBean loggedUserBean;
	
	private List<UserSelectionData> users;
	private List<Long> usersToRemoveFromGroup;
	private List<Long> usersToAddToGroup;

	private long groupId;
	
	// used for group search
	private String searchTerm = "";
	private int usersNumber;
	private int page = 1;
	private int limit = 10;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;

	public void init(long groupId) {
		this.groupId = groupId;
		this.page = 1;
		this.searchTerm = "";
		usersToRemoveFromGroup = new ArrayList<>();
		usersToAddToGroup = new ArrayList<>();
		loadUsers();
	}
	
	public void updateGroupUsers() {
		try {
			userGroupManager.updateGroupUsers(groupId, usersToAddToGroup, usersToRemoveFromGroup);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			UserGroup group = new UserGroup();
			group.setId(groupId);
			for(long id : usersToAddToGroup) {
				User user = new User();
				user.setId(id);
				eventFactory.generateEvent(EventType.ADD_USER_TO_GROUP, loggedUserBean.getUserId(), 
						user, group, page, lContext,
						service, null);
			}
			for(long id : usersToRemoveFromGroup) {
				User user = new User();
				user.setId(id);
				eventFactory.generateEvent(EventType.REMOVE_USER_FROM_GROUP, loggedUserBean.getUserId(), 
						user, group, page, lContext,
						service, null);
			}
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while updating group users");
		}
	}
	
	public void resetAndSearch() {
		this.page = 1;
		loadUsers();
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(usersNumber, limit, page, 
				1, "...");
		//if we want to generate all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

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
		if(this.page != page) {
			this.page = page;
			loadUsers();
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
		return usersNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
	}

	public void loadUsers() {
		try {
			TextSearchResponse1<UserSelectionData> res = textSearch.searchUsersInGroups(searchTerm, 
					page - 1, limit, groupId);
			usersNumber = (int) res.getHitsNumber();
			users = res.getFoundNodes();
			setCurrentlySelectedGroupUsers();
		} catch(Exception e) {
			logger.error(e);
		}
		generatePagination();
	}
	
	private void setCurrentlySelectedGroupUsers() {
		if(users != null) {
			for(UserSelectionData user : users) {
				if(user.isSelected()) {
					user.setSelected(!checkIfExists(user.getUser().getId(), usersToRemoveFromGroup));
				} else {
					user.setSelected(checkIfExists(user.getUser().getId(), usersToAddToGroup));
				}
			}
		}
	}

	private boolean checkIfExists(long id, List<Long> list) {
		return indexOf(id, list) >= 0;
	}
	
	private int indexOf(long id, List<Long> list) {
		if(list == null) {
			return -1;
		}
		int index = 0;
		for(Long l : list) {
			if(id == l) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public void userGroupAssignChecked(int index) {
		UserSelectionData user = users.get(index);
		if(user.isSelected()) {
			int ind = indexOf(user.getUser().getId(), usersToRemoveFromGroup);
			if(ind >= 0) {
				usersToRemoveFromGroup.remove(ind);
			} else {
				usersToAddToGroup.add(user.getUser().getId());
			}
		} else {
			int ind = indexOf(user.getUser().getId(), usersToAddToGroup);
			if(ind >= 0) {
				usersToAddToGroup.remove(ind);
			} else {
				usersToRemoveFromGroup.add(user.getUser().getId());
			}
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public List<UserSelectionData> getUsers() {
		return users;
	}

	public void setUsers(List<UserSelectionData> users) {
		this.users = users;
	}
	
}
