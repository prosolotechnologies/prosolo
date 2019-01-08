package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "credentialPrivacyBean")
@Component("credentialPrivacyBean")
@Scope("view")
public class CredentialPrivacyBean implements Serializable {

	private static final long serialVersionUID = 5456907069755840015L;

	private static Logger logger = Logger.getLogger(CredentialPrivacyBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;

	private String credId;
	private long decodedCredId;
	private String credentialTitle;

	private List<UnitData> units;

	public void init() {
		decodedCredId = idEncoder.decodeId(credId);
		if (decodedCredId > 0) {
			try {
				ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit);
				ResourceAccessData access = credManager.getResourceAccessData(decodedCredId,
						loggedUserBean.getUserId(), req);
				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					credentialTitle = credManager.getCredentialTitle(decodedCredId, CredentialType.Original);
					if (credentialTitle != null) {
						loadData();
					} else {
						PageUtil.notFound();
					}
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}
	
	private void loadData() {
		units = unitManager.getUnitsWithCredentialSelectionInfo(loggedUserBean.getOrganizationId(),
				decodedCredId);
	}
	
	public void unitSelectionListener(AjaxBehaviorEvent event) {
		UIComponent comp = event.getComponent();
		UnitData unit = (UnitData) comp.getAttributes().get("unit");
		try {
			if (unit.isSelected()) {
				unitManager.addCredentialToUnit(decodedCredId, unit.getId(),
						loggedUserBean.getUserContext(
								PageUtil.extractLearningContextDataFromComponent(comp)));
			} else {
				unitManager.removeCredentialFromUnit(decodedCredId, unit.getId(),
						loggedUserBean.getUserContext(
								PageUtil.extractLearningContextDataFromComponent(comp)));
			}
			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
		} catch (DbConnectionException e) {
			//change selection status to previous one since saving failed
			unit.changeSelectionStatus();
			logger.error(e);
			PageUtil.fireErrorMessage("An error has occurred");
		}
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public List<UnitData> getUnits() {
		return units;
	}
}
