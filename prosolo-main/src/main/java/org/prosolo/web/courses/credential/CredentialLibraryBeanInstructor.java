/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
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
public class CredentialLibraryBeanInstructor extends DeliveriesBean implements Serializable {

	private static final long serialVersionUID = -2145386401343084693L;

	private static Logger logger = Logger.getLogger(CredentialLibraryBeanInstructor.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;

	private List<CredentialData> ongoingDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> pastDeliveries;

	private String context = "name:library";

	public void init() {
		initializeValues();
		loadCredentials();
	}

	private void initializeValues() {
		ongoingDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		pastDeliveries = new ArrayList<>();
	}

	public void loadCredentials() {
		try {
			List<CredentialData> deliveries = credManager.getCredentialDeliveriesForUserWithInstructPrivilege(
					loggedUserBean.getUserId());
			CredentialDeliveryUtil.populateCollectionsBasedOnDeliveryStartAndEnd(
					deliveries, ongoingDeliveries, pendingDeliveries, pastDeliveries
			);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
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

}
