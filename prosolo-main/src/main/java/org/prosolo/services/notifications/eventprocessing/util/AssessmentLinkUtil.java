package org.prosolo.services.notifications.eventprocessing.util;

import org.apache.commons.lang3.StringUtils;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.web.util.page.PageSection;

/**
 * @author stefanvuckovic
 * @date 2018-03-15
 * @since 1.2.0
 */
public class AssessmentLinkUtil {

    public static String getCredentialAssessmentUrlForAssessedStudent(
            String encodedCredentialId,
            String encodedCredentialAssessmentId,
            AssessmentType assessmentType,
            PageSection section) {

        if (!StringUtils.isBlank(encodedCredentialId) &&
                !StringUtils.isBlank(encodedCredentialAssessmentId)) {

            return section.getPrefix() +
                    "/credentials/" + encodedCredentialId +
                    "/assessments/" +
                    (section == PageSection.MANAGE ? encodedCredentialAssessmentId : (assessmentType == AssessmentType.PEER_ASSESSMENT
                            ? "peer/" + encodedCredentialAssessmentId
                            : (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT ? "instructor" : "self")));
        } else {
            throw new IllegalArgumentException("Assessment notification link can't be created");
        }
    }

    public static String getCompetenceAssessmentUrlForAssessedStudent(
            String encodedCredentialId,
            String encodedCompetenceId,
            String encodedCompetenceAssessmentId,
            AssessmentType assessmentType,
            PageSection section) {

        if (!StringUtils.isBlank(encodedCredentialId) &&
                !StringUtils.isBlank(encodedCompetenceId) &&
                !StringUtils.isBlank(encodedCompetenceAssessmentId)) {
            return section.getPrefix() +
                    "/credentials/" + encodedCredentialId +
                    "/competences/" + encodedCompetenceId +
                    "/assessments/" +
                    (assessmentType == AssessmentType.PEER_ASSESSMENT
                            ? "peer/" + encodedCompetenceAssessmentId
                            : (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT ? "instructor" : "self"));
        } else {
            throw new IllegalArgumentException("Assessment notification link can't be created");
        }
    }

    public static String getCredentialAssessmentUrlForStudentPeerAssessor(
            String encodedCredentialAssessmentId) {

        if (!StringUtils.isBlank(encodedCredentialAssessmentId)) {
            return "/assessments/my/credentials/" + encodedCredentialAssessmentId;
        } else {
            throw new IllegalArgumentException("Credential assessment notification link can't be created");
        }
    }

    public static String getCompetenceAssessmentUrlForStudentPeerAssessor(
            String encodedCompetenceAssessmentId) {

        if (!StringUtils.isBlank(encodedCompetenceAssessmentId)) {
            return "/assessments/my/competences/" + encodedCompetenceAssessmentId;
        } else {
            throw new IllegalArgumentException("Competence assessment notification link can't be created");
        }
    }

}
