package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.filter.AssessmentStatusFilter;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "myAssessmentsBeanCredential")
@Component("myAssessmentsBeanCredential")
@Scope("view")
public class MyAssessmentsBeanCredential extends MyAssessmentsBean {

	private static Logger logger = Logger.getLogger(MyAssessmentsBeanCredential.class);


	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected PaginatedResult<? extends AssessmentData> loadAndReturnAssessments(long userId, AssessmentStatusFilter filter, int offset, int limit) {
		return assessmentManager.getPaginatedCredentialPeerAssessmentsForAssessor(userId, filter, offset, limit);
	}
}
