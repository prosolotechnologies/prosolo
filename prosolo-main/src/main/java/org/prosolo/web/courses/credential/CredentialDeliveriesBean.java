package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.util.credential.CredentialDeliverySortOption;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.services.assessment.data.AssessmentSortOrder;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.common.data.SortingOption;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ManagedBean(name = "credentialDeliveriesBean")
@Component("credentialDeliveriesBean")
@Scope("view")
public class CredentialDeliveriesBean extends DeliveriesBean implements Serializable {

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
	private List<CredentialData> ongoingDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> completedDeliveries;
	private List<CredentialData> deliveries;
	private CredentialSearchFilterManager searchFilter = CredentialSearchFilterManager.ACTIVE;
	private CredentialSearchFilterManager[] searchFilters;
	private CredentialData selectedDelivery;

	private CredentialDeliverySortOption sortOption = CredentialDeliverySortOption.DELIVERY_ORDER;
	private CredentialDeliverySortOption[] sortOptions;
	
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
		sortOptions = CredentialDeliverySortOption.values();
		searchFilters = CredentialSearchFilterManager.values();
	}

	private void initializeDeliveryCollections(){
		ongoingDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		completedDeliveries = new ArrayList<>();
	}

	private void loadCredentialDeliveries(CredentialSearchFilterManager filter) {
		SortOrder.Builder<CredentialDeliverySortOption> sortBuilder =
				SortOrder.<CredentialDeliverySortOption>builder()
						.addOrder(sortOption, sortOption.getSortOrder());
		if (sortOption != CredentialDeliverySortOption.DELIVERY_ORDER) {
			//add delivery order as a second criteria if not already selected as first
			CredentialDeliverySortOption deliveryOrderSortOption = CredentialDeliverySortOption.DELIVERY_ORDER;
			sortBuilder.addOrder(deliveryOrderSortOption, deliveryOrderSortOption.getSortOrder());
		}
		RestrictedAccessResult<List<CredentialData>> res = credentialManager
				.getCredentialDeliveriesWithAccessRights(decodedId, loggedUser.getUserId(), sortBuilder.build(), filter);
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
				deliveries, ongoingDeliveries, pendingDeliveries, completedDeliveries
		);
	}

	@Override
	public boolean canUserNavigateToWhoCanLearnPage() {
		return true;
	}

	public void applySortOption(CredentialDeliverySortOption sortOption) {
		this.sortOption = sortOption;
		loadCredentialDeliveries(searchFilter);
	}

	public void archive() {
		if(selectedDelivery != null) {
			try {
				credentialManager.archiveCredential(selectedDelivery.getIdData().getId(), loggedUser.getUserContext());
				loadCredentialDeliveries(CredentialSearchFilterManager.ACTIVE);
				PageUtil.fireSuccessfulInfoMessage( "The " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been archived");
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error archiving the " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase());
			}
		}
	}

	public void applySearchFilter(CredentialSearchFilterManager filter) {
		this.searchFilter = filter;
		loadCredentialDeliveries(searchFilter);
	}

	public void restore() {
		if(selectedDelivery != null) {
			try {
				credentialManager.restoreArchivedCredential(selectedDelivery.getIdData().getId(), loggedUser.getUserContext());
				loadCredentialDeliveries(CredentialSearchFilterManager.ARCHIVED);
				PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + " has been restored");
			} catch (DbConnectionException e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error restoring the " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase());
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

	public List<CredentialData> getOngoingDeliveries() {
			return ongoingDeliveries;
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

	public CredentialDeliverySortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CredentialDeliverySortOption sortOption) {
		this.sortOption = sortOption;
	}

	public CredentialDeliverySortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(CredentialDeliverySortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

}
