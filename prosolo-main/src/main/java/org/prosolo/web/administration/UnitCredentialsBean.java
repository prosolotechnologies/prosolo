/**
 * 
 */
package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "unitCredentialsBean")
@Component("unitCredentialsBean")
@Scope("view")
public class UnitCredentialsBean implements Serializable, Paginable {

	private static final long serialVersionUID = 8916968239000368400L;

	private static Logger logger = Logger.getLogger(UnitCredentialsBean.class);

	@Inject private CredentialTextSearch credentialTextSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;
	@Inject private UnitManager unitManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;

	private String unitId;
	private long decodedUnitId;
	private String orgId;
	private long decodedOrgId;
	private int page;

	private String unitTitle;
	private String organizationTitle;

	private List<CredentialData> credentials;
	private CredentialData selectedCred;

	//search
	private String searchTerm = "";
	private CredentialSearchFilterManager searchFilter = CredentialSearchFilterManager.ACTIVE;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData;

	private LearningResourceSortOption[] sortOptions;
	private CredentialSearchFilterManager[] searchFilters;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0) {
				if (page > 0) {
					paginationData = PaginationData.forPage(page);
				} else {
					paginationData = new PaginationData();
				}

				sortOptions = LearningResourceSortOption.values();
				searchFilters = CredentialSearchFilterManager.values();
				try {
					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);
					if (td != null) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();

						loadDataFromDB();
					} else {
						PageUtil.notFound();
					}
				} catch (Exception e) {
					logger.error("Error", e);
					PageUtil.fireErrorMessage("Error loading the page");
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.accessDenied();
		}
	}

	public void resetAndSearch() {
		this.paginationData.setPage(1);
		searchCredentials();
	}

	public void select(CredentialData cred) {
		this.selectedCred = cred;
	}

	private void searchCredentials() {
		PaginatedResult<CredentialData> response = credentialTextSearch.searchCredentialsForAdmin(
				decodedOrgId, decodedUnitId, searchTerm, paginationData.getPage() - 1, paginationData.getLimit(),
				searchFilter, sortOption);
		extractResult(response);
	}

	private void extractResult(PaginatedResult<CredentialData> res) {
		credentials = res.getFoundNodes();
		this.paginationData.update((int) res.getHitsNumber());
	}

	public void applySearchFilter(CredentialSearchFilterManager filter) {
		this.searchFilter = filter;
		this.paginationData.setPage(1);
		searchCredentials();
	}

	public void applySortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
		this.paginationData.setPage(1);
		searchCredentials();
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCredentials();
		}
	}

	public boolean hasDeliveryStarted() {
		return selectedCred.getDeliveryStartTime() >= 0 &&
				getNumberOfMillisecondsBetweenNowAndDeliveryStart() <= 0;
	}

	public boolean hasDeliveryEnded() {
		return selectedCred.getDeliveryEndTime() >= 0 &&
				getNumberOfMillisecondsBetweenNowAndDeliveryEnd() <= 0;
	}

	public long getNumberOfMillisecondsBetweenNowAndDeliveryStart() {
		return selectedCred.getDeliveryStartTime() >= 0
				? getDateDiffInMilliseconds(selectedCred.getDeliveryStartTime(), new Date().getTime())
				: 0;
	}

	public long getNumberOfMillisecondsBetweenNowAndDeliveryEnd() {
		return selectedCred.getDeliveryEndTime() >= 0
				? getDateDiffInMilliseconds(selectedCred.getDeliveryEndTime(), new Date().getTime())
				: 0;
	}

	private long getDateDiffInMilliseconds(long millis1, long millis2) {
		/*
		 * if difference is bigger than one day return one day in millis to avoid bigger number than
		 * js timeout allows
		 */
		long oneDayMillis = 24 * 60 * 60 * 1000;
		long diff = millis1 - millis2;
		return diff < oneDayMillis ? diff : oneDayMillis;
	}

	/*
	 * ACTIONS
	 */

	public void archive() {
		if (selectedCred != null) {
			try {
				credManager.archiveCredential(selectedCred.getId(), loggedUserBean.getUserContext(decodedOrgId));
				searchTerm = null;
				paginationData.setPage(1);

				try {
					loadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage( "The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been archived");
				} catch (DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
			} catch (EventException e) {
				logger.error("Error", e);
			}
		}
	}

	public void restore() {
		if (selectedCred != null) {
			try {
				credManager.restoreArchivedCredential(selectedCred.getId(), loggedUserBean.getUserContext(decodedOrgId));
				searchTerm = null;
				paginationData.setPage(1);

				try {
					loadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " has been restored");
				} catch (DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
			} catch (EventException e) {
				logger.error("Error", e);
			}
		}
	}

	public void updateDelivery() {
		if (selectedCred != null) {
			try {
				credManager.updateDeliveryStartAndEnd(selectedCred, loggedUserBean.getUserContext(decodedOrgId));
				PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been updated");
			} catch (EventException e) {
				logger.error("Error", e);
			} catch (Exception e) {
				logger.error("Error", e);
				//restore dates
				selectedCred.setDeliveryStartTime(selectedCred.getDeliveryStartBeforeUpdate());
				selectedCred.setDeliveryEndTime(selectedCred.getDeliveryEndBeforeUpdate());
				PageUtil.fireErrorMessage("Error updating the " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase());
			}
		}
	}

	private void loadDataFromDB() {
		PaginatedResult<CredentialData> res = credManager.searchCredentialsForAdmin(decodedUnitId, searchFilter, paginationData.getLimit(),
				paginationData.getPage() - 1, sortOption);
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

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public String getUnitTitle() {
		return unitTitle;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}
}
