package org.prosolo.common.domainmodel.organization.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTokensPlugin extends OrganizationPlugin {

    private int initialNumberOfTokensGiven;
    private int numberOfSpentTokensPerRequest;
    private int numberOfEarnedTokensPerAssessment;

    public int getInitialNumberOfTokensGiven() {
        return initialNumberOfTokensGiven;
    }

    public void setInitialNumberOfTokensGiven(int initialNumberOfTokensGiven) {
        this.initialNumberOfTokensGiven = initialNumberOfTokensGiven;
    }

    public int getNumberOfSpentTokensPerRequest() {
        return numberOfSpentTokensPerRequest;
    }

    public void setNumberOfSpentTokensPerRequest(int numberOfSpentTokensPerRequest) {
        this.numberOfSpentTokensPerRequest = numberOfSpentTokensPerRequest;
    }

    public int getNumberOfEarnedTokensPerAssessment() {
        return numberOfEarnedTokensPerAssessment;
    }

    public void setNumberOfEarnedTokensPerAssessment(int numberOfEarnedTokensPerAssessment) {
        this.numberOfEarnedTokensPerAssessment = numberOfEarnedTokensPerAssessment;
    }

}
