package org.prosolo.web.assessments;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.*;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.assessment.data.grading.RubricGradeData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This bean is used on pages for instructor and self assessments as those
 * two assessment types are unique for given credential and student
 */
@ManagedBean(name = "credentialUniqueAssessmentStudentBean")
@Component("credentialUniqueAssessmentStudentBean")
@Scope("view")
public class CredentialUniqueAssessmentStudentBean implements Serializable {

	private static final long serialVersionUID = -4009700290866858467L;

	private static Logger logger = Logger.getLogger(CredentialUniqueAssessmentStudentBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private AssessmentManager assessmentManager;
	@Inject private CredentialAssessmentBean credentialAssessmentBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	public void initInstructorAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getInstructorCredentialAssessmentId(decodedId, loggedUserBean.getUserId());
			if (optAssessmentId.isPresent()) {
				credentialAssessmentBean.initAssessment(id, idEncoder.encodeId(optAssessmentId.get()));
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void initSelfAssessment() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			Optional<Long> optAssessmentId = assessmentManager.getSelfCredentialAssessmentId(decodedId, loggedUserBean.getUserId());
			if (optAssessmentId.isPresent()) {
				credentialAssessmentBean.initAssessment(id, idEncoder.encodeId(optAssessmentId.get()));
			} else {
				PageUtil.notFound();
			}
		} else {
			PageUtil.notFound();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
