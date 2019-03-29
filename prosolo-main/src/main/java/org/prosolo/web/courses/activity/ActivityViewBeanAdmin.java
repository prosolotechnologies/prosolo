package org.prosolo.web.courses.activity;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
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

	@Getter @Setter private String orgId;
	@Getter @Setter private String unitId;
	@Getter @Setter private String actId;
	@Getter @Setter private String compId;
	@Getter @Setter private String credId;

	@Getter private long decodedOrgId;
	private long decodedUnitId;
	private long decodedActId;
	private long decodedCompId;
	private long decodedCredId;

	@Getter private CompetenceData1 competenceData;

	@Getter private String organizationTitle;
	@Getter private String unitTitle;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedActId = idEncoder.decodeId(actId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0 && decodedActId > 0 && decodedCompId > 0 && decodedCredId > 0) {
				try {
					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);

					boolean connectedToUnit = unitManager.isCredentialConnectedToUnit(decodedCredId, decodedUnitId);

					if (td != null && connectedToUnit) {
						// check if credential, competency and activity are mutually connected
						activityManager.checkIfActivityAndCompetenceArePartOfCredential(decodedCredId, decodedCompId, decodedActId);

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

		String credTitle = credManager.getCredentialTitle(decodedCredId);
		competenceData.setCredentialId(decodedCredId);
		competenceData.setCredentialTitle(credTitle);
	}

	public boolean isActivityActive(ActivityData act) {
		return decodedActId == act.getActivityId();
	}
	
	public boolean isCurrentUserCreator() {
		return competenceData.getActivityToShowWithDetails().getCreatorId() == loggedUser.getUserId();
	}
	
}
