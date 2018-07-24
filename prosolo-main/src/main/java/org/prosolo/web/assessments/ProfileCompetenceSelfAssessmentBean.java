package org.prosolo.web.assessments;

import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "profileCompetenceSelfAssessmentBean")
@Component("profileCompetenceSelfAssessmentBean")
@Scope("view")
public class ProfileCompetenceSelfAssessmentBean extends CompetenceSelfAssessmentBean {

	private static final long serialVersionUID = 1475690110876782121L;

	@Inject private ProfileCompetenceAssessmentBean competenceAssessmentBean;

	private String studId;
	private long decodedStudentId;

	@Override
	public void initSelfAssessment() {
		decodedStudentId = getIdEncoder().decodeId(studId);
		if (decodedStudentId > 0) {
			competenceAssessmentBean.setStudentId(studId);
			super.initSelfAssessment();
		} else {
			PageUtil.notFound();
		}
	}

	@Override
	protected long getStudentId() {
		return decodedStudentId;
	}

	@Override
	protected CompetenceAssessmentBean getCompetenceAssessmentBean() {
		return competenceAssessmentBean;
	}

	public String getStudId() {
		return studId;
	}

	public void setStudId(String studId) {
		this.studId = studId;
	}
}
