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

@ManagedBean(name = "credentialPeerAssessmentsBeanPublic")
@Component("credentialPeerAssessmentsBeanPublic")
@Scope("view")
public class CredentialPeerAssessmentsBeanPublic extends CredentialPeerAssessmentsBean {

	private static final long serialVersionUID = -7557523596097487650L;

	private static Logger logger = Logger.getLogger(CredentialPeerAssessmentsBeanPublic.class);

	@Inject private UserManager userManager;

	private String studId;
	private long decodedStudentId;

	private UserData student;

	public void init() {
		try {
			decodeCredentialId();
			decodedStudentId = getIdEncoder().decodeId(studId);

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

	@Override
	public long getStudentId() {
		return decodedStudentId;
	}

	public String getStudId() {
		return studId;
	}

	public void setStudId(String studId) {
		this.studId = studId;
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
