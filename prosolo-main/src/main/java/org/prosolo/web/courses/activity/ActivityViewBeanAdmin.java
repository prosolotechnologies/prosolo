package org.prosolo.web.courses.activity;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.courses.activity.util.ActivityUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "activityViewBeanAdmin")
@Component("activityViewBeanAdmin")
@Scope("view")
public class ActivityViewBeanAdmin implements Serializable {

	private static final long serialVersionUID = 1362049703707672076L;

	private static Logger logger = Logger.getLogger(ActivityViewBeanAdmin.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager compManager;
	@Inject private UnitManager unitManager;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	private String actId;
	private long decodedActId;
	private String compId;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;

	private CompetenceData1 competenceData;

	private String organizationTitle;
	private String unitTitle;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0 && decodedActId > 0 && decodedCompId > 0) {
				if (credId != null) {
					decodedCredId = idEncoder.decodeId(credId);
				}
				try {
					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);
				/*
				if credential id is passed we check if credential is connected to unit because if admin
				comes to this page from credential page he should always see competency and activity details - not found
				page would be confusing for him.
				 */
					boolean connectedToUnit = decodedCredId > 0
							? unitManager.isCredentialConnectedToUnit(decodedCredId, decodedUnitId)
							: unitManager.isCompetenceConnectedToUnit(decodedCompId, decodedUnitId);
					if (td != null && connectedToUnit) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();

						competenceData = activityManager
								.getCompetenceActivitiesWithSpecifiedActivityInFocus(
										decodedCredId, decodedCompId, decodedActId);

						ActivityUtil.createTempFilesAndSetUrlsForCaptions(
								competenceData.getActivityToShowWithDetails().getCaptions(),
								loggedUser.getUserId());

						loadCompetenceAndCredentialTitle();
					} else {
						PageUtil.notFound();
					}
				} catch (ResourceNotFoundException rnfe) {
					PageUtil.notFound();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					PageUtil.fireErrorMessage("Error loading the page");
				}
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.accessDenied();
		}
	}
	
	private void loadCompetenceAndCredentialTitle() {
		String compTitle = compManager.getCompetenceTitle(decodedCompId);
		competenceData.setTitle(compTitle);
		if (decodedCredId > 0) {
			String credTitle = credManager.getCredentialTitle(decodedCredId);
			competenceData.setCredentialId(decodedCredId);
			competenceData.setCredentialTitle(credTitle);
		}
		
	}

	public boolean isActivityActive(ActivityData act) {
		return decodedActId == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData.getActivityToShowWithDetails().getCreatorId() == loggedUser.getUserId();
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
	}

	public String getActId() {
		return actId;
	}

	public void setActId(String actId) {
		this.actId = actId;
	}

	public long getDecodedActId() {
		return decodedActId;
	}

	public void setDecodedActId(long decodedActId) {
		this.decodedActId = decodedActId;
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

	public void setDecodedCompId(long decodedCompId) {
		this.decodedCompId = decodedCompId;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public void setDecodedCredId(long decodedCredId) {
		this.decodedCredId = decodedCredId;
	}

	public void setCompetenceData(CompetenceData1 competenceData) {
		this.competenceData = competenceData;
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
