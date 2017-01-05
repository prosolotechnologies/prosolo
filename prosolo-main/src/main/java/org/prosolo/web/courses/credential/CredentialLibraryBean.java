/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialSearchFilter;
import org.prosolo.search.util.credential.CredentialSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialLibraryBean")
@Component("credentialLibraryBean")
@Scope("view")
public class CredentialLibraryBean implements Serializable, Paginable {

	private static final long serialVersionUID = 5019552987111259682L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBean.class);

	@Inject private TextSearch textSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private LoggingService loggingService;

	private List<CredentialData> credentials;
	
	//search
	private String searchTerm = "";
	private CredentialSearchFilter searchFilter = CredentialSearchFilter.ALL;
	private CredentialSortOption sortOption = CredentialSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private CredentialSortOption[] sortOptions;
	private CredentialSearchFilter[] searchFilters;

	private String context = "name:library";

	public void init() {
		sortOptions = CredentialSortOption.values();
		searchFilters = Arrays.stream(CredentialSearchFilter.values()).filter(
				f -> f != CredentialSearchFilter.BY_STUDENTS &&
					 f != CredentialSearchFilter.YOUR_CREDENTIALS)
				.toArray(CredentialSearchFilter[]::new);
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
		TextSearchResponse1<CredentialData> response = textSearch.searchCredentials(searchTerm, paginationData.getPage() - 1, 
				paginationData.getLimit(), loggedUserBean.getUserId(), searchFilter, sortOption);
		paginationData.update((int) response.getHitsNumber());
		credentials = response.getFoundNodes();
	}
	
	public void applySearchFilter(CredentialSearchFilter filter) {
		this.searchFilter = filter;
		paginationData.setPage(1);
		searchCredentials(true);
	}
	
	public void applySortOption(CredentialSortOption sortOption) {
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
			if(!cred.isFirstTimeDraft()) {
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

	public CredentialSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CredentialSortOption sortOption) {
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
