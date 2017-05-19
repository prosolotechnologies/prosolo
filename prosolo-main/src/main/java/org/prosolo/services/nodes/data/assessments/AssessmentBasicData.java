package org.prosolo.services.nodes.data.assessments;

/**
 * Created by stefanvuckovic on 5/18/17.
 */
public class AssessmentBasicData {

    private long credentialAssessmentId;
    private long competenceAssessmentId;
    private long activityAssessmentId;

    private AssessmentBasicData(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId) {
        this.credentialAssessmentId = credentialAssessmentId;
        this.competenceAssessmentId = competenceAssessmentId;
        this.activityAssessmentId = activityAssessmentId;
    }

    public static AssessmentBasicData of(long credentialAssessmentId, long competenceAssessmentId, long activityAssessmentId) {
        return new AssessmentBasicData(credentialAssessmentId, competenceAssessmentId, activityAssessmentId);
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
}
