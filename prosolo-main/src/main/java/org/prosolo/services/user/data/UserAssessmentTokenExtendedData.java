package org.prosolo.services.user.data;

/**
 * @author stefanvuckovic
 * @date 2019-03-27
 * @since 1.3
 */
public class UserAssessmentTokenExtendedData extends UserAssessmentTokenData {

    private int numberOfTokensSpentPerRequest;

    public UserAssessmentTokenExtendedData(boolean assessmentTokensEnabled, boolean userAvailableForAssessments, int numberOfTokensAvailable, int numberOfTokensSpentPerRequest) {
        super(assessmentTokensEnabled, userAvailableForAssessments, numberOfTokensAvailable);
        this.numberOfTokensSpentPerRequest = numberOfTokensSpentPerRequest;
    }

    public int getNumberOfTokensSpentPerRequest() {
        return numberOfTokensSpentPerRequest;
    }

    public boolean doesUserHaveEnoughTokensForOneRequest() {
        return getNumberOfTokensAvailable() > getNumberOfTokensSpentPerRequest();
    }
}
