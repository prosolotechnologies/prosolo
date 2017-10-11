package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "credentialViewBeanAdmin")
@Component("credentialViewBeanAdmin")
@Scope("view")
public class CredentialViewBeanAdmin implements Serializable {

	private static final long serialVersionUID = -8249064694363873554L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanAdmin.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	private String id;
	private long decodedId;
	
	private CredentialData credentialData;

	private String organizationTitle;
	private String unitTitle;

	public void init() {	
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedId = idEncoder.decodeId(id);

		if (loggedUser.getOrganizationId() == decodedOrgId || loggedUser.hasCapability("admin.advanced")) {
			if (decodedOrgId > 0 && decodedUnitId > 0 && decodedId > 0) {
				try {
					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);
					if (td != null) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();

						credentialData = credentialManager
								.getCredentialData(decodedId, true, true, loggedUser.getUserId(), AccessMode.MANAGER);
						if (!unitManager.isCredentialConnectedToUnit(decodedId, decodedUnitId, credentialData.getType())) {
							//if credential is not connected to the unit this page is for show the not found page
							PageUtil.notFound();
						}
					} else {
						PageUtil.notFound();
					}
				} catch (ResourceNotFoundException rnfe) {
					PageUtil.notFound();
				} catch (Exception e) {
					logger.error("Error", e);
					PageUtil.fireErrorMessage("Error trying to retrieve " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " data");
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.accessDenied();
		}
	}

	public boolean isOriginal() {
		return credentialData != null ? credentialData.getType() == CredentialType.Original : false;
	}
	
	/*
	 * ACTIONS
	 */
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if(!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = activityManager
					.getCompetenceActivitiesData(cd.getCompetenceId());
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}
	
	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public String getUnitTitle() {
		return unitTitle;
	}
}
