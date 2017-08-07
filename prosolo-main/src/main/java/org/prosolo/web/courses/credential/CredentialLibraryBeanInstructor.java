/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
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

	private List<CredentialData> activeDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> pastDeliveries;

	private String context = "name:library";

	public void init() {
		initializeValues();
		loadCredentials();
	}

	private void initializeValues() {
		activeDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		pastDeliveries = new ArrayList<>();
	}

	public void loadCredentials() {
		try {
			List<CredentialData> deliveries = credManager.getCredentialDeliveriesForUserWithInstructPrivilege(
					loggedUserBean.getUserId());
			CredentialDeliveryUtil.populateCollectionsBasedOnDeliveryStartAndEnd(
					deliveries, activeDeliveries, pendingDeliveries, pastDeliveries
			);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public List<CredentialData> getActiveDeliveries() {
		return activeDeliveries;
	}

	public void setActiveDeliveries(List<CredentialData> activeDeliveries) {
		this.activeDeliveries = activeDeliveries;
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

}
