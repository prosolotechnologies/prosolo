package org.prosolo.web.assessments;

import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "credentialUniqueAssessmentStudentBean")
@Component("credentialUniqueAssessmentStudentBean")
@Scope("view")
public class CredentialUniqueAssessmentStudentBean extends CredentialUniqueAssessmentBean implements Serializable {

	@Inject private LoggedUserBean loggedUserBean;

	@Override
	protected long getUserId() {
		return loggedUserBean.getUserId();
	}

	@Override
	protected AssessmentDisplayMode getAssessmentDisplayMode() {
		return AssessmentDisplayMode.FULL;
	}

}
