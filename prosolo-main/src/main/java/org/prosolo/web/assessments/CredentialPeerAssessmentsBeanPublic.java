package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

@ManagedBean(name = "credentialPeerAssessmentsBeanPublic")
@Component("credentialPeerAssessmentsBeanPublic")
@Scope("view")
public class CredentialPeerAssessmentsBeanPublic extends CredentialPeerAssessmentsBean {

	private static final long serialVersionUID = -7557523596097487650L;

	private static Logger logger = Logger.getLogger(CredentialPeerAssessmentsBeanPublic.class);

	@Inject private UserManager userManager;

	private String studentId;
	private long decodedStudentId;

	private UserData student;

	public void init() {
		try {
			decodeCredentialId();
			decodedStudentId = getIdEncoder().decodeId(studentId);

			if (getDecodedId() > 0 && decodedStudentId > 0) {
				boolean displayEnabled = getCredentialManager().isCredentialAssessmentDisplayEnabled(getDecodedId(), decodedStudentId);

				if (!displayEnabled) {
					PageUtil.accessDenied();
				} else {
					student = userManager.getUserData(decodedStudentId);
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

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public long getDecodedStudentId() {
		return decodedStudentId;
	}

	@Override
	protected AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.PUBLIC;
	}

	public UserData getStudent() {
		return student;
	}
}
