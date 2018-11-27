package org.prosolo.web.courses.credential;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentRequestData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.competence.TargetCompetenceData;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.AskForCredentialAssessmentBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "userCredentialReadOnlyBean")
@Component("userCredentialReadOnlyBean")
@Scope("view")
public class UserCredentialReadOnlyBean implements Serializable {

	private static final long serialVersionUID = -5011729831412985935L;

	private static Logger logger = Logger.getLogger(UserCredentialReadOnlyBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private Activity1Manager activityManager;

	private String credId;
	private long decodedCredId;
	private String studentId;
	private long decodedStudentId;

	private CredentialData credentialData;

	public void init() {
		decodedCredId = idEncoder.decodeId(credId);
		decodedStudentId = idEncoder.decodeId(studentId);
		if (decodedCredId > 0 && decodedStudentId > 0) {
			try {
				retrieveUserCredentialData();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void retrieveUserCredentialData() {
		credentialData = credentialManager
				.getTargetCredentialDataWithEvidencesAndAssessmentCount(decodedCredId, decodedStudentId);
	}

	private void retrieveUserCredentialDataWithExceptionHandling() {
		try {
			retrieveUserCredentialData();
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if (!cd.isActivitiesInitialized()) {
			List<ActivityData> activities;

			if (cd.isEnrolled()) {
				activities = activityManager.getTargetActivitiesData(cd.getTargetCompId());
			} else {
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			}

			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}

	public boolean isCurrentUserCredentialStudent() {
		return credentialData != null && credentialData.getStudent().getId() == loggedUser.getUserId();
	}

	/*
	ACTIONS
	 */

	/*
	 * GETTERS / SETTERS
	 */

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public long getDecodedCredId() {
		return decodedCredId;
	}

	public long getDecodedStudentId() {
		return decodedStudentId;
	}
}
