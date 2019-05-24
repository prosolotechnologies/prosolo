package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.filter.AssessmentStatusFilter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;

@ManagedBean(name = "myAssessmentsBeanCompetence")
@Component("myAssessmentsBeanCompetence")
@Scope("view")
public class MyAssessmentsBeanCompetence extends MyAssessmentsBean {

	private static Logger logger = Logger.getLogger(MyAssessmentsBeanCompetence.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected PaginatedResult<? extends AssessmentData> loadAndReturnAssessments(long userId, AssessmentStatusFilter filter, int offset, int limit) {
        return assessmentManager.getPaginatedCompetencePeerAssessmentsForAssessor(userId, filter, offset, limit);
    }
}
