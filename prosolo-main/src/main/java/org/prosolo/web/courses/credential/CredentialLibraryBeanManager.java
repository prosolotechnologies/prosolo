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
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialLibraryBeanManager")
@Component("credentialLibraryBeanManager")
@Scope("view")
public class CredentialLibraryBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = -7737382507101880012L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBeanManager.class);

	@Inject private CredentialTextSearch credentialTextSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credentialManager;
	@Inject private LoggingService loggingService;

	private List<CredentialData> credentials;
	
	//search
	private String searchTerm = "";
	private LearningResourceSearchFilter searchFilter = LearningResourceSearchFilter.ALL;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private LearningResourceSearchFilter[] searchFilters;

	private String context = "name:library";

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = Arrays.stream(LearningResourceSearchFilter.values()).filter(
				f -> shouldIncludeSearchFilter(f))
				.toArray(LearningResourceSearchFilter[]::new);
		searchCredentials(false);
	}
	
	public boolean shouldIncludeSearchFilter(LearningResourceSearchFilter f) {
		boolean isInstructor = loggedUserBean.hasCapability("BASIC.INSTRUCTOR.ACCESS");
		boolean canCreateContent = loggedUserBean.hasCapability("MANAGE.CONTENT.EDIT");
		return f != LearningResourceSearchFilter.ENROLLED && 
			   f != LearningResourceSearchFilter.BY_OTHER_STUDENTS &&
			   (f != LearningResourceSearchFilter.YOUR_CREDENTIALS || isInstructor) &&
			   (f != LearningResourceSearchFilter.FROM_CREATOR || canCreateContent);
	}
	
	public void searchCredentials(boolean userSearch) {
		try {
			getCredentialSearchResults();
			
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
		this.paginationData.setPage(1);
		searchCredentials(true);
	}

	public void getCredentialSearchResults() {
		TextSearchResponse1<CredentialData> response = credentialTextSearch.searchCredentials(
				searchTerm, this.paginationData.getPage() - 1, this.paginationData.getLimit(), 
				loggedUserBean.getUserId(), searchFilter, sortOption, false, true);
		credentials = response.getFoundNodes();
		this.paginationData.update((int) response.getHitsNumber());
	}
	
	public void applySearchFilter(LearningResourceSearchFilter filter) {
		this.searchFilter = filter;
		this.paginationData.setPage(1);
		searchCredentials(true);
	}
	
	public void applySortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
		this.paginationData.setPage(1);
		searchCredentials(true);
	}
	
	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCredentials(true);
		}
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

	public LearningResourceSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public void setCredentials(List<CredentialData> credentials) {
		this.credentials = credentials;
	}

	public List<CredentialData> getCredentials() {
		return credentials;
	}

	public LearningResourceSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(LearningResourceSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public LearningResourceSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(LearningResourceSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public LearningResourceSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(LearningResourceSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
}
