package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
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
	
	private String id;
	private long decodedId;
	
	private String credentialTitle;
	private List<CredentialData> activeDeliveries;
	private List<CredentialData> pendingDeliveries;
	private List<CredentialData> completedDeliveries;
	
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
				loadCredentialDeliveries(decodedId);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage("Error while trying to load credential deliveries");
		}
	}
	
	private void initializeValues() {
		activeDeliveries = new ArrayList<>();
		pendingDeliveries = new ArrayList<>();
		completedDeliveries = new ArrayList<>();
	}

	private void loadCredentialDeliveries(long id) {
		RestrictedAccessResult<List<CredentialData>> res = credentialManager
				.getCredentialDeliveriesWithAccessRights(decodedId, loggedUser.getUserId());
		unpackResult(res);
		
		if(!access.isCanAccess()) {
			PageUtil.accessDenied();
		}
	}
	
	private void unpackResult(RestrictedAccessResult<List<CredentialData>> res) {
		access = res.getAccess();
		List<CredentialData> deliveries = res.getResource();
		CredentialDeliveryUtil.populateCollectionsBasedOnDeliveryStartAndEnd(
				deliveries, activeDeliveries, pendingDeliveries, completedDeliveries
		);
	}
	
	/*
	 * ACTIONS
	 */
	
	
	/*
	 * GETTERS / SETTERS
	 */
	
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
	
}
