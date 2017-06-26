/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialLibraryBean")
@Component("credentialLibraryBean")
@Scope("view")
public class CredentialLibraryBean implements Serializable, Paginable {

	private static final long serialVersionUID = 5019552987111259682L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBean.class);

	@Inject private CredentialTextSearch credentialTextSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private LoggingService loggingService;

	private List<CredentialData> credentials;
	
	//search
	private String searchTerm = "";
	private CredentialSearchFilterUser searchFilter = CredentialSearchFilterUser.ALL;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private CredentialSearchFilterUser[] searchFilters;

	private String context = "name:library";

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = CredentialSearchFilterUser.values();
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
		paginationData.setPage(1);
		searchCredentials(true);
	}

	public void getCredentialSearchResults() {
		PaginatedResult<CredentialData> response = credentialTextSearch.searchCredentialsForUser(
				searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), loggedUserBean.getUserId(), 
				searchFilter, sortOption);
				
		paginationData.update((int) response.getHitsNumber());
		credentials = response.getFoundNodes();
	}
	
	public void applySearchFilter(CredentialSearchFilterUser filter) {
		this.searchFilter = filter;
		paginationData.setPage(1);
		searchCredentials(true);
	}
	
	public void applySortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
		paginationData.setPage(1);
		searchCredentials(true);
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCredentials(true);
		}
	}
	
	/*
	 * ACTIONS
	 */
	public void enrollInCredential(CredentialData cred) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			LearningContextData context = new LearningContextData(page, lContext, service);
			
			credentialManager.enrollInCredential(cred.getId(), loggedUserBean.getUserId(), context);
			
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extContext.redirect(extContext.getRequestContextPath() + 
						"/credentials/" + idEncoder.encodeId(cred.getId()) + "?justEnrolled=true");
			} catch (IOException e) {
				logger.error(e);
			}
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while enrolling in a credential");
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

	@Override
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

	public CredentialSearchFilterUser getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialSearchFilterUser searchFilter) {
		this.searchFilter = searchFilter;
	}

	public CredentialSearchFilterUser[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CredentialSearchFilterUser[] searchFilters) {
		this.searchFilters = searchFilters;
	}
	
}
