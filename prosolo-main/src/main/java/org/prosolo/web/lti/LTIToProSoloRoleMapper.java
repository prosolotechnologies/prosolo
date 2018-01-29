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
            {"urn:lti:sysrole:ims/lis/SysAdmin", null},
            {"urn:lti:sysrole:ims/lis/SysSupport", null},
            {"urn:lti:sysrole:ims/lis/Creator", SystemRoleNames.MANAGER},
            {"urn:lti:sysrole:ims/lis/AccountAdmin", null},
            {"urn:lti:sysrole:ims/lis/User", SystemRoleNames.USER},
            {"urn:lti:sysrole:ims/lis/Administrator", null},
            {"urn:lti:sysrole:ims/lis/None", null},

            // LTI Institution Roles
            {"urn:lti:instrole:ims/lis/Student", SystemRoleNames.USER},
            {"urn:lti:instrole:ims/lis/Faculty", SystemRoleNames.MANAGER},
            {"urn:lti:instrole:ims/lis/Member", SystemRoleNames.USER},
            {"urn:lti:instrole:ims/lis/Learner", SystemRoleNames.USER},
            {"urn:lti:instrole:ims/lis/Instructor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:instrole:ims/lis/Mentor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:instrole:ims/lis/Staff", SystemRoleNames.MANAGER},
            {"urn:lti:instrole:ims/lis/Alumni", null},
            {"urn:lti:instrole:ims/lis/ProspectiveStudent", SystemRoleNames.USER},
            {"urn:lti:instrole:ims/lis/Guest", null},
            {"urn:lti:instrole:ims/lis/Other", null},
            //{"Administrator", null},  // already defined
            {"urn:lti:instrole:ims/lis/Observer", null},
            //{"None", null},   // already defined

            // LTI Context Roles
            //{"Learner", SystemRoleNames.USER},    // already defined
            {"urn:lti:role:ims/lis/Learner/Learner", SystemRoleNames.USER},
            {"urn:lti:role:ims/lis/Learner/NonCreditLearner", SystemRoleNames.USER},
            {"urn:lti:role:ims/lis/Learner/GuestLearner", SystemRoleNames.USER},
            {"urn:lti:role:ims/lis/Learner/ExternalLearner", SystemRoleNames.USER},
            {"urn:lti:role:ims/lis/Learner/Instructor", SystemRoleNames.USER},
            //{"Instructor", SystemRoleNames.INSTRUCTOR},   // already defined
            {"urn:lti:role:ims/lis/Instructor/PrimaryInstructor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Instructor/Lecturer", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Instructor/GuestInstructor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Instructor/ExternalInstructor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/ContentDeveloper", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/ContentDeveloper/ContentDeveloper", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/ContentDeveloper/Librarian", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/ContentDeveloper/ContentExpert", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/ContentDeveloper/ExternalContentExpert", SystemRoleNames.MANAGER},
            // {"Member", null},    // already defined
            {"urn:lti:role:ims/lis/Member/Member", SystemRoleNames.USER},
            {"urn:lti:role:ims/lis/Manager", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/Manager/AreaManager", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/Manager/CourseCoordinator", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/Manager/Observer", SystemRoleNames.MANAGER},
            {"urn:lti:role:ims/lis/Manager/ExternalObserver", SystemRoleNames.MANAGER},
            //{"Mentor", SystemRoleNames.INSTRUCTOR},    // already defined
            {"urn:lti:role:ims/lis/Mentor/Mentor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/Reviewer", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/Advisor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/Auditor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/Tutor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/LearningFacilitator", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalMentor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalReviewer", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalAdvisor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalAuditor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalTutor", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/Mentor/ExternalLearningFacilitator", SystemRoleNames.INSTRUCTOR},
            //{"Administrator", null},    // already defined
            {"urn:lti:role:ims/lis/Administrator/Administrator", null},
            {"urn:lti:role:ims/lis/Administrator/Support", null},
            {"urn:lti:role:ims/lis/Administrator/Developer", null},
            {"urn:lti:role:ims/lis/Administrator/SystemAdministrator", null},
            {"urn:lti:role:ims/lis/Administrator/ExternalSystemAdministrator", null},
            {"urn:lti:role:ims/lis/Administrator/ExternalDeveloper", null},
            {"urn:lti:role:ims/lis/Administrator/ExternalSupport", null},
            {"urn:lti:role:ims/lis/TeachingAssistant", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistant", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantSection", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantSectionAssociation", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantOffering", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantTemplate", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/TeachingAssistantGroup", SystemRoleNames.INSTRUCTOR},
            {"urn:lti:role:ims/lis/TeachingAssistant/Grader", SystemRoleNames.INSTRUCTOR},
    });

    public static String getRole(String ltiRoleName) {
        return (String) mappings.get(ltiRoleName);
    }

}
