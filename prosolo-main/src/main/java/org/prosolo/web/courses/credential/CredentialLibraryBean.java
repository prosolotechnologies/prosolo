/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Inject private UnitManager unitManager;
	@Inject private RoleManager roleManager;

	private List<CredentialData> credentials;
	
	//search
	private String searchTerm = "";
	private CredentialSearchFilterUser searchFilter = CredentialSearchFilterUser.ALL;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private CredentialSearchFilterUser[] searchFilters;

	private String context = "name:library";

	//list of unit ids where user has student role
	private List<Long> unitIds = new ArrayList<>();

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = CredentialSearchFilterUser.values();

		try {
			List<Long> roleIds = roleManager.getRoleIdsForName(RoleNames.USER);
			long roleId = 0;
			if (roleIds.size() == 1) {
				roleId = roleIds.get(0);
			}
			unitIds = unitManager.getUserUnitIdsInRole(loggedUserBean.getUserId(), roleId);

			searchCredentials(false);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error while loading the page");
		}
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
				loggedUserBean.getOrganizationId(), searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), loggedUserBean.getUserId(),
				unitIds, searchFilter, sortOption);
				
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
			credentialManager.enrollInCredential(cred.getId(), loggedUserBean.getUserContext());

			PageUtil.redirect("/credentials/" + idEncoder.encodeId(cred.getId()) + "?justEnrolled=true");
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
