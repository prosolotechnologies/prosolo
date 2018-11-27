package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "profileCompetencePeerAssessmentsBean")
@Component("profileCompetencePeerAssessmentsBean")
@Scope("view")
public class ProfileCompetencePeerAssessmentsBean extends CompetencePeerAssessmentsBean {

	private static final long serialVersionUID = -1552347014869261765L;

	private static Logger logger = Logger.getLogger(ProfileCompetencePeerAssessmentsBean.class);

	@Inject private UserManager userManager;

	private String studentId;
	private long decodedStudentId;

	private UserData student;

	@Override
	public void init() {
		decodedStudentId = getIdEncoder().decodeId(studentId);
		if (decodedStudentId > 0) {
			super.init();
		} else {
			PageUtil.notFound();
		}
	}

	@Override
	void loadAdditionalData() {
		student = userManager.getUserData(decodedStudentId);
	}

	@Override
	boolean isUserAllowedToAccess() {
		return getCompManager().isCompetenceAssessmentDisplayEnabled(getDecodedCompId(), decodedStudentId);
	}

	@Override
	AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.PUBLIC;
	}

	@Override
	long getStudentId() {
		return decodedStudentId;
	}

	public String getStudId() {
		return studentId;
	}

	public void setStudId(String studId) {
		this.studentId = studId;
	}

	public UserData getStudent() {
		return student;
	}
}
