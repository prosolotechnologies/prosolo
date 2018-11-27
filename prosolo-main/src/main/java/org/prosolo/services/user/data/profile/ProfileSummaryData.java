package org.prosolo.services.user.data.profile;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class ProfileSummaryData implements Serializable {

    private static final long serialVersionUID = -3598064489753143720L;

    private final long numberOfCompletedCredentials;
    private final long numberOfCompletedCompetencies;
    private final long numberOfAssessments;
    private final long numberOfPiecesOfEvidence;

    public ProfileSummaryData(long numberOfCompletedCredentials, long numberOfCompletedCompetencies, long numberOfAssessments, long numberOfPiecesOfEvidence) {
        this.numberOfCompletedCredentials = numberOfCompletedCredentials;
        this.numberOfCompletedCompetencies = numberOfCompletedCompetencies;
        this.numberOfAssessments = numberOfAssessments;
        this.numberOfPiecesOfEvidence = numberOfPiecesOfEvidence;
    }

    public long getNumberOfCompletedCredentials() {
        return numberOfCompletedCredentials;
    }

    public long getNumberOfCompletedCompetencies() {
        return numberOfCompletedCompetencies;
    }

    public long getNumberOfAssessments() {
        return numberOfAssessments;
    }

    public long getNumberOfPiecesOfEvidence() {
        return numberOfPiecesOfEvidence;
    }
}
