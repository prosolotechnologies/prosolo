package org.prosolo.services.nodes.data.assessments;

/**
 * Created by stefanvuckovic on 5/18/17.
 */
public class AssessmentBasicData {

    private final long credentialAssessmentId;
    private final long competenceAssessmentId;
    private final long activityAssessmentId;
    private final long assessorId;

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.assessorId = 0;
    }

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId,
                                long assessorId) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.assessorId = assessorId;
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId);
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId, long assessorId) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId, assessorId);
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
}
