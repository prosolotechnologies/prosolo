/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
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
	@Inject private LoggingService loggingService;
	@Inject private CredentialManager credManager;

	private List<CredentialData> credentials;
	private CredentialData selectedCred;
	
	//search
	private String searchTerm = "";
	private CredentialSearchFilterManager searchFilter = CredentialSearchFilterManager.ACTIVE;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private CredentialSearchFilterManager[] searchFilters;

	private String context = "name:library";

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = CredentialSearchFilterManager.values();
		searchCredentials(false);
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
	
	public void selectCredential(CredentialData cred) {
		this.selectedCred = cred;
	}

	public void getCredentialSearchResults() {
		PaginatedResult<CredentialData> response = credentialTextSearch.searchCredentialsForManager(
				searchTerm, this.paginationData.getPage() - 1, this.paginationData.getLimit(),
				loggedUserBean.getUserId(), searchFilter, sortOption);
		credentials = response.getFoundNodes();
		this.paginationData.update((int) response.getHitsNumber());
	}
	
	public void applySearchFilter(CredentialSearchFilterManager filter) {
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
	
//	public void bookmarkCredential(CredentialData cred) {
//		try {
//			String page = PageUtil.getPostParameter("page");
//			String lContext = PageUtil.getPostParameter("learningContext");
//			String service = PageUtil.getPostParameter("service");
//			LearningContextData context = new LearningContextData(page, lContext, service);
//			if(cred.isBookmarkedByCurrentUser()) {
//				credentialManager.deleteCredentialBookmark(cred.getId(), 
//						loggedUserBean.getUserId(), context);
//			} else {
//				credentialManager.bookmarkCredential(cred.getId(), loggedUserBean.getUserId(),
//						context);
//			}
//			cred.setBookmarkedByCurrentUser(!cred.isBookmarkedByCurrentUser());
//		} catch(DbConnectionException e) {
//			PageUtil.fireErrorMessage(e.getMessage());
//		}
//	}
	
	public void archive() {
		if(selectedCred != null) {
			LearningContextData ctx = PageUtil.extractLearningContextData();
			boolean archived = false;
			try {
				credManager.archiveCredential(selectedCred.getId(), loggedUserBean.getUserId(), ctx);
				archived = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while trying to archive credential");
			}
			if(archived) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("Credential archived successfully");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error while refreshing data");
				}
			}
		}
	}
	
	public void restore() {
		if(selectedCred != null) {
			LearningContextData ctx = PageUtil.extractLearningContextData();
			boolean success = false;
			try {
				credManager.restoreArchivedCredential(selectedCred.getId(), loggedUserBean.getUserId(), ctx);
				success = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while trying to restore credential");
			}
			if(success) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("Credential restored successfully");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error while refreshing data");
				}
			}
		}
	}
	
	private void reloadDataFromDB() {
		paginationData.update((int) credManager.countNumberOfCredentials(searchFilter, 
				loggedUserBean.getUserId(), UserGroupPrivilege.Edit));
		credentials = credManager.searchCredentialsForManager(searchFilter, paginationData.getLimit(), 
				paginationData.getPage() - 1, sortOption, loggedUserBean.getUserId());
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

	public CredentialSearchFilterManager getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialSearchFilterManager searchFilter) {
		this.searchFilter = searchFilter;
	}

	public CredentialSearchFilterManager[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CredentialSearchFilterManager[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public CredentialData getSelectedCred() {
		return selectedCred;
	}
	
}
