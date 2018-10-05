package org.prosolo.web.assessments;

import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@ManagedBean(name = "competenceSelfAssessmentStudentBean")
@Component("competenceSelfAssessmentStudentBean")
@Scope("view")
public class CompetenceSelfAssessmentStudentBean extends CompetenceSelfAssessmentBean {

	private static final long serialVersionUID = -7222449338832725797L;

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private StudentCompetenceAssessmentBean competenceAssessmentBean;

	@Override
	protected long getStudentId() {
		return loggedUserBean.getUserId();
	}

	@Override
	protected CompetenceAssessmentBean getCompetenceAssessmentBean() {
		return competenceAssessmentBean;
	}

}
