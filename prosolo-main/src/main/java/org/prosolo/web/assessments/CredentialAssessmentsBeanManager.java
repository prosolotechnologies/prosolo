package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.assessment.data.CredentialAssessmentsSummaryData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "credentialAssessmentsBeanManager")
@Component("credentialAssessmentsBeanManager")
@Scope("view")
public class CredentialAssessmentsBeanManager implements Serializable {

	private static final long serialVersionUID = 3564205554631346991L;

	private static Logger logger = Logger.getLogger(CredentialAssessmentsBeanManager.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;

	// PARAMETERS
	private String id;
	private long decodedId;

	private ResourceAccessData access;

	private CredentialAssessmentsSummaryData credentialAssessmentsSummary;

	public void init() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
					ResourceAccessRequirements.of(AccessMode.MANAGER)
							.addPrivilege(UserGroupPrivilege.Instruct)
							.addPrivilege(UserGroupPrivilege.Edit));

			if (!access.isCanAccess()) {
				PageUtil.accessDenied();
			} else {
				try {
					credentialAssessmentsSummary = assessmentManager.getAssessmentsSummaryData(decodedId, access, loggedUserBean.getUserId());

					if (credentialAssessmentsSummary == null) {
						PageUtil.notFound();
					}
				} catch (Exception e) {
					logger.error("Error loading the page", e);
					PageUtil.fireErrorMessage("Error loading the page");
				}
			}
		}
	}

	public boolean canUserEditDelivery() {
		return access.isCanEdit();
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

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public CredentialAssessmentsSummaryData getCredentialAssessmentsSummary() {
		return credentialAssessmentsSummary;
	}
}
