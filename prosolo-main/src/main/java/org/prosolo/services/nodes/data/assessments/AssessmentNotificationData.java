package org.prosolo.services.nodes.data.assessments;

import org.prosolo.common.domainmodel.assessment.AssessmentType;

/**
 * @author stefanvuckovic
 * @date 2018-02-09
 * @since 1.2.0
 */
public class AssessmentNotificationData {

    private final long credentialId;
    private final long competenceId;
    private final long assessorId;
    private final long studentId;
    private final AssessmentType assessmentType;

    private AssessmentNotificationData(long credentialId, long competenceId, long assessorId, long studentId, AssessmentType assessmentType) {
        this.credentialId = credentialId;
        this.competenceId = competenceId;
        this.assessorId = assessorId;
        this.studentId = studentId;
        this.assessmentType = assessmentType;
    }

    public static AssessmentNotificationData of(long credentialId, long assessorId, long studentId, AssessmentType assessmentType) {
        return new AssessmentNotificationData(credentialId, 0, assessorId, studentId, assessmentType);
    }

    public static AssessmentNotificationData of(long credentialId, long competenceId, long assessorId, long studentId, AssessmentType assessmentType) {
        return new AssessmentNotificationData(credentialId, competenceId, assessorId, studentId, assessmentType);
    }

    public long getCredentialId() {
        return credentialId;
    }

    public long getCompetenceId() {
        return competenceId;
    }

    public long getAssessorId() {
        return assessorId;
    }

    public long getStudentId() {
        return studentId;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }
}
