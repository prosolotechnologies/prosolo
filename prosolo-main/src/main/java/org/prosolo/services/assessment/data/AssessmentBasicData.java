package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.assessment.AssessmentType;

/**
 * Created by stefanvuckovic on 5/18/17.
 */
public class AssessmentBasicData {

    private final long credentialAssessmentId;
    private final long competenceAssessmentId;
    private final long activityAssessmentId;
    private final long studentId;
    private final long assessorId;
    private final int grade;
    private final AssessmentType type;

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId, int grade) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.studentId = 0;
        this.assessorId = 0;
        this.grade = grade;
        this.type = null;
    }

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId,
                                long assessorId, AssessmentType type) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.studentId = 0;
        this.assessorId = assessorId;
        this.type = type;
        this.grade = -1;
    }

    private AssessmentBasicData(long studentId, long assessorId, AssessmentType type) {
        this.credentialAssessmentId = 0;
        this.competenceAssessmentId = 0;
        this.activityAssessmentId = 0;
        this.studentId = studentId;
        this.assessorId = assessorId;
        this.type = type;
        this.grade = -1;
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId, -1);
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId, int grade) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId, grade);
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId, long assessorId, AssessmentType type) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId,
                assessorId, type);
    }

    public static AssessmentBasicData of(long studentId, long assessorId, AssessmentType type) {
        return new AssessmentBasicData(studentId, assessorId, type);
    }

    public static AssessmentBasicData empty() {
        return new AssessmentBasicData(0, 0, 0, -1);
    }

    public long getCredentialAssessmentId() {
        return credentialAssessmentId;
    }

    public long getCompetenceAssessmentId() {
        return competenceAssessmentId;
    }

    public long getActivityAssessmentId() {
        return activityAssessmentId;
    }

    public long getAssessorId() {
        return assessorId;
    }

    public AssessmentType getType() {
        return type;
    }

    public long getStudentId() {
        return studentId;
    }

    public int getGrade() {
        return grade;
    }
}