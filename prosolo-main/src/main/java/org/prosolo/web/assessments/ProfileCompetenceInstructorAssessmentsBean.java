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

/**
 * @author stefanvuckovic
 *
 */

@ManagedBean(name = "profileCompetenceInstructorAssessmentsBean")
@Component("profileCompetenceInstructorAssessmentsBean")
@Scope("view")
public class ProfileCompetenceInstructorAssessmentsBean extends CompetenceInstructorAssessmentsBean {

	private static final long serialVersionUID = -6546882440222174687L;

	private static Logger logger = Logger.getLogger(ProfileCompetenceInstructorAssessmentsBean.class);

	@Inject private UserManager userManager;

	private String studId;
	private long decodedStudentId;

	private UserData student;

	public void init() {
		decodeCredentialAndCompetenceIds();
		decodedStudentId = getIdEncoder().decodeId(studId);
		if (getDecodedCompId() > 0 && decodedStudentId > 0) {
			try {
				boolean displayEnabled = getCompManager().isCompetenceAssessmentDisplayEnabled(getDecodedCompId(), decodedStudentId);

				if (!displayEnabled) {
					PageUtil.accessDenied();
				} else {
					student = userManager.getUserData(decodedStudentId);
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
		return decodedStudentId;
	}

	@Override
	AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.PUBLIC;
	}

	/*
	 * GETTERS / SETTERS
	 */

	public void setStudent(UserData student) {
		this.student = student;
	}

	public UserData getStudent() {
		return student;
	}

	public String getStudId() {
		return studId;
	}

	public void setStudId(String studId) {
		this.studId = studId;
	}
}
