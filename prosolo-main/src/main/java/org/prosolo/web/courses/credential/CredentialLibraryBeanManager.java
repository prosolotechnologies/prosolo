/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialSearchFilter;
import org.prosolo.search.util.credential.CredentialSortOption;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialLibraryBeanManager")
@Component("credentialLibraryBeanManager")
@Scope("view")
public class CredentialLibraryBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = -7737382507101880012L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBeanManager.class);

	@Inject private TextSearch textSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credentialManager;
	@Inject private LoggingService loggingService;

	private List<CredentialData> credentials;
	
	//search
	private String searchTerm = "";
	private int credentialsNumber;
	private int page = 1;
	private int limit = 10;
	private CredentialSearchFilter searchFilter = CredentialSearchFilter.ALL;
	private CredentialSortOption sortOption = CredentialSortOption.ALPHABETICALLY;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
	
	private CredentialSortOption[] sortOptions;
	private CredentialSearchFilter[] searchFilters;

	private String context = "name:library";

	public void init() {
		sortOptions = CredentialSortOption.values();
		searchFilters = Arrays.stream(CredentialSearchFilter.values()).filter(
				f -> shouldIncludeSearchFilter(f))
				.toArray(CredentialSearchFilter[]::new);
		searchCredentials(false);
	}
	
	public boolean shouldIncludeSearchFilter(CredentialSearchFilter f) {
		boolean isInstructor = loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS");
		boolean canCreateContent = loggedUserBean.hasCapability("MANAGE.CONTENT.EDIT");
		return f != CredentialSearchFilter.BY_OTHER_STUDENTS &&
				 (f != CredentialSearchFilter.YOUR_CREDENTIALS || isInstructor) &&
				 (f != CredentialSearchFilter.FROM_CREATOR || canCreateContent);
	}
	
	public void searchCredentials(boolean userSearch) {
		try {
			getCredentialSearchResults();
			generatePagination();
			
			if(userSearch) {
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				LearningContextData lcd = new LearningContextData(page, context, null);
				Map<String, String> params = new HashMap<>();
				params.put("query", searchTerm);
				try {
					loggingService.logServiceUse(loggedUserBean.getUserId(), 
							ComponentName.SEARCH_CREDENTIALS, 
							params, loggedUserBean.getIpAddress(), lcd);
				} catch(Exception e) {
					logger.error(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void resetAndSearch() {
		this.page = 1;
		searchCredentials(true);
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(credentialsNumber, limit, page, 
				1, "...");
		//if we want to genearte all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
		logger.info("Number of pages for credential search: " + numberOfPages);
	}

	public void getCredentialSearchResults() {
		TextSearchResponse1<CredentialData> response = textSearch.searchCredentialsForManager(
				searchTerm, page - 1, limit, loggedUserBean.getUserId(), searchFilter, sortOption);
		credentialsNumber = (int) response.getHitsNumber();
		credentials = response.getFoundNodes();
	}
	
	public void applySearchFilter(CredentialSearchFilter filter) {
		this.searchFilter = filter;
		this.page = 1;
		searchCredentials(true);
	}
	
	public void applySortOption(CredentialSortOption sortOption) {
		this.sortOption = sortOption;
		this.page = 1;
		searchCredentials(true);
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
			searchCredentials(true);
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
		return credentialsNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
	}
	
	/*
	 * ACTIONS
	 */
	
	public void bookmarkCredential(CredentialData cred) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData context = new LearningContextData(page, lContext, service);
			if(cred.isBookmarkedByCurrentUser()) {
				credentialManager.deleteCredentialBookmark(cred.getId(), 
						loggedUserBean.getUserId(), context);
			} else {
				credentialManager.bookmarkCredential(cred.getId(), loggedUserBean.getUserId(),
						context);
			}
			cred.setBookmarkedByCurrentUser(!cred.isBookmarkedByCurrentUser());
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public CredentialSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CredentialSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public void setCredentials(List<CredentialData> credentials) {
		this.credentials = credentials;
	}

	public List<CredentialData> getCredentials() {
		return credentials;
	}

	public CredentialSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(CredentialSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public CredentialSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CredentialSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public CredentialSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
}
