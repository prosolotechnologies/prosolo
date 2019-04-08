package org.prosolo.services.assessment.data.filter;

import org.prosolo.common.domainmodel.assessment.Assessment;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-04-05
 * @since 1.3
 */
public enum AssessmentStatusFilter {

    ALL(List.of()),
    REQUESTED(List.of(AssessmentStatus.REQUESTED)),
    REQUEST_DECLINED(List.of(AssessmentStatus.REQUEST_DECLINED)),
    REQUEST_EXPIRED(List.of(AssessmentStatus.REQUEST_EXPIRED)),
    PENDING(List.of(AssessmentStatus.PENDING)),
    SUBMITTED(List.of(AssessmentStatus.SUBMITTED)),
    ASSESSMENT_QUIT(List.of(AssessmentStatus.ASSESSMENT_QUIT, AssessmentStatus.SUBMITTED_ASSESSMENT_QUIT));

    private List<AssessmentStatus> statuses;

    AssessmentStatusFilter(List<AssessmentStatus> statuses) {
        this.statuses = statuses;
    }

    public List<AssessmentStatus> getStatuses() {
        return statuses;
    }
}
