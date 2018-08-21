package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.Competence1Manager;
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

@ManagedBean(name = "competencePrivacyBean")
@Component("competencePrivacyBean")
@Scope("view")
public class CompetencePrivacyBean implements Serializable {

	private static final long serialVersionUID = -4614143237281463717L;

	private static Logger logger = Logger.getLogger(CompetencePrivacyBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;

	private String compId;
	private long decodedCompId;
	private String competenceTitle;

	private String credId;
	private String credTitle;

	private List<UnitData> units;

	public void init() {
		decodedCompId = idEncoder.decodeId(compId);
		if (decodedCompId > 0) {
			try {
				ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit);
				ResourceAccessData access = compManager.getResourceAccessData(decodedCompId,
						loggedUserBean.getUserId(), req);

				if (!access.isCanAccess()) {
					PageUtil.accessDenied();
				} else {
					competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					if (competenceTitle != null) {
						long decodedCredId = idEncoder.decodeId(credId);
						if (decodedCredId > 0){
							this.credTitle = credManager.getCredentialTitle(decodedCredId);
						}

						loadData();
					} else {
						PageUtil.notFound();
					}
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void loadData() {
		units = unitManager.getUnitsWithCompetenceSelectionInfo(loggedUserBean.getOrganizationId(),
				decodedCompId);
	}

	public void unitSelectionListener(AjaxBehaviorEvent event) {
		UIComponent comp = event.getComponent();
		UnitData unit = (UnitData) comp.getAttributes().get("unit");
		try {
			if (unit.isSelected()) {
				unitManager.addCompetenceToUnit(decodedCompId, unit.getId(),
						loggedUserBean.getUserContext(
								PageUtil.extractLearningContextDataFromComponent(comp)));
			} else {
				unitManager.removeCompetenceFromUnit(decodedCompId, unit.getId(),
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

	public List<UnitData> getUnits() {
		return units;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredTitle() {
		return credTitle;
	}
}
