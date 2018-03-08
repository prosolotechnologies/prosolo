package org.prosolo.web.assessments;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author stefanvuckovic
 *
 */

@ManagedBean(name = "competenceInstructorAssessmentsBean")
@Component("competenceInstructorAssessmentsBean")
@Scope("view")
public class CompetenceInstructorAssessmentsBean implements Serializable {

	private static final long serialVersionUID = -8693906801755334848L;

	private static Logger logger = Logger.getLogger(CompetenceInstructorAssessmentsBean.class);

	@Inject private AssessmentManager assessmentManager;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ActivityAssessmentBean activityAssessmentBean;
	@Inject private Competence1Manager compManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;

	private String competenceId;
	private long decodedCompId;
	private String credentialId;
	private long decodedCredId;

	private List<CompetenceAssessmentData> assessments;
	private String competenceTitle;
	private String credentialTitle;

	public void init() {
		decodedCompId = idEncoder.decodeId(competenceId);
		decodedCredId = idEncoder.decodeId(credentialId);

		if (decodedCompId > 0) {
			boolean userEnrolled = compManager.isUserEnrolled(decodedCompId, loggedUserBean.getUserId());

			if (!userEnrolled) {
				PageUtil.accessDenied();
			} else {
				try {
					assessments = assessmentManager.getInstructorCompetenceAssessmentsForStudent(
							decodedCompId, loggedUserBean.getUserId(), new SimpleDateFormat("MMMM dd, yyyy"));
					competenceTitle = compManager.getCompetenceTitle(decodedCompId);
					if (decodedCredId > 0) {
						credentialTitle = credManager.getCredentialTitle(decodedCredId);
					}
				} catch (Exception e) {
					logger.error("Error loading assessment data", e);
					PageUtil.fireErrorMessage("Error loading assessment data");
				}
			}
		}
	}

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY)

