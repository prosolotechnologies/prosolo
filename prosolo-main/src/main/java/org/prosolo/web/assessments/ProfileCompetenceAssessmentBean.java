package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;

/**
 * @author stefanvuckovic
 *
 */

@ManagedBean(name = "profileCompetenceAssessmentBean")
@Component("profileCompetenceAssessmentBean")
@Scope("view")
public class ProfileCompetenceAssessmentBean extends CompetenceAssessmentBean {

	private static final long serialVersionUID = 6117443987043924595L;

	private static Logger logger = Logger.getLogger(ProfileCompetenceAssessmentBean.class);

	private String studentId;
	private long decodedStudentId;

	@Override
	public void initAssessment(AssessmentType assessmentType) {
		decodedStudentId = getIdEncoder().decodeId(studentId);
		if (decodedStudentId > 0) {
			super.initAssessment(assessmentType);
		} else {
			PageUtil.notFound();
		}
	}

	@Override
	boolean canAccessPreLoad() {
		return getCompManager().isCompetenceAssessmentDisplayEnabled(getDecodedCompId(), decodedStudentId);
	}

	@Override
	boolean canAccessPostLoad() {
		return true;
	}

	@Override
	AssessmentDisplayMode getDisplayMode() {
		return AssessmentDisplayMode.PUBLIC;
	}

	@Override
	public GradeData getGradeData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RubricCriteriaGradeData getRubricForLearningResource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void editComment(String newContent, String activityMessageEncodedId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void addComment() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGrade() throws DbConnectionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public AssessmentType getType() {
		throw new UnsupportedOperationException();
	}

	/*
	ACTIONS
	 */

	/*
	 * GETTERS / SETTERS
	 */

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
}
