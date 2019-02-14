package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

/**
 * @author stefanvuckovic
 *
 */

@ManagedBean(name = "competenceInstructorAssessmentsBean")
@Component("competenceInstructorAssessmentsBean")
@Scope("view")
public class StudentCompetenceInstructorAssessmentsBean extends CompetenceInstructorAssessmentsBean {

	private static final long serialVersionUID = -8693906801755334848L;

	private static Logger logger = Logger.getLogger(StudentCompetenceInstructorAssessmentsBean.class);

	@Inject private LoggedUserBean loggedUserBean;

	public void init() {
		decodeCredentialAndCompetenceIds();
		if (getDecodedCompId() > 0) {
			try {
				boolean userEnrolled = getCompManager().isUserEnrolled(getDecodedCompId(), loggedUserBean.getUserId());

				if (!userEnrolled) {
					PageUtil.accessDenied();
				} else {
					loadInitialAssessmentData();
				}
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	@Override
	long getStudentId() {
		return loggedUserBean.getUserId();
	}

	@Override
	AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.FULL;
	}

	public void markActivityAssessmentDiscussionRead() {
		String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			getAssessmentManager().markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					getIdEncoder().decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		for (CompetenceAssessmentData comp : getAssessments()) {
			if (comp.getActivityAssessmentData() != null) {
				for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
					if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
						return Optional.of(act);
					}
				}
			}
		}
		return Optional.empty();
	}

	public void markCompetenceAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = getIdEncoder().decodeId(encodedAssessmentId);
			getAssessmentManager().markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			Optional<CompetenceAssessmentData> compAssessment = getCompetenceAssessmentById(assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<CompetenceAssessmentData> getCompetenceAssessmentById(long assessmentId) {
		for (CompetenceAssessmentData ca : getAssessments()) {
			if (assessmentId == ca.getCompetenceAssessmentId()) {
				return Optional.of(ca);
			}
		}
		return Optional.empty();
	}

	private String getEncodedAssessmentIdFromRequest() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		return params.get("assessmentEncId");
	}
	//MARK DISCUSSION READ END

	/*
	 * GETTERS / SETTERS
	 */
}
