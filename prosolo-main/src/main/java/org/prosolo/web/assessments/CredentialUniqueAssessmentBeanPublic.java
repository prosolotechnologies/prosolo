package org.prosolo.web.assessments;

import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;

@ManagedBean(name = "credentialUniqueAssessmentBeanPublic")
@Component("credentialUniqueAssessmentBeanPublic")
@Scope("view")
public class CredentialUniqueAssessmentBeanPublic extends CredentialUniqueAssessmentBean implements Serializable {

	private String studentId;
	private long decodedStudentId;

	public void initInstructorAssessment() {
		decodedStudentId = getIdEncoder().decodeId(studentId);
		if (decodedStudentId > 0) {
			super.initInstructorAssessment();
		} else {
			PageUtil.notFound();
		}
	}

	public void initSelfAssessment() {
		decodedStudentId = getIdEncoder().decodeId(studentId);
		if (decodedStudentId > 0) {
			super.initSelfAssessment();
		} else {
			PageUtil.notFound();
		}
	}

	@Override
	protected AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.PUBLIC;
	}

	@Override
	protected long getUserId() {
		return decodedStudentId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public long getDecodedStudentId() {
		return decodedStudentId;
	}
}
