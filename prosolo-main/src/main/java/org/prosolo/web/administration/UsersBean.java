package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "adminUsers")
@Component("adminUsers")
@Scope("view")
public class UsersBean implements Serializable, Paginable {

	private static final long serialVersionUID = 138952619791500473L;

	protected static Logger logger = Logger.getLogger(UsersBean.class);

	@Autowired private UserManager userManager;
	@Autowired private UserEntityESService userEntityESService;
	@Inject private TextSearch textSearch;

	private List<UserData> users;
	
	private UserData userToDelete;

	// used for search
	private String searchTerm = "";
	private int usersNumber;
	private int page = 1;
	private int limit = 10;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;

	@PostConstruct
	public void init() {
		logger.debug("initializing");
		loadUsers();
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

	public void delete() {
		if (userToDelete != null) {
			try {
				User user = userManager.loadResource(User.class, this.userToDelete.getId());
				user.setDeleted(true);
				userManager.saveEntity(user);
				
				userEntityESService.deleteNodeFromES(user);
				users.remove(userToDelete);
				PageUtil.fireSuccessfulInfoMessage("User " + userToDelete.getName()+" "+userToDelete.getLastName()+" is deleted.");
				userToDelete = null;
			} catch (Exception ex) {
				logger.error(ex);
				PageUtil.fireErrorMessage("Error while trying to delete user");
			}
		}
	}

	public void loadUsers() {
		this.users = new ArrayList<UserData>();
		TextSearchResponse1<UserData> res = textSearch.getUsersWithRoles(searchTerm, page - 1, limit, true);
		usersNumber = (int) res.getHitsNumber();
		users = res.getFoundNodes();
		generatePagination();
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getUsers() {
		return this.users;
	}

	public UserData getUserToDelete() {
		return userToDelete;
	}

	public void setUserToDelete(UserData userToDelete) {
		this.userToDelete = userToDelete;
	}

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
	
}
