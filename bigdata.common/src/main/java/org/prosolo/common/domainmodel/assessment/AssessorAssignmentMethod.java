package org.prosolo.common.domainmodel.assessment;

/**
 * This enum is used for configuring a method by which an instructor is assigned to a student in a credential delivery.
 *
 * @author Nikola Milikic
 * @date 2018-07-23
 * @since 1.2
 */
public enum AssessorAssignmentMethod {

    AUTOMATIC,      // Assessors are assigned to students automatically
    MANUAL,         // Assessors are assigned to students manually
    BY_STUDENTS     // Student can choose assessor
    ;
}
