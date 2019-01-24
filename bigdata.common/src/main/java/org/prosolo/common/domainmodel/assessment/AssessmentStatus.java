package org.prosolo.common.domainmodel.assessment;

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
    SUBMITTED
}
