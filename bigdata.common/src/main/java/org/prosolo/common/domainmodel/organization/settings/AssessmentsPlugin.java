package org.prosolo.common.domainmodel.organization.settings;

import lombok.*;
import org.prosolo.common.domainmodel.organization.Organization;

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
@NoArgsConstructor
public class AssessmentsPlugin extends OrganizationPlugin {

    private boolean assessmentTokensEnabled;
    private boolean privateDiscussionEnabled;
    private int initialNumberOfTokensGiven;
    private int numberOfSpentTokensPerRequest;
    private int numberOfEarnedTokensPerAssessment;

    @Builder
    public AssessmentsPlugin(long id, boolean enabled, OrganizationPluginType type, Organization organization,
                             boolean assessmentTokensEnabled, boolean privateDiscussionEnabled, int initialNumberOfTokensGiven,
                             int numberOfSpentTokensPerRequest, int numberOfEarnedTokensPerAssessment) {
        super(id, enabled, type, organization);
        this.assessmentTokensEnabled = assessmentTokensEnabled;
        this.privateDiscussionEnabled = privateDiscussionEnabled;
        this.initialNumberOfTokensGiven = initialNumberOfTokensGiven;
        this.numberOfSpentTokensPerRequest = numberOfSpentTokensPerRequest;
        this.numberOfEarnedTokensPerAssessment = numberOfEarnedTokensPerAssessment;
    }

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

    public boolean isAssessmentTokensEnabled() {
        return assessmentTokensEnabled;
    }

    public void setAssessmentTokensEnabled(boolean assessmentTokensEnabled) {
        this.assessmentTokensEnabled = assessmentTokensEnabled;
    }

    public boolean isPrivateDiscussionEnabled() {
        return privateDiscussionEnabled;
    }

    public void setPrivateDiscussionEnabled(boolean privateDiscussionEnabled) {
        this.privateDiscussionEnabled = privateDiscussionEnabled;
    }
}
