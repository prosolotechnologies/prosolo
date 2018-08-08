package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.grading.AutomaticGradeData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricCriteriaGradeData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.assessments.util.AssessmentDisplayMode;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.Date;

/**
 * @author Bojan
 *
 */

@ManagedBean(name = "profileCredentialPeerAssessmentBean")
@Component("profileCredentialPeerAssessmentBean")
@Scope("view")
public class ProfileCredentialPeerAssessmentBean extends CredentialAssessmentBean {

	private static final long serialVersionUID = -3938057856496283464L;

	private static Logger logger = Logger.getLogger(ProfileCredentialPeerAssessmentBean.class);

	private String studentId;
	private long decodedStudentId;

	public void init() {
		decodedStudentId = getIdEncoder().decodeId(studentId);
		if (decodedStudentId > 0) {
			setDisplayMode(AssessmentDisplayMode.PUBLIC);
			boolean success = initPeerAssessmentAndReturnSuccessMessage();
			if (success) {
				//if passed student id is different than actual student id from assessment redirect to not found page
				if (decodedStudentId != getFullAssessmentData().getAssessedStudentId()) {
					PageUtil.notFound();
				}
			}
		} else {
			PageUtil.notFound();
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
}
