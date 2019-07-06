package org.prosolo.common.domainmodel.assessment;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-01-24
 * @since 1.3
 */
public enum AssessmentStatus {

    REQUESTED,
    REQUEST_EXPIRED,
    REQUEST_DECLINED,
    /*
    it means assessment is in progress; when assessor accepts assessment request or
    assessment is created without request
     */
    PENDING,
    ASSESSMENT_QUIT,
    SUBMITTED,
    SUBMITTED_ASSESSMENT_QUIT;

    public static List<AssessmentStatus> getActiveStatuses() {
        return List.of(REQUESTED, PENDING, SUBMITTED);
    }

    public static List<AssessmentStatus> getInactiveStatuses() {
        return List.of(REQUEST_DECLINED, REQUEST_EXPIRED, ASSESSMENT_QUIT, SUBMITTED_ASSESSMENT_QUIT);
    }
}
