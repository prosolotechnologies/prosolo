package org.prosolo.web.lti;

import org.apache.commons.lang3.ArrayUtils;
import org.prosolo.services.util.roles.SystemRoleNames;

import java.util.Map;

/**
 * @author Nikola Milikic
 * @date 2018-01-29
 * @since 1.2
 */
public class LTIToProSoloRoleMapper {

    // some roles could be mapped to SystemRoleNames.ADMIN, but we don't want to allow creating
    // admin accounts over LTI
    private static final Map mappings = ArrayUtils.toMap(new String[][] {
            // LTI System Roles
            {"SysAdmin", null},
            {"SysSupport", null},
            {"Creator", SystemRoleNames.MANAGER},
            {"AccountAdmin", null},
            {"User", SystemRoleNames.USER},
            {"Administrator", null},
            {"None", null},

            // LTI Institution Roles
            {"Student", SystemRoleNames.USER},
            {"Faculty", SystemRoleNames.MANAGER},
            {"Member", SystemRoleNames.USER},
            {"Learner", SystemRoleNames.USER},
            {"Instructor", SystemRoleNames.INSTRUCTOR},
            {"Mentor", SystemRoleNames.INSTRUCTOR},
            {"Staff", SystemRoleNames.MANAGER},
            {"Alumni", null},
            {"ProspectiveStudent", SystemRoleNames.USER},
            {"Guest", null},
            {"Other", null},
            //{"Administrator", null},  // already defined
            {"Observer", null},
            //{"None", null},   // already defined

            // LTI Context Roles
            //{"Learner", SystemRoleNames.USER},    // already defined
            {"Learner/Learner", SystemRoleNames.USER},
            {"Learner/NonCreditLearner", SystemRoleNames.USER},
            {"Learner/GuestLearner", SystemRoleNames.USER},
            {"Learner/ExternalLearner", SystemRoleNames.USER},
            {"Learner/Instructor", SystemRoleNames.USER},
            //{"Instructor", SystemRoleNames.INSTRUCTOR},   // already defined
            {"Instructor/PrimaryInstructor", SystemRoleNames.INSTRUCTOR},
            {"Instructor/Lecturer", SystemRoleNames.INSTRUCTOR},
            {"Instructor/GuestInstructor", SystemRoleNames.INSTRUCTOR},
            {"Instructor/ExternalInstructor", SystemRoleNames.INSTRUCTOR},
            {"ContentDeveloper", SystemRoleNames.MANAGER},
            {"ContentDeveloper/ContentDeveloper", SystemRoleNames.MANAGER},
            {"ContentDeveloper/Librarian", SystemRoleNames.MANAGER},
            {"ContentDeveloper/ContentExpert", SystemRoleNames.MANAGER},
            {"ContentDeveloper/ExternalContentExpert", SystemRoleNames.MANAGER},
            // {"Member", null},    // already defined
            {"Member/Member", SystemRoleNames.USER},
            {"Manager", SystemRoleNames.MANAGER},
            {"Manager/AreaManager", SystemRoleNames.MANAGER},
            {"Manager/CourseCoordinator", SystemRoleNames.MANAGER},
            {"Manager/Observer", SystemRoleNames.MANAGER},
            {"Manager/ExternalObserver", SystemRoleNames.MANAGER},
            //{"Mentor", SystemRoleNames.INSTRUCTOR},    // already defined
            {"Mentor/Mentor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/Reviewer", SystemRoleNames.INSTRUCTOR},
            {"Mentor/Advisor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/Auditor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/Tutor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/LearningFacilitator", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalMentor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalReviewer", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalAdvisor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalAuditor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalTutor", SystemRoleNames.INSTRUCTOR},
            {"Mentor/ExternalLearningFacilitator", SystemRoleNames.INSTRUCTOR},
            //{"Administrator", null},    // already defined
            {"Administrator/Administrator", null},
            {"Administrator/Support", null},
            {"Administrator/Developer", null},
            {"Administrator/SystemAdministrator", null},
            {"Administrator/ExternalSystemAdministrator", null},
            {"Administrator/ExternalDeveloper", null},
            {"Administrator/ExternalSupport", null},
            {"TeachingAssistant", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistant", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistantSection", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistantSectionAssociation", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistantOffering", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistantTemplate", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/TeachingAssistantGroup", SystemRoleNames.INSTRUCTOR},
            {"TeachingAssistant/Grader", SystemRoleNames.INSTRUCTOR},
    });

    public static String getRole(String ltiRoleName) {
        return (String) mappings.get(ltiRoleName);
    }

}
