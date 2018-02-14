/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				PageContextData lcd = new PageContextData(page, context, null);
				Map<String, String> params = new HashMap<>();
				params.put("query", searchTerm);
				try {
					loggingService.logServiceUse(loggedUserBean.getUserContext(lcd), ComponentName.SEARCH_CREDENTIALS,
							null, params, loggedUserBean.getIpAddress());
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
	
	public void select(CredentialData cred) {
		this.selectedCred = cred;
	}

	public void getCredentialSearchResults() {
		PaginatedResult<CredentialData> response = credentialTextSearch.searchCredentialsForManager(
				loggedUserBean.getOrganizationId(), searchTerm, this.paginationData.getPage() - 1, this.paginationData.getLimit(),
				loggedUserBean.getUserId(), searchFilter, sortOption);
		extractResult(response);
	}

	private void extractResult(PaginatedResult<CredentialData> res) {
		credentials = res.getFoundNodes();
		this.paginationData.update((int) res.getHitsNumber());
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
//			PageContextData context = new PageContextData(page, lContext, service);
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
			boolean archived = false;
			try {
				credManager.archiveCredential(selectedCred.getId(), loggedUserBean.getUserContext());
				archived = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
			}
			if(archived) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage( "The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been archived");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			}
		}
	}
	
	public void restore() {
		if(selectedCred != null) {
			boolean success = false;
			try {
				credManager.restoreArchivedCredential(selectedCred.getId(), loggedUserBean.getUserContext());
				success = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
			}
			if(success) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been restored");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			}
		}
	}
	
	private void reloadDataFromDB() {
		PaginatedResult<CredentialData> res = credManager.searchCredentialsForManager(
				searchFilter, paginationData.getLimit(),paginationData.getPage() - 1, sortOption, loggedUserBean.getUserId());
		extractResult(res);
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
