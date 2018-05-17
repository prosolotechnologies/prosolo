package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "studentCredentialPeerAssessmentsBean")
@Component("studentCredentialPeerAssessmentsBean")
@Scope("view")
public class StudentCredentialPeerAssessmentsBean extends CredentialPeerAssessmentsBean {

	private static final long serialVersionUID = 7123718009079866695L;

	private static Logger logger = Logger.getLogger(StudentCredentialPeerAssessmentsBean.class);

	@Inject private AskForCredentialAssessmentBean askForAssessmentBean;
	@Inject private LoggedUserBean loggedUserBean;

	//needed when new assessment request is sent
	private long targetCredId;

	public void init() {
		try {
			decodeCredentialId();

			if (getDecodedId() > 0) {
				targetCredId = getCredentialManager().getTargetCredentialId(getDecodedId(), loggedUserBean.getUserId());

				//if user is not enrolled he is not allowed to access this page
				if (targetCredId <= 0) {
					PageUtil.accessDenied();
				} else {
					loadInitialAssessmentData();
				}
			} else {
				PageUtil.notFound();
			}
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	public void initAskForAssessment() {
		askForAssessmentBean.init(getDecodedId(), targetCredId, AssessmentType.PEER_ASSESSMENT);
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

	@Override
	protected AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.FULL;
	}

	@Override
	protected long getStudentId() {
		return loggedUserBean.getUserId();
	}
}
