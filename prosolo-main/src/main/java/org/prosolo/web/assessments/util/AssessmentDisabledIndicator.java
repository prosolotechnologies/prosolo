package org.prosolo.web.assessments.util;

/**
 * Enum listing reasons an assessment submission is disabled. When the value is NONE,
 * then assessment submission is not disabled.
 *
 * @author Nikola Milikic
 * @date 2019-01-04
 * @since 1.2
 */
public enum AssessmentDisabledIndicator {
    
    NONE,   // when assessment submission is not disabled
    CREDENTIAL_NOT_GRADED,
    CREDENTIAL_COMPETENCES_NOT_GRADED,
    CREDENTIAL_ACTIVITY_NOT_GRADED,
    COMPETENCE_NOT_GRADED,
    COMPETENCE_ACTIVITY_NOT_GRADED;
}
