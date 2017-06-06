package org.prosolo.services.nodes.data.assessments;

/**
 * Created by stefanvuckovic on 5/18/17.
 */
public class AssessmentBasicData {

    private final long credentialAssessmentId;
    private final long competenceAssessmentId;
    private final long activityAssessmentId;
    private final long assessorId;
    private final boolean isDefault;

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.assessorId = 0;
        this.isDefault = false;
    }

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId,
                                long assessorId, boolean isDefault) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
        this.assessorId = assessorId;
        this.isDefault = isDefault;
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId);
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId,
                                         long activityAssessmentId, long assessorId, boolean isDefault) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId,
                assessorId, isDefault);
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

    public boolean isDefault() {
        return isDefault;
    }
}
