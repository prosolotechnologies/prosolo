package org.prosolo.services.user.data;

/**
 * A mode that defines how does instructor removal from user group affect his role as instructor in credentials where
 * this user group is used.
 *
 * @author stefanvuckovic
 * @date 2019-08-16
 * @since 1.3.3
 */
public enum UserGroupInstructorRemovalMode {

    LEAVE_AS_INSTRUCTOR,
    INACTIVATE,
    WITHDRAW_FROM_STUDENTS_WITH_UNSUBMITTED_ASSESSMENT_AND_INACTIVATE


}
