package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("credentialDeliveriesBean")
@Scope("view")
public class CredentialDeliveriesBean implements Serializable {

	private static final long serialVersionUID = 2020680872327236846L;

	private static Logger logger = Logger.getLogger(CredentialDeliveriesBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CredentialTextSearch credentialTextSearch;
	@Inject private LoggingService loggingService;
	
	private String id;
	private long decodedId;
	
	private String credentialTitle;
	private List<CredentialData> activeDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> completedDeliveries;
	private List<CredentialData> deliveries;
	private CredentialSearchFilterManager searchFilter = CredentialSearchFilterManager.ACTIVE;
	private CredentialSearchFilterManager[] searchFilters;
	private CredentialData selectedDelivery;
	
	private ResourceAccessData access;
	
	public void init() {
		initializeValues();
		try {
			decodedId = idEncoder.decodeId(id);
			logger.info("Credential deliveries for credential: " + decodedId);
			//get title only if credentials is original
			credentialTitle = credentialManager.getCredentialTitle(decodedId, CredentialType.Original);
			if (credentialTitle == null) {
				PageUtil.notFound();
			} else {
				loadCredentialDeliveries(CredentialSearchFilterManager.ACTIVE);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to load credential deliveries");
		}
	}
	
	private void initializeValues() {
		initializeDeliveryCollections();
		searchFilters = CredentialSearchFilterManager.values();
	}

	private void initializeDeliveryCollections(){
		activeDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		completedDeliveries = new ArrayList<>();
	}

	private void loadCredentialDeliveries(CredentialSearchFilterManager filter) {
		RestrictedAccessResult<List<CredentialData>> res = credentialManager
				.getCredentialDeliveriesWithAccessRights(decodedId, loggedUser.getUserId(),filter);
		unpackResult(res);

		if(!access.isCanAccess()) {
			PageUtil.accessDenied();
		}
	}

	private void unpackResult(RestrictedAccessResult<List<CredentialData>> res) {
		access = res.getAccess();
		this.deliveries = res.getResource();
		initializeDeliveryCollections();
		CredentialDeliveryUtil.populateCollectionsBasedOnDeliveryStartAndEnd(
				deliveries, activeDeliveries, pendingDeliveries, completedDeliveries
		);
	}

	public void archive() {
		if(selectedDelivery != null) {
			boolean archived = false;
			try {
				credentialManager.archiveCredential(selectedDelivery.getId(), loggedUser.getUserContext());
				archived = true;
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase());
			}
			if(archived) {
				try {
					loadCredentialDeliveries(CredentialSearchFilterManager.ACTIVE);
					PageUtil.fireSuccessfulInfoMessage( "The " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been archived");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			}
		}
	}

	public void applySearchFilter(CredentialSearchFilterManager filter) {
		this.searchFilter = filter;
		loadCredentialDeliveries(searchFilter);
	}

	public void restore() {
		if(selectedDelivery != null) {
			boolean success = false;
			try {
				credentialManager.restoreArchivedCredential(selectedDelivery.getId(), loggedUser.getUserContext());
				success = true;
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase());
			}
			if(success) {
				try {
					loadCredentialDeliveries(CredentialSearchFilterManager.ARCHIVED);
					PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been restored");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error refreshing the data");
				}
			}
		}
	}

	/*
	 * ACTIONS
	 */
	
	
	/*
	 * GETTERS / SETTERS
	 */

	public void select(CredentialData delivery) { this.selectedDelivery = delivery; }

	public long getCredentialId() {
		return decodedId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<CredentialData> getActiveDeliveries() {
			return activeDeliveries;
	}

	public List<CredentialData> getPendingDeliveries() {
		return pendingDeliveries;
	}

	public List<CredentialData> getCompletedDeliveries() {
		return completedDeliveries;
	}

	public CredentialSearchFilterManager getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialSearchFilterManager searchFilter) {
		this.searchFilter = searchFilter;
	}

	public CredentialData getSelectedDelivery() {
		return selectedDelivery;
	}

	public void setSelectedDelivery(CredentialData selectedDelivery) {
		this.selectedDelivery = selectedDelivery;
	}

	public CredentialSearchFilterManager[] getSearchFilters() {
		return searchFilters;
	}

	public List<CredentialData> getDeliveries() {
		return deliveries;
	}

	public void setDeliveries(List<CredentialData> deliveries) {
		this.deliveries = deliveries;
	}
}
