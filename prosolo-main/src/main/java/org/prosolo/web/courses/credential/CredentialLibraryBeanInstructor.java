/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.search.util.credential.CredentialDeliverySortOption;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.web.LoggedUserBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "credentialLibraryBeanInstructor")
@Component("credentialLibraryBeanInstructor")
@Scope("view")
public class CredentialLibraryBeanInstructor implements Serializable {

	private static final long serialVersionUID = -2145386401343084693L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBeanInstructor.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;

	private List<CredentialData> ongoingDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> pastDeliveries;

	private CredentialDeliverySortOption sortOption = CredentialDeliverySortOption.DATE_STARTED;
	private CredentialDeliverySortOption[] sortOptions;

	private String context = "name:library";

	public void init() {
		sortOptions = CredentialDeliverySortOption.values();
		loadCredentials();
	}

	private void initializeDeliveriesCollections() {
		ongoingDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		pastDeliveries = new ArrayList<>();
	}

	public void loadCredentials() {
		try {
			List<CredentialData> deliveries = credManager.getCredentialDeliveriesForUserWithInstructPrivilege(
					loggedUserBean.getUserId(), sortOption);
			initializeDeliveriesCollections();
			CredentialDeliveryUtil.populateCollectionsBasedOnDeliveryStartAndEnd(
					deliveries, ongoingDeliveries, pendingDeliveries, pastDeliveries
			);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	public void applySortOption(CredentialDeliverySortOption sortOption) {
		this.sortOption = sortOption;
		loadCredentials();
	}

	public List<CredentialData> getOngoingDeliveries() {
		return ongoingDeliveries;
	}

	public void setOngoingDeliveries(List<CredentialData> ongoingDeliveries) {
		this.ongoingDeliveries = ongoingDeliveries;
	}

	public List<CredentialData> getPendingDeliveries() {
		return pendingDeliveries;
	}

	public void setPendingDeliveries(List<CredentialData> pendingDeliveries) {
		this.pendingDeliveries = pendingDeliveries;
	}

	public List<CredentialData> getPastDeliveries() {
		return pastDeliveries;
	}

	public void setPastDeliveries(List<CredentialData> pastDeliveries) {
		this.pastDeliveries = pastDeliveries;
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
