package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.assessments.util.AssessmentUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@ManagedBean(name = "studentCompetencePeerAssessmentsBean")
@Component("studentCompetencePeerAssessmentsBean")
@Scope("view")
public class StudentCompetencePeerAssessmentsBean extends CompetencePeerAssessmentsBean {

	private static final long serialVersionUID = 9022737920789959225L;

	private static Logger logger = Logger.getLogger(StudentCompetencePeerAssessmentsBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AskForCompetenceAssessmentBean askForAssessmentBean;

	//needed when new assessment request is sent
	private long targetCompId;

	@Override
	void loadAdditionalData() {
		loadTargetCompetenceIdIfNotLoaded();
	}

	@Override
	boolean isUserAllowedToAccess() {
		loadTargetCompetenceIdIfNotLoaded();
		return targetCompId > 0;
	}

	private void loadTargetCompetenceIdIfNotLoaded() {
		if (targetCompId == 0) {
			targetCompId = getCompManager().getTargetCompetenceId(getDecodedCompId(), loggedUserBean.getUserId());
		}
	}

	@Override
	AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.FULL;
	}

	@Override
	long getStudentId() {
		return loggedUserBean.getUserId();
	}

	public void initAskForAssessment() {
		/*
		in this context new assessment request is always initiated so blind assessment mode is
		loaded from competence.
		 */
		askForAssessmentBean.init(getDecodedCredId(), getDecodedCompId(), targetCompId, AssessmentType.PEER_ASSESSMENT, getBlindAssessmentMode());
	}

	public void submitAssessment() {
		try {
			boolean success = askForAssessmentBean.submitAssessmentRequestAndReturnStatus();
			if (success) {
				getPaginationData().setPage(1);
				getAssessmentsWithExceptionHandling();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error sending the assessment request");
		}
	}

}
