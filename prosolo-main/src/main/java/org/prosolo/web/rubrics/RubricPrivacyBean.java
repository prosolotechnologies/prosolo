package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RubricManager;
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

@ManagedBean(name = "rubricPrivacyBean")
@Component("rubricPrivacyBean")
@Scope("view")
public class RubricPrivacyBean implements Serializable {

	private static final long serialVersionUID = 8190599654525010498L;

	private static Logger logger = Logger.getLogger(RubricPrivacyBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;

	private String rubricId;
	private long decodedRubricId;
	private String rubricName;

	private List<UnitData> units;

	public void init() {
		decodedRubricId = idEncoder.decodeId(rubricId);
		if (decodedRubricId > 0) {
			try {
				rubricName = rubricManager.getRubricName(decodedRubricId);
				if (rubricName != null) {
					loadData();
				} else {
					PageUtil.notFound();
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
		units = unitManager.getUnitsWithRubricSelectionInfo(loggedUserBean.getOrganizationId(),
				decodedRubricId);
	}

	public void unitSelectionListener(AjaxBehaviorEvent event) {
		UIComponent comp = event.getComponent();
		UnitData unit = (UnitData) comp.getAttributes().get("unit");
		try {
			if (unit.isSelected()) {
				unitManager.addRubricToUnit(decodedRubricId, unit.getId(),
						loggedUserBean.getUserContext(
								PageUtil.extractLearningContextDataFromComponent(comp)));
			} else {
				unitManager.removeRubricFromUnit(decodedRubricId, unit.getId(),
						loggedUserBean.getUserContext(
								PageUtil.extractLearningContextDataFromComponent(comp)));
			}
			PageUtil.fireSuccessfulInfoMessage("Changes have been saved");
		} catch (DbConnectionException e) {
			//change selection status to previous one since saving failed
			unit.changeSelectionStatus();
			logger.error(e);
			PageUtil.fireErrorMessage("An error has occurred");
		} catch (EventException ee) {
			logger.error(ee);
		}
	}

	public String getRubricId() {
		return rubricId;
	}

	public void setRubricId(String rubricId) {
		this.rubricId = rubricId;
	}

	public String getRubricName() {
		return rubricName;
	}

	public List<UnitData> getUnits() {
		return units;
	}
}
