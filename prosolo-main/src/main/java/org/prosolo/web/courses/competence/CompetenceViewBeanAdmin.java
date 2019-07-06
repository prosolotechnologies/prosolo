package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "competenceViewBeanAdmin")
@Component("competenceViewBeanAdmin")
@Scope("view")
public class CompetenceViewBeanAdmin implements Serializable {

	private static final long serialVersionUID = -2637745292429162558L;

	private static Logger logger = Logger.getLogger(CompetenceViewBeanAdmin.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credManager;
	@Inject private Competence1Manager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;
	@Inject private PageAccessRightsResolver pageAccessRightsResolver;

	private String orgId;
	private long decodedOrgId;
	private String unitId;
	private long decodedUnitId;
	private String credId;
	private long decodedCredId;
	private String compId;
	private long decodedCompId;

	private String organizationTitle;
	private String unitTitle;

	private CompetenceData1 competenceData;

	private String credentialTitle;

	public void init() {
		decodedOrgId = idEncoder.decodeId(orgId);
		decodedUnitId = idEncoder.decodeId(unitId);
		decodedCompId = idEncoder.decodeId(compId);
		decodedCredId = idEncoder.decodeId(credId);

		if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
			if (decodedOrgId > 0 && decodedUnitId > 0 && decodedCompId > 0 && decodedCredId > 0) {
				try {
					// check if credential and competency are connected
					competenceManager.checkIfCompetenceIsPartOfACredential(decodedCredId, decodedCompId);

					TitleData td = unitManager.getOrganizationAndUnitTitle(decodedOrgId, decodedUnitId);

					boolean connectedToUnit = unitManager.isCredentialConnectedToUnit(decodedCredId, decodedUnitId);

					if (td != null && connectedToUnit) {
						organizationTitle = td.getOrganizationTitle();
						unitTitle = td.getUnitTitle();

						competenceData = competenceManager.getCompetenceData(decodedCompId, true, false, true, true,
								false);

						if (decodedCredId > 0) {
							credentialTitle = credManager.getCredentialTitle(decodedCredId);
							competenceData.setCredentialId(decodedCredId);
						}
					} else {
						PageUtil.notFound();
					}
				} catch (ResourceNotFoundException rnfe) {
					PageUtil.notFound();
				} catch (Exception e) {
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
	
	public boolean isCurrentUserCreator() {
		return competenceData == null || competenceData.getCreator() == null ? false : 
			competenceData.getCreator().getId() == loggedUser.getUserId();
	}
	
	public boolean hasMoreActivities(int index) {
		return competenceData.getActivities().size() != index + 1;
	}
	
	public String getLabelForCompetence() {
		return competenceData.isPublished() ? "" : "(Unpublished)";
 	}

	/*
	 * ACTIONS
	 */
	
	
	/*
	 * GETTERS / SETTERS
	 */

	public CompetenceData1 getCompetenceData() {
		return competenceData;
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

	public String getCredentialTitle() {
		return credentialTitle;
	}
}