//	public long getCurrentAssessmentCompetenceId() {
//		if (currentResType == null) {
//			return 0;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getCompetenceId();
//			case COMPETENCE:
//				return competenceAssessmentBean.getCompetenceAssessmentData().getCompetenceId();
//		}
//		return 0;
//	}
//
//	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
//			case COMPETENCE:
//				return competenceAssessmentBean.getCompetenceAssessmentData().getMessages();
//		}
//		return null;
//	}
//
//	public LearningResourceAssessmentBean getCurrentAssessmentBean() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean;
//			case COMPETENCE:
//				return competenceAssessmentBean;
//		}
//		return null;
//	}
//
//	public long getCurrentAssessmentId() {
//		if (currentResType == null) {
//			return 0;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
//			case COMPETENCE:
//				return competenceAssessmentBean.getCompetenceAssessmentData().getCompetenceAssessmentId();
//		}
//		return 0;
//	}
//
//	public boolean hasStudentCompletedCurrentResource() {
//		if (currentResType == null) {
//			return false;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().isCompleted();
//			case COMPETENCE:
//				//for now
//				return true;
//		}
//		return false;
//	}
//
//	public GradeData getCurrentGradeData() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getGrade();
//			case COMPETENCE:
//				return competenceAssessmentBean.getCompetenceAssessmentData().getGradeData();
//		}
//		return null;
//	}
//
//	public String getCurrentResTitle() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getTitle();
//			case COMPETENCE:
//				return competenceAssessmentBean.getCompetenceAssessmentData().getTitle();
//		}
//		return null;
//	}
//
//	//prepare grading
//
//	public void prepareLearningResourceAssessmentForGrading(CompetenceAssessmentData assessment) {
//		competenceAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
//		currentResType = LearningResourceType.COMPETENCE;
//	}
//
//	public void prepareLearningResourceAssessmentForGrading(ActivityAssessmentData assessment) {
//		activityAssessmentBean.prepareLearningResourceAssessmentForGrading(assessment);
//		currentResType = LearningResourceType.ACTIVITY;
//	}

	//prepare grading end

	//GET DATA DEPENDING ON WHICH ASSESSMENT IS CURRENTLY SELECTED (COMPETENCE OR ACTIVITY) END

	//MARK DISCUSSION READ

	public void markActivityAssessmentDiscussionRead() {
		String encodedActivityDiscussionId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedActivityDiscussionId)) {
			assessmentManager.markActivityAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					idEncoder.decodeId(encodedActivityDiscussionId));
			Optional<ActivityAssessmentData> seenActivityAssessment = getActivityAssessmentByEncodedId(
					encodedActivityDiscussionId);
			seenActivityAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<ActivityAssessmentData> getActivityAssessmentByEncodedId(String encodedActivityDiscussionId) {
		for (CompetenceAssessmentData comp : assessments) {
			for (ActivityAssessmentData act : comp.getActivityAssessmentData()) {
				if (encodedActivityDiscussionId.equals(act.getEncodedActivityAssessmentId())) {
					return Optional.of(act);
				}
			}
		}
		return Optional.empty();
	}

	public void markCompetenceAssessmentDiscussionRead() {
		String encodedAssessmentId = getEncodedAssessmentIdFromRequest();

		if (!StringUtils.isBlank(encodedAssessmentId)) {
			long assessmentId = idEncoder.decodeId(encodedAssessmentId);
			assessmentManager.markCompetenceAssessmentDiscussionAsSeen(loggedUserBean.getUserId(),
					assessmentId);
			Optional<CompetenceAssessmentData> compAssessment = getCompetenceAssessmentById(assessmentId);
			compAssessment.ifPresent(data -> data.setAllRead(true));
		}
	}

	private Optional<CompetenceAssessmentData> getCompetenceAssessmentById(long assessmentId) {
		for (CompetenceAssessmentData ca : assessments) {
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


	//actions based on currently selected resource type

//	public List<AssessmentDiscussionMessageData> getCurrentAssessmentMessages() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getActivityDiscussionMessageData();
//			case COMPETENCE:
//				return competenceAssessmentData.getMessages();
//		}
//		return null;
//	}
//
//	public LearningResourceAssessmentBean getCurrentAssessmentBean() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean;
//			case COMPETENCE:
//				return this;
//		}
//		return null;
//	}
//
//	public void updateAssessmentGrade() {
//		try {
//			switch (currentResType) {
//				case ACTIVITY:
//					activityAssessmentBean.updateGrade();
//					break;
//				case COMPETENCE:
//					updateGrade();
//					break;
//			}
//		} catch (Exception e) {
//			logger.error("Error", e);
//		}
//	}
//
//	public long getCurrentAssessmentId() {
//		if (currentResType == null) {
//			return 0;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return idEncoder.decodeId(activityAssessmentBean.getActivityAssessmentData().getEncodedActivityAssessmentId());
//			case COMPETENCE:
//				return competenceAssessmentData.getCompetenceAssessmentId();
//		}
//		return 0;
//	}
//
//	public boolean hasStudentCompletedCurrentResource() {
//		if (currentResType == null) {
//			return false;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().isCompleted();
//			case COMPETENCE:
//				//for now
//				return true;
//		}
//		return false;
//	}
//
//	public String getCurrentResTitle() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getTitle();
//			case COMPETENCE:
//				return competenceAssessmentData.getTitle();
//		}
//		return null;
//	}
//
//	public GradeData getCurrentGradeData() {
//		if (currentResType == null) {
//			return null;
//		}
//		switch (currentResType) {
//			case ACTIVITY:
//				return activityAssessmentBean.getActivityAssessmentData().getGrade();
//			case COMPETENCE:
//				return competenceAssessmentData.getGradeData();
//		}
//		return null;
//	}

	//actions based on currently selected resource type end

	/*
	ACTIONS
	 */

	//comment actions


	// grading actions

	/*
	 * GETTERS / SETTERS
	 */

	public String getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(String competenceId) {
		this.competenceId = competenceId;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public List<CompetenceAssessmentData> getAssessments() {
		return assessments;
	}
}
